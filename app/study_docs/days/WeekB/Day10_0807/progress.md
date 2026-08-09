# Day10 (2026-08-07 계획 → 2026-08-09 완료, Week B D3) 진행 기록

> 주제: Entity 매핑 + 기본 CRUD (JDBC 구현 → Spring Data JPA 교체)
> 상태: **완료.** 유실 뒤 남아 있던 코드와 예측 기록을 복구하고 통합 테스트로 재검증했다.

## 1. 완료한 것

| 항목 | 내용 |
|---|---|
| `build.gradle.kts` | `spring-boot-starter-data-jpa` 추가. **`starter-jdbc`는 유지** — 어제 만든 JDBC 구현을 대조군으로 남기기 위해 |
| `application.yml` | `spring.jpa` 블록 추가 |
| `Reservation.java` | `@Entity`, 필드의 `@Id`·`@GeneratedValue(IDENTITY)`, `protected` 기본 생성자 매핑 |
| `SpringDataReservationRepository.java` | `interface` + `extends JpaRepository<Reservation, Long>`, 본문 비움 |
| `JpaReservationRepository.java` | 기존 `ReservationRepository`를 구현하고 Spring Data에 `save/findById/findAll` 위임 |
| `JdbcReservationRepository.java` | `@Repository` 제거. JDBC 코드는 Day9 대조군으로 남기되 실행 Bean 후보에서는 제외 |
| `JpaReservationRepositoryTest.java` | 신규 저장·조회와 기존 ID 갱신·중복 방지를 실제 H2/Flyway/JPA로 검증 |

### `ddl-auto: none`으로 못 박은 이유

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: none        # Hibernate는 스키마를 건드리지 않는다. 주인은 Flyway.
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

DB가 `jdbc:h2:file:./data/studyroom` — 파일에 남는 DB이고, Day8에 Flyway `V1__init`으로 장부까지 찍어둔 상태다. Hibernate가 여기서 테이블을 새로 만들면 **Flyway 장부와 실제 스키마가 어긋나는 "두 주인" 문제**가 생긴다. D7에 `validate`(장부와 엔티티 일치 검사만)로 올린다.

## 2. 예측 기록과 판정

### P1. `@Entity` + `protected Reservation() {}` 추가 후 컴파일

현재 필드가 `private final String roomName;` / `private final String requesterName;` 인 상태.

- **학습자 예측: "된다"**
- **판정: 오답.** 빈 기본 생성자도 두 `final` 필드를 초기화해야 한다. 완성 코드에서는 두 필드의 `final`을 제거했다.

### P2. `cancel/1` 요청 시 `show-sql`에 찍히는 SQL

선택지: (A) SELECT+INSERT / (B) SELECT+UPDATE / (C) SELECT만 / (D) 없음

- **학습자 예측: (B) `SELECT` 1개 + `UPDATE` 1개**
- **판정: 정답.** 근거도 맞았다 — `findById`로 꺼낸 객체는 `id`가 이미 있으므로 신규가 아니다
- **실행 확인: 완료.** 통합 테스트 로그에서 기존 ID 저장 시 `UPDATE ... WHERE id=?`를 확인했고, 조회 결과가 같은 ID 한 행이며 `confirmed=false`임을 검증했다.

## 3. 다음 Day로 이월한 관찰 과제

**`UPDATE`가 로그에 찍히는 *시점*.** `save()`를 호출한 줄에서 바로 나오는가, 메서드가 끝난 뒤에 나오는가?

Day10 테스트에서는 검증 지점을 고정하려고 `EntityManager.flush()`를 명시했다. `save()`와 SQL 실행 시점이 왜 분리될 수 있는지는 **D4·D5(영속성 컨텍스트·변경 감지·flush)** 에서 예측 후 관찰한다.

관련: `@GeneratedValue(strategy = IDENTITY)`는 DB가 INSERT를 실행해야 번호를 알 수 있어 **INSERT는 미룰 수 없다.** UPDATE와 대비된다.

## 4. D2 잔여 3건 처리 결과

| # | 항목 | 처리 |
|---|---|---|
| ① | `JdbcReservationRepository.save()`의 UPDATE 분기 | **JDBC 구현에는 끝내 쓰지 않았다.** Spring Data `save()`가 동일한 분기(ID 없으면 INSERT, 있으면 UPDATE)를 수행하므로 대체됨 |
| ② | `JdbcTemplate`/`JdbcClient` 한 줄 비교 | D3 도입부에서 흡수 — **둘 다 "SQL은 내가 쓴다"의 세계**. 반복 코드만 줄이고 SQL·`RowMapper`는 남는다 |
| ③ | JPA·Hibernate·Spring Data JPA 구분 | D3 도입부에서 흡수 — 명세(`jakarta.persistence`) / 구현(`org.hibernate`) / 편의층(`org.springframework.data.jpa`)의 3층. JDBC↔드라이버와 같은 구조 |

## 5. 밀린 인출 (D7 버퍼로 유지)

- **Day09 `quiz.md` §1 — 6문항 전부**
- **오답 재시험 4건** — 저장 계약(2회), 예외 로그/응답 분리(2회), `final`의 작용 범위(**3회**), 싱글톤 요청별 값
- Day08 계열 +2일 도래분 8건

## 6. 오늘 만든 학습 자산 (Day 범위 밖)

학습자 지적: *"vocab 뿐으로는 Spring 코드 작성시 어떻게 써야되나 이런게 보충 안되는것 같은데"*

산출물 구조에 **절차적 지식(코드 형태)을 담는 파일이 없다**는 진단이 맞아, 두 파일을 신설했다.

| 파일 | 용도 |
|---|---|
| `study_docs/CODE_PATTERNS.md` | 검증된 코드 골격 + 판단 근거 + **본인이 실제로 낸 오류**. **찾아보는 참조서** |
| `study_docs/PATTERN_DRILLS.md` | 패턴과 대응하는 빈칸·판정 문제. **손으로 먼저 채우는 곳** |

`❌ 흔한 실수` 항목은 전부 Week A `explain-log.md` 7개에서 뽑았다. 일반론은 넣지 않았다.

## 7. 상태 확인

| 시점 | 결과 |
|---|---|
| 세션 시작 | `./gradlew test` **green** (종료 코드 0) |
| 유실 전 중단 지점 | `protected Reservation()}` 문법 오류로 `compileJava` 실패 |
| 복구 후 기존 전체 검증 | `./gradlew clean test --no-daemon` 성공 |
| 최종 전체 검증 | `./gradlew clean test --no-daemon` — 12개 통과 |
| Day10 전용 검증 | `JpaReservationRepositoryTest` 2개 통과. 첫 전체 실행의 고정 행 수 가정은 상대 행 수 검사로 교정 |
| SQL 관찰 | 신규 `INSERT`, 단건 `SELECT`, 기존 ID `UPDATE`, 전체 `SELECT` 확인 |

Day9 JDBC 대조군의 무조건 INSERT 결함은 코드에 그대로 남아 있다. 그러나 실행 Bean은 JPA 어댑터 하나이고, Day10 통합 테스트가 실제 선택된 `ReservationRepository`의 갱신 계약과 중복 방지를 검증한다.

## 8. 다음 시작점

Day11(Week B D4) 영속성 컨텍스트·1차 캐시·동일성. 한 트랜잭션 안에서 같은 ID를 두 번 조회할 때 예상 SELECT 횟수와 `first == second` 결과를 먼저 예측한다.

## 9. [직접 작성] 오늘 배운 것을 내 문장으로

<!-- 아래는 학습자가 직접 채운다. 비워두지 말 것. -->

- `jakarta.persistence`가 명세라는 게 무슨 뜻인지:
- Spring Data 인터페이스를 `class`가 아니라 `interface`로 선언해야 하는 이유:
- 애노테이션의 "위치"가 의미를 갖는 이유:
