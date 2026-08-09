# [백엔드 기본기 Day9] 관계형 DB 접근 계층 — JDBC 직접 구현과 저장 계약

Day8에서 `V1__init.sql`로 `reservation` 테이블을 만들어두고도 애플리케이션은 여전히 `InMemoryReservationRepository`의 `ArrayList`에 예약을 담고 있었다. 오늘은 그 자리를 순수 JDBC 구현으로 교체했다. JPA는 쓰지 않았다 — 다음 Day에 지워질 코드가 얼마나 되는지를 눈으로 보려면 먼저 손으로 써봐야 하기 때문이다.

> `DataSource`에서 커넥션을 빌려 `PreparedStatement`로 INSERT·SELECT를 실행하는 Repository를 만들고, 앱을 강제 종료했다 재기동해도 예약이 남아 있는 것을 확인했다. 테스트 10개가 전부 통과한 상태에서 만든 적 없는 3번 예약이 조회되는 것을 발견했고, 원인은 `save()`가 무조건 INSERT만 하도록 구현된 것이었다. 이 결함은 아직 수정하지 않았다.

## 1. 개념 설명

JDBC는 하나의 저장 동작을 네 개의 객체로 쪼갠다. 앞의 셋은 자원 관리의 대상이고, 마지막 하나는 사고방식을 바꿔야 하는 대상이다.

| 용어 | 한줄뜻 | 코드 모습 |
|---|---|---|
| `DataSource` | 커넥션 풀을 감싼 창구. "연결 하나 빌려줘" | `dataSource.getConnection()` |
| `Connection` | DB와의 세션 하나. TCP 연결과 인증이 끝난 상태 | try-with-resources로 반납 |
| `PreparedStatement` | 구조가 먼저 굳은 SQL. 값은 `?` 자리에 따로 넣는다 | `ps.setLong(1, id)` |
| `ResultSet` | 결과 행을 가리키는 **커서**. 행들을 담은 컬렉션이 아니다 | `while (rs.next())` |
| `rs.next()` | 커서를 다음 행으로 **이동**하고, 행이 있으면 `true` | 행을 넘길 때만 쓴다 |
| `rs.getXxx("컬럼")` | 지금 커서가 있는 행에서 값만 읽는다. 이동하지 않는다 | `rs.getBoolean("confirmed")` |
| `save()`의 계약 | "저장한다"가 아니라 "id가 없으면 신규, 있으면 갱신" | `getId() == null` 분기 |

`PreparedStatement`의 `?`가 SQL Injection을 막는 이유는 값을 검사해서가 아니다. DB가 **SQL의 구조를 먼저 파싱해 굳혀두고** 그다음에 값을 채우기 때문에, 값에 무엇이 들어오든 구조를 바꿀 수 없다. 문자열을 이어붙이면 값과 구조가 같은 문장 안에 섞여 들어가고, 그때부터는 파서가 둘을 구분할 방법이 없다.

`ResultSet`은 자료구조 시간의 Iterator와 같은 물건이다. 결과 전체를 메모리에 들고 있는 게 아니라 한 행씩 가리키는 위치일 뿐이라, 값을 읽기 전에 반드시 커서를 옮겨야 한다. 여기서 `next()`가 **이동과 판정을 동시에** 한다는 점이 함정이 된다.

> `next()`는 행을 넘길 때만, `getXxx()`는 값을 읽을 때만.

그리고 `Connection`·`PreparedStatement`·`ResultSet`은 셋 다 닫아야 하는 자원이다. try-with-resources는 소괄호 안에서 연 것을 블록을 벗어날 때 예외 발생 여부와 무관하게 역순으로 닫는다. 반납하지 않으면 커넥션 풀이 마르고, 그때는 이 기능 하나가 아니라 서버 전체가 멈춘다.

> **더 볼 것**
> - [Data Access with JDBC :: Spring Framework](https://docs.spring.io/spring-framework/reference/data-access/jdbc.html): `DataSource` 추상화와 예외 변환
> - [Externalized Configuration :: Spring Boot](https://docs.spring.io/spring-boot/reference/features/external-config.html): 설정 출처의 우선순위
> - 아직 안 본 것 — `JdbcTemplate`/`JdbcClient`, JPA·Hibernate·Spring Data JPA의 층위 구분

## 2. 코드 구현

### `mapRow` — 행 하나를 객체 하나로 되돌리는 자리

```java
private Reservation mapRow(ResultSet rs) throws SQLException {
    Reservation reservation = new Reservation(
            rs.getString("room_name"),
            rs.getString("requester_name"));
    reservation.assignId(rs.getLong("id"));
    if (rs.getBoolean("confirmed")) {
        reservation.confirm();
    }
    return reservation;
}
```

한 줄이면 될 것 같은 메서드가 세 단계로 나뉜 이유는 Day4의 설계 결정 때문이다. `Reservation`의 생성자는 `(roomName, requesterName)`만 받고 `confirmed`는 항상 `false`로 시작하며, 상태 변경은 `confirm()`으로만 하도록 캡슐화했다. 새 예약을 만드는 규칙으로는 옳지만, **DB에는 이미 확정된 행이 있다.** 만들고 → id를 심고 → true일 때만 따라잡는 우회로가 생긴 건 그래서다.

`if`가 필요한 것도 같은 이유다. `setConfirmed(boolean)`이 없으니 `false`인 행에는 할 일이 없고, `true`인 행만 `confirm()`으로 맞춰준다.

### 예측이 틀렸던 지점 — 취소했는데 예약이 하나 더 생김

`InMemoryReservationRepository`의 `store.add()` 한 줄이 JDBC에서는 SQL 작성·자원 개폐·파라미터 바인딩·키 회수·예외 변환·행 매핑으로 흩어졌고, 그중 **비즈니스 로직은 한 줄도 없다.** 그런데 늘어난 코드보다 더 문제였던 건 빠뜨린 코드였다.

```java
// JdbcReservationRepository.save() — 현재 구현
String sql = "INSERT INTO reservation (room_name, requester_name, confirmed) VALUES (?,?,?)";
```

취소 흐름은 `findById` → 객체의 `cancel()` → `save(reservation)`인데, `id`가 이미 있는 객체가 들어와도 이 코드는 새 행을 만든다. 그래서 만든 적 없는 3번 예약이 생겼고, 정작 1번 행의 `confirmed`는 여전히 `true`로 남았다. 취소가 반영되지 않은 것이다.

`InMemoryReservationRepository`에는 그 분기가 있었다.

```java
if (reservation.getId() == null) { store.add(reservation); }
else { /* 같은 id를 찾아 store.set(index, reservation) */ }
```

인터페이스는 이름·인자·반환타입만 강제하고 **의미는 강제하지 못한다.** 같은 `Reservation save(Reservation)`이라도 계약을 지키는지는 구현자 책임이고, 오늘 나는 그 절반을 흘렸다.

### 오늘 확인한 것

| 항목 | 방법 | 결과 |
|---|---|---|
| 저장·조회·취소 동작 | 자동 테스트 10개 | 전부 통과 (`BUILD SUCCESSFUL`) |
| 재시작 후 데이터 생존 | 수동 (앱 강제 종료 후 재기동) | 2번 예약 살아남음, 없는 99번은 404 |
| 중복 행의 실제 개수와 1번의 `confirmed` 값 | — | **미검증** (H2 콘솔 조회 안 함) |

```text
[재기동 후]
Current version of schema "PUBLIC": 1
Schema "PUBLIC" is up to date. No migration necessary.

POST /reservations/cancel/2  → minji님이B202 예약을 취소하셨습니다
POST /reservations/cancel/99 → {"error":"예약을 찾을 수 없습니다. (id: 99)"}
POST /reservations/cancel/3  → jinwoo님이A101 예약을 취소하셨습니다   ← 만든 적 없는 번호
```

Day4에 등록해둔 "프로세스를 재시작하면 데이터가 사라진다"는 부채는 이걸로 해소됐다. Flyway는 `V1`을 다시 실행하지 않고 장부만 확인하고 넘어갔다.

<!-- 게시 전: 해당 Day 커밋 permalink 추가 -->

## 3. 스스로 답한 질문

### Q1. 같은 인터페이스의 구현체가 둘이면 어느 쪽이 주입되나

**A1.** 처음에는 "나중 것이 이기지 않을까"라고 답했다. 실제로는 아무것도 주입되지 않고 기동이 실패했다.

```text
NoUniqueBeanDefinitionException: expected single matching bean but found 2:
inMemoryReservationRepository, jdbcReservationRepository
```

스프링은 생성자 주입에서 **타입으로 후보를 모은다.** 결과가 2개면 고를 근거가 없고, "나중 것" 같은 임의의 규칙을 만들지 않은 이유는 그렇게 골랐을 때의 사고가 조용하기 때문이다. `InMemory`가 선택됐다면 앱은 정상적으로 뜨고 예약도 되는 것처럼 보이지만 재시작마다 데이터가 사라진다. 모호하면 즉시 죽는 편이 낫다.

해결은 `InMemoryReservationRepository`에서 `@Repository`만 떼는 것으로 했다. 파일을 지우지 않은 건 `ReservationServiceTest`가 이 클래스를 스프링 없이 `new`로 조립해 쓰는 **테스트 대역**이기 때문이다. 실행할 땐 DB 구현, 테스트할 땐 메모리 구현 — Day4에 Repository를 인터페이스로 뽑은 목적이 여기서 값을 치렀다.

### Q2. `mapRow` 안에서 확정 여부를 `if (rs.next())`로 판정하면 왜 안 되나

**A2.** 처음에 `if(rs)`라고 썼다가 자바에 truthiness가 없다는 걸 확인하고 `if (rs.next())`로 고쳤는데, 이쪽이 더 위험한 오답이었다. 컴파일도 되고 예외도 나지 않기 때문이다.

`next()`는 판정만 하는 게 아니라 커서를 전진시킨다. `findAll`의 `while (rs.next())` 안에서 `mapRow`가 또 `next()`를 부르면 **행이 하나 걸러 하나씩 사라지고**, 확정 여부는 "다음 행이 존재하는가"라는 무관한 조건으로 결정된다. 조회 결과가 이상하다는 걸 한참 뒤에 발견하게 되는 종류의 버그다.

지금 알고 싶은 것은 "이 행의 `confirmed` 값"이므로 커서를 옮길 이유가 없다. `rs.getBoolean("confirmed")`로 값만 읽으면 된다.

### Q3. `application.yml`에 H2를 썼는데 왜 PostgreSQL 드라이버를 찾았나

**A3.** 설정이 `application.yml`에만 있다고 생각한 것이 틀렸다. OS 환경변수에 다른 프로젝트용 PostgreSQL 접속 정보와 `SPRING_PROFILES_ACTIVE=prod`가 사용자 계정 범위로 남아 있었고, **환경변수는 `application.yml`보다 우선순위가 높다.**

이 규칙 자체는 같은 jar를 환경만 바꿔 배포하기 위한 장치다. 문제는 그 장치에 의도치 않게 걸렸을 때다. 드라이버가 의존성에 없어 커넥션 생성 단계에서 막혔지만, 있었다면 `flyway.enabled: true`인 로컬 실행이 원격 DB에 마이그레이션을 실행했을 것이다.

재발 방지로 `test` 태스크에서 DataSource를 고정했다. 테스트는 실행하는 사람의 환경이 무엇이든 항상 같은 DB에서 돌아야 재현 가능하다.

```kotlin
tasks.withType<Test> {
    environment("SPRING_DATASOURCE_URL", "jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1")
    environment("SPRING_PROFILES_ACTIVE", "test")
}
```

## 4. 정리하며

오늘 가장 크게 바뀐 건 초록불에 대한 감각이다. 테스트 10개가 전부 통과한 상태에서 취소 기능이 망가져 있었다. 단위 테스트는 계약이 지켜진 `InMemory` 구현을 쓰고 있었고, HTTP 테스트의 취소 검증은 404 케이스만 보고 있어서 성공 후 행 개수를 아무도 세지 않았다. **초록불은 "버그가 없다"가 아니라 "지금 확인한 것들에는 문제가 없다"** 라는 뜻이다.

그리고 `save`라는 이름이 얼마나 많은 것을 감추고 있었는지도 알게 됐다. `ArrayList`를 쓸 때는 `add`와 `set`의 차이가 눈앞에 있었지만, JDBC로 옮기면서 INSERT만 쓰고도 "저장했다"고 생각했다. 이름이 계약을 설명해주지 않는다.

**아직 남은 것**은 둘이다. `save()`의 갱신 분기가 없어 취소·수정이 실행될 때마다 중복 행이 쌓이는 것 — 이건 **바로 고칠 것**이고 다음 Day의 첫 작업이다. 그리고 `bootRun`은 여전히 사용자 환경변수의 영향을 받는 상태다. 테스트만 격리해뒀으니 **나중에 고칠 것**(환경변수를 해당 프로젝트로 이전)으로 분류한다.

면접 질문으로 남겨둔다.

- Repository 구현을 갈아끼울 수 있게 인터페이스로 뽑았는데, 구현마다 `save()`의 의미가 달라질 수 있다면 그 계약은 어디에 문서화되고 무엇으로 검증되어야 하나?
- 커넥션을 반납하지 않는 코드는 부하가 낮을 때 정상으로 보인다. 이 결함을 배포 전에 드러내려면 무엇을 관찰해야 하나?
