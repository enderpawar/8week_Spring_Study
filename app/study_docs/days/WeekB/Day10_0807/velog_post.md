# [백엔드 기본기 Day 10] 객체-관계 매핑 — Entity와 Spring Data JPA 기본 CRUD

Day9에는 JDBC로 예약 저장소를 직접 구현했다. `INSERT` 하나를 위해 SQL 문자열, 커넥션, `PreparedStatement`, 파라미터 바인딩, 생성 키 회수, 예외 변환을 작성했고 조회에는 `ResultSet`과 `mapRow`가 더 필요했다. 그 과정에서 기존 예약도 무조건 INSERT하는 결함까지 만들었다.

오늘의 중심 질문은 하나다.

> Spring Data JPA로 바꾸면 반복 코드는 어디로 사라지고, 기존 Repository의 저장 계약은 실제로 유지되는가?

`Reservation`을 Entity로 매핑하고, 기존 `ReservationRepository`를 구현하는 JPA 어댑터를 만들었다. Service와 Controller는 수정하지 않았다. 통합 테스트에서는 신규 예약이 저장·조회되고, 기존 ID를 다시 저장하면 새 행이 생기는 대신 같은 행이 UPDATE되는 것을 확인했다.

## 1. 개념 설명

### JPA, Hibernate, Spring Data JPA는 같은 이름이 아니다

세 이름을 한 덩어리로 부르면 코드가 동작하는 주체를 설명하기 어렵다.

| 층 | 역할 | 오늘 확인한 모습 |
|---|---|---|
| JPA | 자바 객체와 관계형 DB를 매핑하는 명세 | `jakarta.persistence` 애노테이션 |
| Hibernate | JPA 명세를 구현해 SQL을 실행하는 도구 | 로그의 `insert`, `select`, `update` |
| Spring Data JPA | Repository 인터페이스 구현을 만들어주는 편의층 | 빈 `JpaRepository` 하위 인터페이스 |

JPA는 규칙이고 Hibernate는 그 규칙을 실행한다. Spring Data JPA는 Hibernate 위에서 Repository 반복 구현을 줄인다. JDBC 표준 인터페이스와 실제 JDBC 드라이버가 구분되는 것과 비슷한 층위다.

```java
public interface SpringDataReservationRepository
        extends JpaRepository<Reservation, Long> {
}
```

이 인터페이스에는 메서드 본문이 없다. Spring Data가 애플리케이션 시작 시 인터페이스를 찾고 구현 객체를 만들어 Bean으로 등록한다. 이번 테스트의 기동 로그에는 JPA Repository 인터페이스 1개가 발견됐다고 기록됐고, 그 구현을 통해 `save`, `findById`, `findAll`이 실제 SQL을 실행했다.

### Entity는 테이블의 복사본이 아니라 식별자를 가진 객체다

`Reservation`에는 네 가지 매핑을 추가했다.

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

`@Entity`는 이 클래스를 JPA가 관리할 대상으로 표시한다. `@Id`는 두 객체와 두 DB 행을 구분하는 식별자를 지정한다. 애노테이션이 필드에 있으므로 Hibernate는 필드 접근 방식으로 매핑 정보를 읽는다.

`IDENTITY`는 ID 생성을 DB의 자동 증가 컬럼에 맡긴다는 뜻이다. 새 Entity의 `id`는 저장 전에는 `null`이고, INSERT 뒤 DB가 만든 값이 객체에 들어온다. Day8의 `AUTO_INCREMENT`와 연결되는 지점이다.

기본 생성자는 조회한 행을 객체로 복원할 때 JPA 구현체가 사용할 진입점이다. `protected`로 둔 이유는 JPA에는 열어주되 애플리케이션 코드가 방 이름과 신청자 이름도 없는 예약을 자유롭게 만들지 못하게 하기 위해서다.

### Flyway와 Hibernate에 스키마를 동시에 맡기지 않았다

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: none
```

Day8부터 스키마 변경 이력의 주인은 Flyway다. `V1__init.sql`과 체크섬 장부가 이미 있는데 Hibernate도 테이블을 만들고 고치게 두면 스키마의 주인이 둘이 된다. 그래서 Hibernate의 DDL 변경은 `none`으로 막았다.

통합 테스트에서도 Flyway가 빈 H2 스키마에 V1을 적용해 version 1을 만든 뒤 Hibernate가 초기화됐다. 이번 단계에서 Hibernate는 테이블을 만들지 않고, 이미 존재하는 테이블과 Entity 사이의 데이터 왕복만 담당했다. Week B 마지막에는 `validate`로 올려 변경 없이 매핑 일치 여부를 검사할 예정이다.

## 2. 코드 구현

### 기존 애플리케이션과 Spring Data 사이에 어댑터를 뒀다

Service는 Day4부터 `ReservationRepository` 인터페이스에 의존한다. 이 경계를 유지하기 위해 Spring Data 인터페이스를 Service에 직접 주입하지 않고 어댑터로 감쌌다.

```java
@Repository
public class JpaReservationRepository implements ReservationRepository {
    private final SpringDataReservationRepository delegate;

    @Override
    public Reservation save(Reservation reservation) {
        return delegate.save(reservation);
    }

