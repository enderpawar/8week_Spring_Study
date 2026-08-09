# Day10 (8/7 계획 → 8/9 완료, Week B D3) 예측→실행→차이 기록

주제: JDBC 구현을 Spring Data JPA 어댑터로 교체

## 실험 1 — `final` 필드와 JPA 기본 생성자

**조건:** `roomName`, `requesterName`이 `final`인 상태에서 인자 없는 `protected Reservation() {}`를 추가한다.

**예측(학습자):** "된다"

**판정:** 빗나갔다. 자바의 `final` 인스턴스 필드는 모든 생성자 경로에서 한 번 초기화되어야 한다. 인자 없는 생성자에는 두 값을 넣을 방법이 없으므로 그 상태로는 컴파일할 수 없다.

**완성 코드에서 선택한 것:** 두 필드의 `final`을 제거하고 도메인용 생성자와 JPA용 기본 생성자를 함께 유지했다.

```java
@Entity
public class Reservation {
    private String roomName;
    private String requesterName;

    protected Reservation() {
    }
}
```

Day9에서 세 번 틀린 `final`의 범위와 연결된다. `final`은 "객체가 영원히 불변"이라는 표시가 아니라 **그 자바 변수의 재대입을 컴파일러가 막는 장치**다. JPA가 DB 행으로 객체를 복원하는 과정과 충돌하면 매핑 방식이나 객체 설계를 함께 판단해야 한다.

## 실험 2 — 비어 있는 Repository 인터페이스가 동작하는가

**완성 코드:**

```java
public interface SpringDataReservationRepository
        extends JpaRepository<Reservation, Long> {
}
```

인터페이스에는 `save`, `findById`, `findAll` 구현이 없다. 애플리케이션 기동 로그에서 Spring Data가 JPA Repository 인터페이스 1개를 찾았고, 통합 테스트는 그 런타임 구현을 통해 세 메서드를 실행했다.

JDBC의 `PreparedStatement`, 파라미터 바인딩, `ResultSet`, `mapRow`가 사라진 것은 DB 왕복이 사라졌기 때문이 아니다. Hibernate가 Entity 매핑을 읽고 그 반복 작업을 대신 수행한다.

## 실험 3 — 기존 ID를 `save()`하면 중복 행이 생기는가

**예측(학습자):** `findById`의 `SELECT` 뒤 `UPDATE`가 실행된다.

**검증 테스트:**

1. 새 예약을 저장하고 `flush()`한다.
2. 같은 객체를 취소한 뒤 다시 `save()`하고 `flush()`한다.
3. 영속성 컨텍스트를 비우고 `findAll()`한다.
4. 두 번 저장해도 전체 행 수는 한 건만 증가하고, 같은 ID가 한 행이며, `confirmed == false`인지 검사한다.

**실행 결과:** 테스트 통과. Hibernate 로그에서 `INSERT` 뒤 다음 SQL을 확인했다.

```sql
update reservation
set confirmed=?, requester_name=?, room_name=?
where id=?
```

Day9 JDBC 구현은 같은 흐름에서 무조건 INSERT하여 복제 행을 만들었다. JPA 어댑터로 교체한 뒤에는 두 번 저장해도 전체 행 수가 한 건만 증가하고 기존 ID의 상태가 갱신됐다.

## 실험 4 — Flyway와 Hibernate 중 누가 테이블을 만드는가

설정은 `ddl-auto: none`이다. 통합 테스트 로그에서 Flyway가 `V1__init.sql`을 적용해 스키마를 version 1로 만든 뒤 Hibernate의 `EntityManagerFactory`가 초기화됐다. Hibernate가 DDL을 생성했다는 기록은 없었다.

**결론:** 현재 역할 분리는 다음과 같다.

- Flyway: 스키마 생성·변경과 버전 이력
- JPA/Hibernate: 이미 존재하는 테이블과 Entity 사이의 데이터 왕복
- Spring Data JPA: Repository 구현 생성

## 자동 검증

| 검증 | 결과 |
|---|---|
| `JpaReservationRepositoryTest` 신규 저장·단건 조회 | 통과 |
| `JpaReservationRepositoryTest` 기존 ID 갱신·중복 방지 | 통과 |
| 전체 `clean test` 12개 | 성공 |

첫 전체 실행에서는 다른 통합 테스트가 남긴 행을 고려하지 않고 전체 행 수를 1로 고정해 Day10 테스트 1개가 실패했다. 저장 전후의 **상대 행 수(+1)** 와 같은 ID의 행 개수(1)를 검사하도록 바꿨고, 재실행에서 12개가 모두 통과했다. 기능 결함이 아니라 테스트 격리 가정의 결함이었다.

## 다음 학습 시작점

Day11: 영속성 컨텍스트·1차 캐시·동일성. 같은 ID를 한 트랜잭션 안에서 두 번 조회하기 전에 SQL 횟수와 객체 참조 동일성을 먼저 예측한다.

## [직접 작성] 오늘 배운 것을 내 문장으로

<!-- 유실 전 학습자 원문을 확인할 수 없어 임의 복원하지 않았다. 아래는 학습자가 직접 채운다. -->

- `jakarta.persistence`가 명세라는 게 무슨 뜻인지:
- Spring Data 인터페이스를 `class`가 아니라 `interface`로 선언해야 하는 이유:
- 애노테이션의 "위치"가 의미를 갖는 이유:
