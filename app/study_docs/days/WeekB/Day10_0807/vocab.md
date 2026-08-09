# Day10 (8/7 계획 → 8/9 완료, Week B D3) 용어

주제: Entity 매핑 + Spring Data JPA 기본 CRUD

## 1. JPA의 세 층

| 층 | 역할 | 오늘 코드 |
|---|---|---|
| JPA | 자바 객체와 관계형 DB를 매핑하는 **명세(규칙)** | `jakarta.persistence.Entity`, `Id` |
| Hibernate | JPA 명세를 실제 SQL로 실행하는 구현체 | 실행 로그의 `insert`, `select`, `update` |
| Spring Data JPA | Repository 인터페이스의 구현을 런타임에 만들어 반복 코드를 줄이는 편의층 | `JpaRepository<Reservation, Long>` |

`JpaRepository`를 상속한 인터페이스의 본문이 비어 있어도 동작하는 이유는 Spring Data가 애플리케이션 시작 시 구현 객체를 만들어 Bean으로 등록하기 때문이다.

## 2. Entity 매핑

| 용어 | 한줄뜻 | 코드 모습 |
|---|---|---|
| `@Entity` | 이 클래스를 JPA가 관리할 영속 객체로 표시 | `Reservation` 클래스 위 |
| `@Id` | Entity를 구분하는 식별자, DB의 PK에 대응 | `private Long id` |
| `@GeneratedValue(IDENTITY)` | INSERT 때 DB가 생성한 식별자를 Entity에 돌려받음 | H2 `AUTO_INCREMENT`와 대응 |
| 기본 생성자 | Hibernate가 조회 결과로 객체를 만들 때 사용하는 진입점 | `protected Reservation() {}` |
| 필드 접근 | `@Id`가 필드에 있으므로 JPA가 필드를 기준으로 매핑 | getter가 아닌 필드 애노테이션 |

기본 생성자를 `protected`로 둔 것은 JPA에는 열어주되 애플리케이션 코드가 의미 없는 빈 예약을 자유롭게 만들지 못하게 하기 위해서다.

## 3. CRUD와 어댑터

| 동작 | Spring Data 호출 | 관찰된 SQL |
|---|---|---|
| 신규 저장 | `save(id == null)` | `INSERT` |
| 단건 조회 | `findById(id)` | `SELECT ... WHERE id=?` |
| 전체 조회 | `findAll()` | `SELECT` |
| 기존 Entity 저장 | `save(id != null)` | 검증 실험에서 `UPDATE` |

`JpaReservationRepository`는 애플리케이션이 이미 의존하던 `ReservationRepository`를 구현하고, 실제 DB 작업은 `SpringDataReservationRepository`에 위임한다. 그래서 Service와 Controller는 바꾸지 않고 저장 기술만 JDBC에서 JPA로 교체할 수 있었다.

## 4. 스키마의 주인

`spring.jpa.hibernate.ddl-auto: none`으로 두어 Hibernate가 테이블을 만들거나 고치지 않게 했다. 현재 스키마의 주인은 Day8에 만든 Flyway `V1__init.sql`이다. 두 도구가 동시에 스키마를 변경하면 Flyway 이력과 실제 DB가 어긋날 수 있다.

Week B D7에는 `none`을 `validate`로 올려, Hibernate가 변경은 하지 않고 Entity와 스키마가 맞는지만 검사할 예정이다.

## 5. 다음 Day로 넘기는 경계

이번 Day의 `flush()`는 테스트에서 SQL을 실제로 발생시켜 검증하기 위한 장치로만 사용했다. 왜 UPDATE가 `save()` 호출 즉시가 아니라 flush 시점에 나갈 수 있는지, 같은 ID를 두 번 조회했을 때 왜 같은 객체가 될 수 있는지는 Day11의 영속성 컨텍스트에서 다룬다.