    // findById, findAll도 같은 방식으로 위임
}
```

변경의 범위가 데이터 접근 계층 안에서 멈췄다. `ReservationService`와 `ReservationController`는 JDBC가 사라지고 JPA가 들어왔다는 사실을 모른다. 의존성 역전이 단순히 인터페이스를 만드는 형식이 아니라 변경의 전파 범위를 제한하는 장치라는 것을 확인했다.

기존 `JdbcReservationRepository`에서는 `@Repository`를 제거했다. 두 구현체를 모두 Bean으로 등록하면 생성자 주입 후보가 둘이 되어 애플리케이션이 기동하지 않기 때문이다. JDBC 코드는 JPA와 비교할 대조군으로 남겼지만 실행 경로에서는 제외했다.

### 예측과 달랐던 지점 — 빈 기본 생성자와 `final`

두 문자열 필드가 `final`인 상태에서 빈 기본 생성자를 추가해도 컴파일될 것이라고 예측했다. 하지만 `final` 인스턴스 필드는 모든 생성자 경로에서 초기화되어야 한다. 인자 없는 생성자에는 두 값을 넣을 방법이 없었다.

완성 코드에서는 두 필드의 `final`을 제거하고, 도메인용 생성자와 JPA용 기본 생성자를 함께 유지했다. 이 경험은 `final`을 "객체 전체가 불변이라는 표시"로 이해하면 안 되는 이유를 다시 보여줬다. `final`이 직접 막는 것은 해당 자바 변수의 재대입이다.

### 맞았던 예측 — 기존 ID는 INSERT가 아니라 UPDATE

기존 예약을 `findById`로 읽어 `cancel()`한 뒤 다시 저장하면 `SELECT`와 `UPDATE`가 필요하다고 예측했다. 통합 테스트에서 예측을 실행으로 확인했다.

```java
Reservation saved = repository.save(reservation);
entityManager.flush();

saved.cancel();
repository.save(saved);
entityManager.flush();
entityManager.clear();

List<Reservation> reservations = repository.findAll();

assertEquals(countBeforeSave + 1, reservations.size());
assertEquals(1, savedIdRows.size());
assertFalse(savedIdRows.get(0).isConfirmed());
```

Hibernate 로그에는 신규 저장의 INSERT 뒤 기존 행을 바꾸는 SQL이 찍혔다.

```sql
update reservation
set confirmed=?, requester_name=?, room_name=?
where id=?
```

두 번 저장한 뒤 전체 행 수가 한 건만 증가하고 같은 ID가 한 행이라는 단언이 중요하다. UPDATE 로그만 봐서는 다른 INSERT가 함께 발생하지 않았다고 단정할 수 없지만, 상대 행 수와 ID 개수까지 검사하면 Day9의 중복 행 결함이 재발하지 않았음을 확인할 수 있다.

### 오늘 확인한 것

| 검증 대상 | 방법 | 결과 |
|---|---|---|
| Entity 신규 저장 | JPA 통합 테스트 + 강제 flush | ID 생성, `INSERT` 확인 |
| 단건 조회 | 영속성 컨텍스트를 비운 뒤 `findById` | `SELECT ... WHERE id=?`, 필드 값 일치 |
| 기존 Entity 갱신 | 취소 후 재저장 + 강제 flush | `UPDATE ... WHERE id=?` 확인 |
| 중복 행 방지 | 저장 전후 행 수와 같은 ID 개수 비교 | 행 수 +1, 같은 ID 한 행, `confirmed=false` |
| 기존 회귀 | 전체 12개 `clean test` | 성공 |

`flush()`는 오늘 SQL을 검증 지점까지 보내기 위해 명시했다. 왜 `save()`를 호출한 줄과 UPDATE 실행 시점이 다를 수 있는지는 아직 결론 내리지 않았다. 다음 Day의 영속성 컨텍스트와 그다음 Day의 변경 감지·flush에서 관찰할 주제다.

## 3. 스스로 답한 질문

### Q1. 빈 기본 생성자가 있어도 `final` 필드는 유지할 수 있는가

**A1.** 처음에는 컴파일된다고 예측했지만 틀렸다. `final` 인스턴스 필드는 모든 생성자 경로에서 초기화되어야 한다. 완성 코드에서는 두 문자열 필드의 `final`을 제거하고 JPA용 생성자를 `protected`로 제한했다. JPA에는 객체 복원 경로를 주면서, 일반 코드에는 의미 없는 빈 예약 생성을 감춘 선택이다.

## 4. 정리하며

JPA 전환의 결과를 "코드가 짧아졌다"로만 정리하면 핵심을 놓친다. 줄어든 것은 DB 작업 자체가 아니라 JDBC 반복 코드다. SQL 생성과 행-객체 변환의 책임은 Hibernate로 이동했고, Repository 구현 생성은 Spring Data가 맡았다.

애플리케이션의 경계는 그대로 유지됐다. Service는 여전히 `ReservationRepository`에 의존하고, JPA 어댑터만 Spring Data를 안다. 덕분에 Controller와 Service를 건드리지 않고 저장 기술을 교체했다.

가장 중요한 검증은 Day9의 실패를 다시 겨냥한 테스트였다. 기존 ID를 저장했을 때 UPDATE가 발생하는지뿐 아니라 행이 하나로 유지되는지까지 확인했다. 테스트의 가치는 통과 개수보다 어떤 실패를 겨냥했는지에 달려 있다.

다음 Day에는 같은 ID를 한 트랜잭션 안에서 두 번 조회한다. 실행 전에 SELECT 횟수와 두 객체의 참조 동일성을 예측하고, 영속성 컨텍스트의 1차 캐시가 무엇을 보장하는지 확인할 예정이다.

면접 질문으로 남겨둔다.

- JPA, Hibernate, Spring Data JPA의 역할을 각각 설명하고, 현재 코드에서 각 층의 근거를 찾을 수 있는가?
- Repository 구현을 JDBC에서 JPA로 바꿨는데 Service가 수정되지 않은 이유를 의존성 방향으로 설명할 수 있는가?
