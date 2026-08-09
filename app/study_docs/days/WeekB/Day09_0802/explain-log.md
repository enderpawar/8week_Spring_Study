# Day09 (8/2 계획 → 8/6 진행, Week B D2) 예측→실행→차이 기록

주제: `InMemoryReservationRepository`를 JDBC 구현으로 교체

## 실험 1 — 구현체가 둘이 되면 무슨 일이 나는가

`@Repository`가 붙은 `ReservationRepository` 구현체를 2개로 만든 채 기동.

**예측(학습자):** (A) 나중에 만든 쪽이 이긴다

**실행:**
```
org.springframework.beans.factory.NoUniqueBeanDefinitionException:
No qualifying bean of type 'com.example.studyroom.repository.ReservationRepository' available:
expected single matching bean but found 2:
inMemoryReservationRepository, jdbcReservationRepository

10 tests completed, 6 failed
```

**차이:** 예측 빗나감. 기동 자체가 실패했다. 스프링은 생성자 주입에서 **타입으로 후보를 모으고**, 결과가 2개면 고를 근거가 없다고 판단한다. "나중 것" 같은 규칙을 만들지 않은 이유는, 임의로 골랐을 때의 사고가 조용하기 때문이다 — InMemory가 선택됐다면 앱은 정상적으로 뜨고 예약도 되는 것처럼 보이지만 재시작마다 데이터가 사라진다. 모호하면 즉시 죽는 편이 낫다.

**부수 관찰:** Repository와 무관한 `reservationServiceBeanIsSingleton`까지 6개가 함께 죽었다. 컨텍스트가 못 뜨면 그 위의 모든 테스트가 같이 죽는다 — Day8 체크섬 실험과 같은 패턴(그때도 6개).

**해결:** `InMemoryReservationRepository`의 `@Repository`를 제거. 삭제하지 않은 이유는 `ReservationServiceTest`가 이 클래스를 **테스트 대역(fake)** 으로 쓰고 있기 때문. Day4에 인터페이스를 뽑은 목적이 여기서 값을 치렀다 — 실행은 DB 구현, 테스트는 메모리 구현.

## 실험 2 — application.yml에 H2라고 썼는데 PostgreSQL을 찾았다

**증상:**
```
Caused by: java.lang.RuntimeException:
Failed to load driver class org.postgresql.Driver
```

**원인:** 사용자 계정 범위 환경변수가 걸려 있었다.
```
SPRING_PROFILES_ACTIVE     = prod
SPRING_DATASOURCE_URL      = jdbc:postgresql://...neon.tech/neondb
SPRING_DATASOURCE_USERNAME / PASSWORD
```

Spring Boot는 설정을 여러 출처에서 겹쳐 읽고, **OS 환경변수가 `application.yml`보다 우선순위가 높다.** 이 규칙 자체는 같은 jar를 환경만 바꿔 배포하기 위한 장치(Week E D1·D3)인데, 이번엔 다른 프로젝트용 값에 의도치 않게 걸렸다.

**위험했던 점:** `SPRING_PROFILES_ACTIVE=prod` + 원격 운영 DB 주소 + `flyway.enabled: true` 조합이었다. PostgreSQL 드라이버가 의존성에 없어 커넥션 단계에서 막혔지만, 있었다면 **로컬 실행이 원격 DB에 `V1__init.sql`을 실행했을 것이다.**

**조치:** `build.gradle.kts`의 `test` 태스크에서 DataSource 환경변수를 고정.
```kotlin
environment("SPRING_DATASOURCE_URL", "jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1")
environment("SPRING_PROFILES_ACTIVE", "test")
```
임시방편이 아니라 원래 있어야 할 것이다. 테스트는 실행하는 사람의 환경변수가 무엇이든 항상 같은 DB에서 돌아야 재현 가능하다.

**미완:** 사용자 환경변수 자체는 아직 제거하지 않았다. → 아래 "남은 일"

## 실험 3 — 컴파일 오류는 몇 개인가

`return reservation` 뒤 세미콜론 누락 상태로 빌드하니 `1 error`만 보고됐다. 고치자 **새로 3개**가 나타났다.

| 단계 | 하는 일 | 잡는 오류 |
|---|---|---|
| ① 파싱 | 글자를 문법 구조로 해석 | 세미콜론·괄호 |
| ② 의미 분석 | 이름이 실제 존재하는지 대조 | `cannot find symbol`, 타입 불일치 |

①에서 멈추면 ②는 아예 실행되지 않는다. **오류 개수도 증상일 뿐이다.**

새로 나온 3개 중 2개는 원인이 하나였다. `public JdbcReservation Repository(...)` — 클래스명 중간에 공백이 들어가 **생성자가 아니라 반환타입 `JdbcReservation` + 메서드 `Repository`** 로 해석됐고(문법상 합법이라 파싱은 통과), 그 결과 생성자가 아닌 곳에서 `final` 필드에 대입하게 되어 `cannot assign a value to final variable`이 따라왔다.

**이것이 아침에 3회 틀린 Q5의 실물 시연이다.** `final`이 막는 것 = JVM 메모리 안 변수의 재대입. 8/5에 H2 콘솔로 `UPDATE`했을 때 컴파일러가 아무 말도 못 했던 것과 대비된다. **컴파일러의 사정권 안이면 막고, 밖(DB 파일)이면 못 막는다.**

## 실험 4 — 재시작해도 데이터가 남는가

```
POST /reservations {"roomName":"A101","requesterName":"jinwoo"}  → 예약 번호1
POST /reservations {"roomName":"B202","requesterName":"minji"}   → 예약 번호2
POST /reservations/cancel/1                                       → 취소 응답
[앱 강제 종료 → 재기동]
Current version of schema "PUBLIC": 1
Schema "PUBLIC" is up to date. No migration necessary.
POST /reservations/cancel/2   → minji님이B202 예약을 취소하셨습니다
POST /reservations/cancel/99  → {"error":"예약을 찾을 수 없습니다. (id: 99)"}
```

**결과:** 2번 예약이 재시작을 넘어 살아남았다. `ArrayList` 구현이었다면 99번과 똑같이 404가 났을 것이다. Day4에 등록한 기술부채("프로세스를 재시작하면 데이터가 사라진다")가 해소됐다. Flyway는 `V1`을 다시 실행하지 않고 장부만 확인하고 넘어갔다.

## 실험 5 — 만든 적 없는 3번 예약이 존재한다

```
POST /reservations/cancel/3 → jinwoo님이A101 예약을 취소하셨습니다
```

만든 것은 1·2번뿐인데 3번이 **1번의 복사본**이었다.

**원인:** `JdbcReservationRepository.save()`가 무조건 INSERT만 한다. 취소 흐름은
`findById` → 객체의 `cancel()` → `save(reservation)` 인데, `id`가 이미 있는 객체가 들어와도 새 행을 만들어버린다. 그 결과 **1번 행의 `confirmed`는 여전히 true**이고 취소가 반영되지 않았다.

`InMemoryReservationRepository`에는 그 분기가 있었다(오늘 아침 W3 문항이 바로 이것).
```java
if (reservation.getId() == null) { ... add ... }   // 신규
else { ... store.set(index, reservation) ... }     // 기존 교체
```

**인터페이스는 시그니처만 강제하고 의미는 강제하지 못한다.** 같은 `Reservation save(Reservation)`이라도 계약을 지키는지는 구현자가 책임진다.

**그런데 테스트 10개는 전부 통과했다.**
- `ReservationServiceTest`는 InMemory 구현을 쓴다 → 거긴 계약이 지켜져 있다
- `ReservationControllerHttpTest`의 취소 테스트는 404 케이스만 본다 → 성공 후 행 개수를 아무도 세지 않는다

**초록불은 "버그가 없다"가 아니라 "지금 확인한 것들에는 문제가 없다"이다.**

## 오늘 얻은 답 — 왜 JPA인가

`InMemoryReservationRepository`의 `store.add(reservation)` **한 줄**이 JDBC에서 이렇게 늘어났다.

| 하는 일 | 줄 수(대략) |
|---|---|
| SQL 문자열 작성 | 1 |
| 커넥션·statement 열고 닫기 | 3 |
| 파라미터 번호 맞춰 바인딩 | 3 |
| 생성된 키 되받기 | 5 |
| 예외 변환 | 3 |
| 행→객체 매핑(`mapRow`) | 8 |

늘어난 것 중 **비즈니스 로직은 한 줄도 없다.** 전부 "자바 객체와 SQL 행 사이의 왕복"이다. 게다가 `mapRow`에서는 도메인 규칙과 충돌까지 났다 — 생성자가 `(roomName, requesterName)`만 받고 `confirmed`는 항상 false로 시작하도록 캡슐화해뒀는데(Day4), DB에는 이미 확정된 행이 있다. 그래서 "만들고 → id를 심고 → true일 때만 `confirm()`"의 3단계가 필요했다.

내일(D3) 이 코드가 얼마나 지워지는지가 "왜 JPA인가"의 답이다.

## 남은 일

1. **`save()`의 UPDATE 분기 미구현** — 오늘 과제로 냈으나 코드에 반영되지 않았다(`grep UPDATE` 결과 0건). 8/7 시작 시 첫 항목.
   - `getId() == null` → INSERT (현행 유지) / 아니면 `UPDATE ... WHERE id = ?`
   - `executeUpdate()` 반환값이 0이면 `IllegalArgumentException` (InMemory와 같은 계약)
2. **H2 콘솔 실제 확인 미실시** — `SELECT id, room_name, requester_name, confirmed FROM reservation` 로 중복 행과 1번의 `confirmed` 값을 눈으로 확인할 것.
3. **사용자 환경변수 정리 미완** — `SPRING_DATASOURCE_*`, `SPRING_PROFILES_ACTIVE`를 해당 프로젝트(`NextDoor CS` 또는 `CS-NextDoor` 추정)로 옮기고 사용자 범위에서 삭제. 비밀번호가 평문으로 노출된 상태이므로 교체 검토.
4. **D2 잔여 학습** — `JdbcTemplate`/`JdbcClient` 한 줄 비교, JPA·Hibernate·Spring Data JPA 구분.

## [직접 작성] 오늘 배운 것을 내 문장으로

<!-- 아래는 학습자가 직접 채운다. 비워두지 말 것. -->

- `ResultSet`을 한 문장으로:
- 테스트가 전부 통과했는데도 버그가 있었던 이유:
- `save()`가 "저장"이 아니라 계약인 이유:
