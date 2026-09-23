# [백엔드 기본기 Day9] Relational Data Access 계층 — JDBC 직접 구현과 저장 계약

Day8에서 `V1__init.sql`로 `reservation` 테이블을 만들어두고도 애플리케이션은 여전히 `InMemoryReservationRepository`의 `ArrayList`에 예약을 담고 있었다. 오늘은 그 자리를 순수 JDBC 구현으로 교체했다. JPA는 쓰지 않았다. 다음 Day에 지워질 코드가 얼마나 되는지를 눈으로 보려면 먼저 손으로 써봐야 하기 때문이다.

> `DataSource`에서 커넥션을 빌려 `PreparedStatement`로 INSERT·SELECT를 실행하는 Repository를 만들고, 앱을 강제 종료했다 재기동해도 예약이 남아 있는 것을 확인했다. 테스트 10개가 전부 통과한 상태에서 만든 적 없는 3번 예약이 조회되는 것을 발견했고, 원인은 `save()`가 무조건 INSERT만 하도록 구현된 것이었다. 이 결함은 이 Day에서 수정하지 않았다.

## 1. 개념 설명

| 용어 | 한줄뜻 | 현재 프로젝트 적용 지점 |
|---|---|---|
| `DataSource` | 커넥션 풀을 감싼 표준 인터페이스. "연결 하나 빌려줘"의 창구 | `JdbcReservationRepository`의 생성자 주입 필드 |
| 커넥션 풀 | 미리 열어둔 DB 연결을 빌려주고 반납받는 저장소. Boot 기본은 HikariCP | 자동 구성 Bean |
| `Connection` | DB와의 세션 하나. TCP 연결과 인증이 끝난 상태 | `dataSource.getConnection()` |
| `PreparedStatement` | 구조가 먼저 굳은 SQL. 값은 `?` 자리에 따로 바인딩 | `ps.setLong(1, id)` — 1부터 시작 |
| `ResultSet` | 결과 행을 가리키는 **커서**. 행들을 담은 컬렉션이 아님 | `ps.executeQuery()` |
| `rs.next()` | 커서를 다음 행으로 **이동**하고, 행이 있으면 `true` | `findAll()`의 `while (rs.next())` |
| `rs.getXxx("컬럼")` | 지금 커서가 있는 행에서 값만 읽음. 이동하지 않음 | `mapRow()`의 `rs.getBoolean("confirmed")` |
| try-with-resources | 소괄호 안에서 연 자원을 블록 이탈 시 역순으로 `close()` | `save()`·`findById()`·`findAll()` |
| `RETURN_GENERATED_KEYS` | INSERT 후 DB가 붙인 `AUTO_INCREMENT` 값을 돌려받는 옵션 | `save()`의 `ps.getGeneratedKeys()` |
| `save()`의 계약 | "저장한다"가 아니라 "id가 없으면 신규, 있으면 갱신" | `InMemoryReservationRepository.save()`의 `getId() == null` 분기 |
| 설정 우선순위 | OS 환경변수가 `application.yml`보다 우선 | `SPRING_DATASOURCE_URL` |

JDBC는 저장 한 번을 `DataSource` → `Connection` → `PreparedStatement` → `ResultSet` 네 객체로 쪼갠다. 앞의 셋은 **빌리고 반납해야 하는 자원**이고, 마지막 하나는 **사고방식을 바꿔야 하는 커서**다.

### 영속 저장소와 JDBC 계층의 필요성

`InMemoryReservationRepository`는 JVM 힙의 `ArrayList`에 예약을 담았다. 프로세스가 끝나면 힙이 사라지고, 예약도 함께 사라진다. Day4에 기술부채로 남겨둔 문제가 이것이다.

Day8에 테이블은 만들었지만 자바 객체가 그 테이블까지 가는 길은 없었다. 자바 코드와 DB 사이에는 네트워크 연결, SQL 문자열, 결과 행을 객체로 바꾸는 과정이 끼어 있다. JDBC는 이 과정을 DB 종류와 무관한 표준 인터페이스로 정의하고, 실제 구현은 H2 같은 드라이버가 제공한다.

