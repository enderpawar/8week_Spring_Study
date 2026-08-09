# [백엔드 기본기 Day10] 객체-관계 매핑 — Entity 매핑과 Spring Data JPA 기본 CRUD

Day9에 만든 `JdbcReservationRepository`는 `save()`에 INSERT만 있어서, 취소 흐름에서 기존 예약을 저장하면 새 행이 하나 더 생겼다. 오늘은 그 자리를 Spring Data JPA 구현으로 바꾸고 같은 흐름을 다시 검증했다. 영속성 컨텍스트와 flush 시점은 다음 Day의 주제라 여기서는 다루지 않고, Entity 매핑과 기본 CRUD까지만 본다.

> `Reservation`에 매핑 애노테이션을 붙이고, 기존 `ReservationRepository`를 구현하는 어댑터를 통해 Spring Data에 위임했다. Service와 Controller는 고치지 않았다. 통합 테스트 2개로 신규 저장·단건 조회와 기존 ID 갱신을 확인했고, 기존 ID를 저장했을 때 행이 늘지 않고 `UPDATE`가 나가는 것까지 봤다. 다만 실제 취소 요청이 몇 번의 SQL을 내는지는 측정하지 못했다.

## 1. 개념 설명

| 용어 | 한줄뜻 | 코드 모습 |
|---|---|---|
| JPA | 자바 객체와 관계형 DB를 매핑하는 명세 | `jakarta.persistence.*` |
| Hibernate | 그 명세를 실제 SQL로 실행하는 구현체 | 로그의 `insert`·`select`·`update` |
| Spring Data JPA | Repository 구현을 런타임에 만들어주는 편의층 | `JpaRepository<Reservation, Long>` |
| `@Entity` | 이 클래스를 JPA가 관리할 영속 객체로 표시 | `Reservation` 클래스 위 |
| `@Id` + `@GeneratedValue(IDENTITY)` | 식별자 생성을 DB의 자동 증가 컬럼에 맡김 | `private Long id` |
| 기본 생성자 | 조회한 행으로 객체를 만들 때 쓰는 진입점 | `protected Reservation()` |

앞의 세 이름은 같은 층이 아니다. JPA는 규칙만 정하고 실행하지 않으며, 그 규칙대로 SQL을 만들어 보내는 건 Hibernate다. Spring Data JPA는 그 위에서 Repository 구현을 대신 만들어준다. JDBC 표준 인터페이스와 H2 드라이버의 관계에 편의층이 하나 더 얹힌 구조다.

그래서 오늘 추가한 파일 중 하나는 본문이 비어 있다.

![클래스 다이어그램. ReservationService가 «interface» ReservationRepository를 생성자 주입으로 참조하고, «@Repository» JpaReservationRepository가 그 인터페이스를 «realize»한다. JpaReservationRepository는 delegate 필드로 «interface» SpringDataReservationRepository를 주입받고, 그 인터페이스는 오퍼레이션 칸이 비어 있는 채로 JpaRepository<Reservation, Long>를 상속한다. 왼쪽 아래의 «@Entity» Reservation은 roomName·requesterName·confirmed·id가 모두 private이고 id에 «@Id, IDENTITY»가 붙어 있으며, 생성자 Reservation()은 protected다. 노트는 Spring Data가 기동 시 이 인터페이스를 찾아 구현 객체를 만들어 Bean으로 등록한다는 것과 기동 로그의 "Found 1 JPA repository interface."를 가리킨다.](../../assets/day10-jpa-adapter.png)

`SpringDataReservationRepository`에는 `save`도 `findById`도 없는데 테스트가 그 셋을 다 호출한다. 구현을 소스에서 찾으려고 하면 못 찾는다. 기동 로그에 `Found 1 JPA repository interface.`가 찍히는데, Spring Data가 시작할 때 인터페이스를 스캔해 구현 객체를 만들고 Bean으로 등록하기 때문이다.

스키마는 이 층 분담에서 빠져 있다. `ddl-auto: none`으로 두어 Hibernate가 테이블을 만들지 못하게 막았고, 주인은 Day8에 만든 Flyway `V1__init.sql`이다. 통합 테스트 로그에서도 Flyway가 version 1을 적용한 **다음에** Hibernate가 초기화된다. 둘 다 스키마를 바꾸면 Flyway 장부와 실제 DB가 어긋나기 때문에, 지금 Hibernate의 몫은 이미 있는 테이블과 Entity 사이의 데이터 왕복뿐이다.