```text
ReservationService
→ ReservationRepository (인터페이스, Day4에 분리)
→ JdbcReservationRepository (오늘 추가)
→ JDBC 표준 인터페이스 (DataSource, Connection, ...)
→ H2 드라이버
→ data/studyroom.mv.db
```

Wikimedia Commons의 JDBC 구조도는 애플리케이션이 JDBC API와 DriverManager/DataSource만 거치고, DB별 차이는 각 JDBC 드라이버가 맡는 구조를 다음처럼 그린다(그림의 `Base de Données`는 프랑스어로 "데이터베이스").

![구조도. 맨 위 Java 애플리케이션이 JDBC API와 양방향으로 연결되고, 그 아래 JDBC Driver Manager 또는 DataSource 객체가 있다. 그 아래에 MariaDB, PostgreSQL, Oracle용 JDBC 드라이버 세 개가 각각 DriverManager/DataSource에 연결되고, 각 드라이버는 자기 데이터베이스와 양방향으로 통신한다.](../../../assets/day09-web-jdbc-api-driver.png)
<!-- velog 업로드: 이 줄 위 이미지 자리에 app/study_docs/assets/day09-web-jdbc-api-driver.png 파일을 드래그해 교체 -->

*출처: [File:DessinApiJDBC.svg — Wikimedia Commons](https://commons.wikimedia.org/wiki/File:DessinApiJDBC.svg) — Unareil, CC BY-SA 4.0 (PNG 렌더링·여백 자름)*

Service는 인터페이스만 알고 있으므로, 저장 기술을 바꿔도 Service 코드는 바뀌지 않는다. 오늘 바뀐 것은 이 사슬의 아래쪽 절반이다.

### `DataSource`와 Connection Pool

DB에 SQL을 보내려면 먼저 연결이 있어야 한다. 매번 새로 연결하면 TCP 연결과 인증을 반복하게 된다. 커밋 주석에는 이 비용을 "수십 ms짜리"라고 적었고, 그래서 미리 열어둔 커넥션을 빌렸다 반납하는 편이 싸다고 정리했다. 이 수치는 직접 측정하지 않았다.

Spring Boot는 jdbc 스타터와 드라이버가 classpath에 있으면 `application.yml`의 URL·계정으로 HikariCP 풀을 만들고 `DataSource` Bean으로 등록한다. `JdbcReservationRepository`는 이 Bean을 생성자로 주입받기만 한다.

```text
getConnection() → 풀에서 커넥션 하나 대여
SQL 실행
con.close()     → 연결을 끊는 것이 아니라 풀에 반납
```

풀의 커넥션 수는 유한하다. 반납하지 않은 커넥션이 쌓이면 풀이 마르고, 그 뒤에는 이 기능 하나가 아니라 DB를 쓰는 모든 요청이 커넥션을 기다린다. 이 누수 상황은 설명으로만 정리했고 실행해보지는 않았다(미검증).

### `PreparedStatement`와 Parameter Binding

`save()`의 SQL은 값 자리를 `?`로 비워둔다.

```text
"INSERT INTO reservation (room_name, requester_name, confirmed) VALUES (?,?,?)"
→ con.prepareStatement(sql)  : SQL 구조를 먼저 전달
→ ps.setString(1, roomName)  : 1번 자리에 값 바인딩 (0이 아니라 1부터)
→ ps.setString(2, requesterName)
→ ps.setBoolean(3, confirmed)
→ ps.executeUpdate()         : 영향받은 행 수 반환
```

`?`가 SQL Injection을 막는 이유는 값을 검사해서가 아니다. SQL의 구조를 먼저 굳혀두고 그다음에 값을 채우기 때문에, 값에 무엇이 들어오든 구조를 바꿀 수 없다. 문자열을 이어붙이면 값과 구조가 같은 문장 안에 섞이고, 그때부터 파서는 둘을 구분할 방법이 없다.

오늘은 바인딩 방식으로만 구현했고, 문자열 연결 방식으로 실제 공격 문자열을 넣어보는 대조군은 실행하지 않았다(미검증).

### `ResultSet` Cursor — `next()`와 `getXxx()`의 구분

`ResultSet`은 결과 전체를 담은 리스트가 아니라 한 행을 가리키는 위치다. 자료구조의 Iterator와 같은 구조다. Java 공식 문서도 커서가 처음에는 첫 행 **앞**에 있고, `next()`가 한 행씩 앞으로 이동하며, `false`를 반환하면 마지막 행 뒤에 있다고 설명한다.

```text
executeQuery() → 커서: 첫 행 앞
next() == true  → 커서: 1행      → getXxx()로 1행의 값 읽기
next() == true  → 커서: 2행      → getXxx()로 2행의 값 읽기
next() == false → 커서: 마지막 행 뒤 → 반복 종료
```

여기서 함정은 `next()`가 **이동과 판정을 동시에** 한다는 점이다. "행이 있는가"를 묻는 것처럼 보이지만, 묻는 순간 커서가 한 칸 움직인다. 반대로 `getXxx()`는 커서를 움직이지 않고 현재 행의 값만 읽는다.

![시퀀스 다이어그램. 참여자는 JdbcReservationRepository, DataSource(HikariCP 풀), Connection, PreparedStatement, ResultSet(커서)이다. Repository가 getConnection()으로 풀에서 con을 대여하고, prepareStatement(sql)로 ps를 받고, executeQuery()로 첫 행 앞에 놓인 rs를 받는다. rs.next()가 true인 동안 도는 loop 프레임 안에서 next()는 다음 행으로 이동하며 true를 돌려주고, mapRow(rs)가 getXxx("컬럼명")로 현재 행의 값을 이동 없이 읽는다. 마지막 next()는 마지막 행 뒤에서 false를 돌려준다. 끝으로 ResultSet, PreparedStatement, Connection 순으로 close()하며 Connection은 풀에 반납된다.](../../../assets/day09-jdbc-cursor.png)
<!-- velog 업로드: 이 줄 위 이미지 자리에 app/study_docs/assets/day09-jdbc-cursor.png 파일을 드래그해 교체 -->

`findAll()`의 `while (rs.next())`가 이미 커서를 한 행씩 옮기고 있다. 그 안에서 호출되는 `mapRow()`는 "이미 정해진 한 행을 객체로 바꾸는" 자리다. 여기서 `next()`를 또 부르면 커서가 한 칸 더 가서 행이 하나 걸러 하나씩 사라진다. 컴파일도 되고 예외도 나지 않는 종류의 버그다.

> **정리.** `next()`는 행을 넘길 때만, `getXxx()`는 값을 읽을 때만 쓴다.

### try-with-resources와 checked 예외

`Connection`, `PreparedStatement`, `ResultSet`은 셋 다 닫아야 하는 자원이다. try-with-resources는 소괄호 안에서 연 자원을 블록을 벗어날 때 예외 발생 여부와 무관하게 **연 순서의 역순으로** 닫는다. 로드맵도 try-with-resources를 기본으로 두고, `finally`로 직접 닫는 방식은 옛 반복 코드의 대조용으로만 다룬다.

JDBC 메서드는 `SQLException`을 던진다. `SQLException`은 checked 예외라서 잡거나 던진다고 선언하지 않으면 컴파일이 안 된다. `JdbcReservationRepository`는 이 예외를 잡아 `IllegalStateException`으로 바꿔 던지고, 원인 `e`를 두 번째 인자로 넘긴다.

```text
catch (SQLException e)
→ throw new IllegalStateException("예약 저장 실패", e)
→ 스택트레이스에 원래 SQLException이 cause로 남음
```

원인을 넘기지 않으면 스택트레이스가 거기서 잘려 실제 SQL 오류를 잃는다. 예외 로그와 응답 본문을 나누는 원칙(원인은 로그로, 사용자에게는 일반 문구로)도 원인이 보존돼 있어야 성립한다.

### `save()`의 저장 계약과 인터페이스의 한계

`ReservationRepository`는 `Reservation save(Reservation reservation)`이라는 시그니처만 선언한다. Day4의 `InMemoryReservationRepository`는 그 안에 두 갈래를 두고 있었다.

```text
save(reservation)
├ getId() == null → assignId(nextId++) → store.add()     : 신규
└ getId() != null → 같은 id를 찾아 store.set(index, ...) : 갱신
                    못 찾으면 IllegalArgumentException
```

오늘 쓴 `JdbcReservationRepository.save()`에는 첫 갈래만 있었다. 항상 INSERT를 실행하고 `RETURN_GENERATED_KEYS`로 DB가 붙인 id를 받아 `assignId()`로 심는다.

취소 흐름은 `ReservationService.cancel()`에서 `findById()` → `reservation.cancel()` → `save(reservation)` 순이다. `findById()`로 꺼낸 객체는 이미 id가 있는데도 JDBC 구현은 새 행을 만들었다. 그래서 만든 적 없는 3번 예약이 1번의 복사본으로 생겼고, 원래 1번 행의 `confirmed`는 `true`로 남아 취소가 반영되지 않았다.

인터페이스는 이름·인자·반환 타입만 강제하고 **의미는 강제하지 못한다.** 같은 시그니처라도 계약을 지키는지는 구현자 책임이다. 갱신 분기를 넣는다면 `UPDATE ... WHERE id = ?`의 `executeUpdate()` 반환값이 0일 때 그 id가 DB에 없다는 뜻이 되고, InMemory와 같은 예외 계약을 맞출 수 있다. 이 분기는 설계만 하고 코드에 넣지 않았다.

> **정리.** `save()`는 "저장한다"가 아니라 "id가 없으면 신규, 있으면 갱신"이라는 계약이다. 인터페이스는 이 의미를 강제하지 못하므로 구현과 테스트가 지켜야 한다.

### Bean 후보 모호성과 설정 우선순위

`JdbcReservationRepository`에 `@Repository`를 붙이자 같은 인터페이스의 Bean 후보가 둘이 됐다. Spring은 생성자 주입에서 **타입으로 후보를 모으고**, 후보가 둘이면 임의로 고르지 않고 기동을 실패시킨다. 조용히 잘못 고른 앱보다 즉시 멈춘 앱이 원인을 찾기 쉽다는 fail-fast 원칙이다.

설정에도 우선순위가 있다. Spring Boot는 여러 출처의 설정을 겹쳐 읽고, 대략 커맨드라인 > OS 환경변수 > `application-{profile}.yml` > `application.yml` 순서로 앞의 값이 이긴다. 환경변수 `SPRING_DATASOURCE_URL`은 대문자와 `_`가 소문자와 `.`로 바뀌어 `spring.datasource.url`에 대응한다.

| 구분 | 설정 위치 | 오늘의 영향 |
|---|---|---|
| `application.yml` | 저장소 안 | H2 파일 DB URL |
| OS 환경변수 | 사용자 계정 | 다른 프로젝트의 PostgreSQL URL이 yml을 덮음 |
| `test` 태스크 `environment(...)` | `build.gradle.kts` | 테스트 JVM의 URL을 H2 메모리 DB로 고정 |

이 규칙은 같은 jar를 환경만 바꿔 배포하기 위한 장치다. 오늘은 그 장치에 의도치 않게 걸렸고, 구체적인 경위는 3절 Q3에 적었다.

### Unit Test와 Integration Test의 검증 범위

`ReservationServiceTest`는 Spring 없이 `new InMemoryReservationRepository()`로 조립하는 단위 테스트다. DB가 없고 즉시 끝난다. `StudyRoomApiApplicationTests`와 `ReservationControllerHttpTest`는 `@SpringBootTest`로 컨테이너를 띄우는 통합 테스트라 Bean 스캔과 DB 연결이 일어난다.

| 테스트 | 사용한 저장소 | 오늘 결함을 볼 수 있었는지 |
|---|---|---|
| `ReservationServiceTest` | InMemory — 갱신 분기 있음 | 볼 수 없음 |
| `ReservationControllerHttpTest`의 취소 | JDBC | 404·400 같은 실패 케이스만 검사해 볼 수 없음 |

테스트 10개가 전부 통과했지만, JDBC 구현에서 성공한 취소 뒤에 행 개수를 세는 테스트는 없었다. 초록불은 "버그가 없다"가 아니라 "지금 확인한 것들에는 문제가 없다"는 뜻이다.

### JDBC 반복 코드와 JPA 도입 배경

`InMemoryReservationRepository`의 `store.add(reservation)` 한 줄이 JDBC에서는 다음 일로 흩어졌다. 줄 수는 코드를 보고 센 대략치다.

| 하는 일 | 줄 수(대략) |
|---|---|
| SQL 문자열 작성 | 1 |
| 커넥션·statement 열고 닫기 | 3 |
| 파라미터 번호 맞춰 바인딩 | 3 |
| 생성된 키 되받기 | 5 |
| 예외 변환 | 3 |
| 행→객체 매핑(`mapRow`) | 8 |

늘어난 것 중 비즈니스 로직은 한 줄도 없다. 전부 "자바 객체와 SQL 행 사이의 왕복"이다. 로드맵이 강조하는 것처럼 JPA는 커넥션 풀을 없애는 것이 아니라, 이 직접 관리와 매핑을 추상화한다. 다음 Day에 이 코드가 얼마나 지워지는지가 "JPA 도입 이유"의 근거가 된다.

> **더 볼 것**
> - [Data Access with JDBC :: Spring Framework](https://docs.spring.io/spring-framework/reference/data-access/jdbc.html): `DataSource` 추상화와 예외 변환
> - [ResultSet (Java SE 21 & JDK 21)](https://docs.oracle.com/en/java/javase/21/docs/api/java.sql/java/sql/ResultSet.html): 커서의 초기 위치와 `next()`의 이동 규칙
> - [Externalized Configuration :: Spring Boot](https://docs.spring.io/spring-boot/reference/features/external-config.html): 설정 출처의 우선순위
> - 아직 안 본 것 — `JdbcTemplate`/`JdbcClient`, JPA·Hibernate·Spring Data JPA의 층위 구분

## 2. 코드 구현

### `save()` — INSERT만 있는 저장

```java
String sql = "INSERT INTO reservation (room_name, requester_name, confirmed) VALUES (?,?,?)";
try (Connection con = dataSource.getConnection();
     PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
    ps.setString(1, reservation.getRoomName());
    ps.setString(2, reservation.getRequesterName());
    ps.setBoolean(3, reservation.isConfirmed());
    ps.executeUpdate();
    try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next()) { reservation.assignId(keys.getLong(1)); }
    }
    return reservation;
} catch (SQLException e) {
    throw new IllegalStateException("예약 저장 실패", e);
}
```

자원 대여·바인딩·키 회수·예외 변환은 모두 들어갔지만 `getId() != null`인 객체를 위한 갈래가 없다. 취소 흐름의 복제 행은 이 코드에서 나왔다. 오늘 과제로 UPDATE 분기를 냈지만 커밋에는 반영되지 않았다(`grep UPDATE` 결과 0건).

### `mapRow` — 캡슐화된 생성자를 거치는 복원

```java
private Reservation mapRow(ResultSet rs) throws SQLException {
    Reservation reservation = new Reservation(rs.getString("room_name"), rs.getString("requester_name"));
    reservation.assignId(rs.getLong("id"));
    if (rs.getBoolean("confirmed")) {
        reservation.confirm();
    }
    return reservation;
}
```

한 줄이면 될 것 같은 메서드가 세 단계로 나뉜 이유는 Day4의 설계다. `Reservation`의 생성자는 `(roomName, requesterName)`만 받고 `confirmed`는 항상 `false`로 시작하며, 상태 변경은 `confirm()`으로만 하도록 캡슐화했다. 새 예약을 만드는 규칙으로는 옳지만 **DB에는 이미 확정된 행이 있다.** 만들고 → id를 심고 → true일 때만 `confirm()`으로 따라잡는 우회로가 생긴 이유다.

### 컴파일 오류의 단계별 보고

세미콜론을 빠뜨린 상태로 빌드하자 `1 error`만 보고됐다. 고치자 새 오류 3개가 나타났다. 컴파일러는 먼저 글자를 문법 구조로 파싱하고, 그다음 이름과 타입을 대조하는 의미 분석을 한다. 파싱에서 멈추면 의미 분석은 실행되지 않으므로 오류 개수도 증상일 뿐이다.

새 오류 중 2개는 `public JdbcReservation Repository(...)`처럼 클래스명 중간에 들어간 공백 하나에서 나왔다. 문법상 생성자가 아니라 반환 타입 `JdbcReservation`과 메서드 `Repository`로 해석됐고, 그 메서드 안에서 `final` 필드에 대입하자 `cannot assign a value to final variable`이 따라왔다. Day8에서 `final`이 DB 값은 못 막는 것을 봤다면, 이번에는 컴파일러가 사정권 안의 재대입을 막는 모습을 실물로 봤다.

### 자동 검증 결과

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

Day4에 등록한 "프로세스를 재시작하면 데이터가 사라진다"는 부채는 이걸로 해소됐다. Flyway는 `V1`을 다시 실행하지 않고 장부만 확인하고 넘어갔다. 커밋: [293d260](https://github.com/enderpawar/8week_Spring_Study/commit/293d260b289c77ae2555635df54bade7c929054a)

## 3. 스스로 답한 질문

### Q1. 같은 인터페이스 구현체 두 개의 주입 결과

**질문.** `@Repository`가 붙은 `ReservationRepository` 구현체가 둘이면 어느 쪽이 주입되는가?

**A1.** 처음에는 "나중 것이 이기지 않을까"라고 답했다. 실제로는 아무것도 주입되지 않고 기동이 실패했다.

```text
NoUniqueBeanDefinitionException: expected single matching bean but found 2:
inMemoryReservationRepository, jdbcReservationRepository

10 tests completed, 6 failed
```

"나중 것" 같은 임의의 규칙이 없는 이유는 그렇게 골랐을 때의 사고가 조용하기 때문이다. `InMemory`가 선택됐다면 앱은 정상적으로 뜨고 예약도 되는 것처럼 보이지만 재시작마다 데이터가 사라진다. Repository와 무관한 `reservationServiceBeanIsSingleton`까지 6개가 함께 죽은 것은 Day8 체크섬 실험과 같은 모양이다. 컨텍스트가 못 뜨면 그 위의 모든 테스트가 같이 죽는다.

해결은 `InMemoryReservationRepository`에서 `@Repository`만 떼는 것으로 했다. 파일을 지우지 않은 건 `ReservationServiceTest`가 이 클래스를 Spring 없이 `new`로 조립해 쓰는 **테스트 대역**이기 때문이다. 실행할 땐 DB 구현, 테스트할 땐 메모리 구현. Day4에 Repository를 인터페이스로 뽑은 목적이 여기서 값을 치렀다.

### Q2. `mapRow` 안의 확정 여부 판정

**질문.** `mapRow` 안에서 확정 여부를 `if (rs.next())`로 판정하면 어떤 문제가 생기는가?

**A2.** 처음에 `if(rs)`라고 썼다가 자바에 truthiness가 없다는 걸 확인하고 `if (rs.next())`로 고쳤는데, 이쪽이 더 위험한 오답이었다. 컴파일도 되고 예외도 나지 않기 때문이다.

`next()`는 판정만 하는 게 아니라 커서를 전진시킨다. `findAll`의 `while (rs.next())` 안에서 `mapRow`가 또 `next()`를 부르면 행이 하나 걸러 하나씩 사라지고, 확정 여부는 "다음 행이 존재하는가"라는 무관한 조건으로 결정된다.

지금 알고 싶은 것은 "이 행의 `confirmed` 값"이므로 커서를 옮길 이유가 없다. `rs.getBoolean("confirmed")`로 값만 읽는 것으로 고쳤다.

### Q3. `application.yml`의 H2 설정과 PostgreSQL 드라이버 요구

**질문.** `application.yml`에 H2를 썼는데 왜 `Failed to load driver class org.postgresql.Driver`가 났는가?

**A3.** 설정이 `application.yml`에만 있다고 생각한 것이 틀렸다. OS 환경변수에 다른 프로젝트용 PostgreSQL 접속 정보와 `SPRING_PROFILES_ACTIVE=prod`가 사용자 계정 범위로 남아 있었고, 환경변수는 `application.yml`보다 우선순위가 높다.

드라이버가 의존성에 없어 커넥션 생성 단계에서 막혔지만, 있었다면 `flyway.enabled: true`인 로컬 실행이 원격 DB에 `V1__init.sql`을 실행했을 것이다. 재발 방지로 `test` 태스크에서 DataSource를 고정했다. 테스트는 실행하는 사람의 환경이 무엇이든 항상 같은 DB에서 돌아야 재현 가능하다.

```kotlin
environment("SPRING_DATASOURCE_URL", "jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1")
environment("SPRING_DATASOURCE_USERNAME", "sa")
environment("SPRING_DATASOURCE_PASSWORD", "")
environment("SPRING_PROFILES_ACTIVE", "test")
```

## 4. 학습 정리와 다음 범위

오늘 가장 크게 바뀐 건 초록불에 대한 감각이다. 테스트 10개가 전부 통과한 상태에서 취소 기능이 망가져 있었다. 단위 테스트는 계약이 지켜진 `InMemory` 구현을 쓰고 있었고, HTTP 테스트의 취소 검증은 404 케이스만 보고 있었다.

그리고 `save`라는 이름이 얼마나 많은 것을 감추고 있었는지도 알게 됐다. `ArrayList`를 쓸 때는 `add`와 `set`의 차이가 눈앞에 있었지만, JDBC로 옮기면서 INSERT만 쓰고도 "저장했다"고 생각했다. 이름이 계약을 설명해주지 않는다.

**아직 남은 것**은 둘이다. `save()`의 갱신 분기가 없어 취소가 실행될 때마다 중복 행이 쌓이는 것은 이 시점에 **바로 고칠 것**으로 분류했다. 이후 Day10에서 JDBC에는 UPDATE 분기를 쓰지 않고 Spring Data `save()`로 대체했으며, `JdbcReservationRepository`의 중복 행 결함은 대조군으로 그대로 남겼다. `bootRun`이 사용자 환경변수의 영향을 받는 문제는 테스트만 격리해뒀으므로 **나중에 고칠 것**(환경변수를 해당 프로젝트로 이전)으로 분류한다.

면접에서 다시 답해볼 항목을 남긴다.

- 구현마다 `save()`의 의미가 달라질 수 있을 때 그 계약의 문서화 위치와 검증 수단
- 부하가 낮을 때 정상으로 보이는 커넥션 미반납 결함을 배포 전에 드러내는 관찰 지점

---

오늘 공부한 소스코드: `app/src/main/java/com/example/studyroom/repository/JdbcReservationRepository.java`, `app/src/main/java/com/example/studyroom/repository/InMemoryReservationRepository.java`, `app/build.gradle.kts`