> **더 볼 것**
> - [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/reference/): 비어 있는 인터페이스가 구현으로 바뀌는 지점
> - [Hibernate ORM User Guide](https://docs.jboss.org/hibernate/orm/6.6/userguide/html_single/Hibernate_User_Guide.html): 매핑 애노테이션이 SQL로 번역되는 규칙
> - 아직 안 본 것 — 영속성 컨텍스트, 1차 캐시, 변경 감지

## 2. 코드 구현

### `Reservation` — 애노테이션 세 개가 만든 INSERT

```java
@Entity
public class Reservation {
    private String roomName;
    private String requesterName;
    private boolean confirmed;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    protected Reservation() {
    }
}
```

이 상태로 저장하니 Hibernate가 이런 SQL을 보냈다.

```sql
insert into reservation (confirmed, requester_name, room_name, id) values (?, ?, ?, default)
```

`id` 자리에 값이 아니라 `default`가 들어간다. `IDENTITY`가 "번호는 DB가 정한다"는 뜻이라 값을 보내지 않는 것이고, 그 번호를 만드는 건 `V1__init.sql`에 써둔 `AUTO_INCREMENT`다. 컬럼 순서가 소스의 필드 순서와 다르고 `requesterName`이 `requester_name`으로 바뀐 것도 내가 한 일이 아니다.

`@Id`를 getter가 아니라 필드에 붙였기 때문에 Hibernate는 필드를 기준으로 매핑을 읽는다. 조회할 때도 `protected` 기본 생성자로 빈 객체를 만든 뒤 필드에 값을 직접 넣는다. Day9에 행 하나를 객체로 되돌리려고 `new` → `assignId()` → `confirm()` 세 단계를 거치던 `mapRow`가 통째로 사라진 이유가 이거다. 대신 `confirm()`·`cancel()`로만 상태를 바꾸게 해둔 규칙을 Hibernate는 지나가지 않는다는 뜻이기도 하다.

### 어댑터 — 구현만 갈아끼우고 Service는 두기

```java
@Repository
public class JpaReservationRepository implements ReservationRepository {
    private final SpringDataReservationRepository delegate;

    @Override
    public Reservation save(Reservation reservation) {
        return delegate.save(reservation);
    }
    // findById, findAll도 한 줄 위임
}
```

Spring Data 인터페이스를 Service에 바로 주입할 수도 있었지만 그러면 Service가 `JpaRepository`를 알게 된다. 어댑터를 하나 두는 값으로 Day4에 정한 경계를 유지했고, 실제로 `ReservationService`와 `ReservationController`는 이번 Day에 한 글자도 바뀌지 않았다.

대신 `JdbcReservationRepository`에서 `@Repository`를 뗐다. 같은 타입의 Bean이 둘이면 생성자 주입이 고를 근거가 없어 기동이 실패하는데, Day9에 `NoUniqueBeanDefinitionException`으로 이미 겪은 적이 있다. JDBC 코드는 대조군으로 파일에는 남겼지만 실행 경로에서는 빠졌다.

### 오늘 확인한 것

| 확인한 것 | 방법 | 결과 |
|---|---|---|
| 신규 저장과 단건 조회 | 자동 — `JpaReservationRepositoryTest` | `id` 생성, `insert` 뒤 `select ... where r1_0.id=?` |
| 기존 ID 저장 시 중복 행 | 자동 — 저장 전후 행 수와 같은 `id` 행 개수 비교 | 행 수 +1, 같은 `id` 한 행, `confirmed=false` |
| 실제 취소 요청의 SQL 횟수 | **미검증** — 테스트 안에서만 관찰했다 | 4절 |

전체 `./gradlew clean test`는 12개 통과한다. 코드는 [ff795f9](https://github.com/enderpawar/8week_Spring_Study/commit/ff795f9214a74ab37391c80f4a2bc6216c3d6e2a)에 있다.

## 3. 스스로 답한 질문

### Q1. `final` 필드를 둔 채 빈 기본 생성자를 추가하면 컴파일되는가

**A1.** 처음에는 "된다"라고 답했다. 두 문자열 필드가 기존 생성자에서 초기화되고 있으니 문제될 게 없다고 봤는데, 컴파일이 안 됐다.

```text
Reservation.java:13: error: variable roomName might not have been initialized
```

`final` 인스턴스 필드는 생성자 하나가 아니라 **모든 생성자 경로에서** 초기화되어야 한다. 인자 없는 생성자에는 방 이름과 신청자 이름을 넣을 방법이 없으니 그 경로가 비고, 컴파일러가 거기서 막는다. 내가 본 건 기존 생성자 하나였고 컴파일러가 본 건 경로 전체였다.

두 필드의 `final`을 떼고 기본 생성자를 `protected`로 뒀다. Hibernate에는 객체를 만들 진입점을 주면서, 애플리케이션 코드가 `new Reservation()`으로 방 이름도 신청자도 없는 예약을 아무 데서나 만들지는 못하게 하는 선택이다.

이번 주에 `final`로 틀린 게 처음이 아니다. Day8에도 필드가 `final`이면 DB 값이 안 바뀔 거라고 봤다가 `UPDATE`가 그냥 실행되는 걸 봤다. `final`은 자바 변수의 재대입을 컴파일 시점에 막는 장치일 뿐, 그 바깥에는 권한이 없다.

### Q2. `@Transactional`을 붙였는데 왜 전체 실행에서만 테스트가 깨졌나

**A2.** 단독으로 돌릴 때는 통과하던 테스트가 `clean test`에서 실패했다. 처음 쓴 단언이 `assertEquals(1, reservations.size())`였는데, 내 테스트가 한 건 저장했으니 전체가 한 건이라고 본 것이다.

원인은 다른 테스트가 남긴 행이었다. `ReservationControllerHttpTest`의 예약 성공 테스트에는 `@Transactional`이 없어서 `POST /reservations`로 만든 예약이 커밋된다. 테스트용 DB는 `build.gradle.kts`에 `jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1`로 고정해둔 상태라 JVM이 살아 있는 동안 유지되고, 그 행이 내 `findAll()`에 같이 잡혔다.

여기서 깨진 건 `@Transactional`에 대한 오해다. 롤백되는 것은 그 테스트가 쓴 것뿐이고, 남이 이미 커밋한 행은 지워주지 않는다. 절대값 단언은 사실 실행 순서에 기대고 있었다.

```java
int countBeforeSave = repository.findAll().size();
assertEquals(countBeforeSave + 1, reservations.size());
```

`+1`은 앞에 몇 건이 있든 성립한다. 애초에 확인하려던 것도 "전체가 한 건"이 아니라 "두 번 저장했는데 행은 한 개만 늘었다"였으니, 단언이 의도에 맞게 좁혀진 셈이다.

## 4. 예측은 맞았는데 로그가 달랐던 지점

기존 예약을 조회해 `cancel()`하고 저장하면 어떤 SQL이 나가는지도 미리 답했다. 내 답은 `SELECT` 1개와 `UPDATE` 1개였고 판정은 정답이었다. 실제 로그에도 `UPDATE`가 있었다.

```sql
update reservation set confirmed=?, requester_name=?, room_name=? where id=?
```

그런데 이 `update` 앞에 `SELECT`가 없었다. 내가 예측한 SELECT는 `findById`가 낼 것으로 본 것인데, 테스트는 방금 저장한 객체를 그대로 다시 저장하므로 조회 단계가 없다. 답은 맞았지만 근거가 되는 실행 경로는 내가 생각한 것과 달랐다.

실제 취소 흐름은 `findById` → `cancel()` → `save()`이고, `ReservationService`에는 `@Transactional`이 없다. 테스트 클래스에는 붙어 있으니 두 경우의 조건이 같지 않은데, 이 차이가 SQL 횟수를 바꾸는지는 오늘 측정하지 않았다. 같은 이유로 테스트에서 `flush()`를 명시했다. `save()`를 호출한 줄과 SQL이 실제로 나가는 시점이 왜 다를 수 있는지 설명할 수 없어서, 검증 지점을 고정하려고 강제로 밀어낸 것이다.

두 항목 다 D4·D5에서 예측을 먼저 적고 관찰한다. `IDENTITY`는 DB가 INSERT를 실행해야 번호를 알 수 있어 INSERT만은 미룰 수 없다는 데까지 확인했으니, 거기서 시작하면 될 것 같다.

## 5. 정리하며

JPA로 바꾼 결과를 "코드가 줄었다"로 정리하려다 말았다. 줄어든 건 SQL 작성과 행-객체 변환 같은 JDBC 반복 코드이고, `insert`·`select`·`update`는 로그에 그대로 찍힌다. DB 작업이 사라진 게 아니라 그 일을 하는 주체가 Hibernate로 바뀐 것이다.

테스트에 대한 생각도 한 번 더 고쳐졌다. Day9에는 초록불 열 개를 켜놓고 중복 행 버그를 안고 있었고, 오늘은 통과하던 테스트가 실행 순서 때문에 깨졌다. 무엇을 검사하느냐만큼 그 검사가 무엇을 가정하느냐도 봐야 했다.

**아직 남은 것**은 두 가지다. `JdbcReservationRepository`의 중복 행 결함은 코드에 그대로 있지만 Bean 후보에서 빠져 실행되지 않으므로 **고치지 않을 것**(대조군 보존)으로 둔다. `ddl-auto`는 아직 `none`이라 Entity와 스키마가 어긋나도 기동에서 걸리지 않는데, 이건 **나중에 고칠 것**으로 Week B D7에 `validate` 전환을 배정했다.

면접에서 받으면 다시 정리해봐야 할 질문을 남긴다.

- Repository 구현을 JDBC에서 JPA로 바꿨는데 Service가 수정되지 않은 이유를 의존성 방향으로 설명할 수 있는가
- `@Transactional`이 붙은 테스트에서도 격리가 깨지는 조건은 무엇이고, 그 결함은 어떤 실행 순서에서 드러나는가

---

오늘 공부한 소스코드: [8week_Spring_Study/app](https://github.com/enderpawar/8week_Spring_Study/tree/master/app)
