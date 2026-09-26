# Day19 (2026-09-26 시작, Week C D5) 진행 기록

> 주제: N+1 확인 + fetch join
> 상태: **완성예제(①)까지 완료, 나머지는 다음 세션으로 이월.** Full 루프 6단계 중 ①완성예제(문제 재현 + fetch join 해결)·⑤예측→실행→차이설명 완료. ②빈칸예제·③독립 변형·④인출(노트 덮고 재작성)·⑥복습큐 등록(신규 일부만)은 남음.

## 1. 완료한 것

| 항목 | 내용 |
|---|---|
| `NPlusOneTest.java` (신규) | `findAllTriggersNPlusOneSelects()` — LAZY 목록 순회 시 N+1 재현(1+3=4번). `findAllWithMemberUsesSingleJoinQuery()` — fetch join으로 1번 확인 |
| `SpringDataReservationRepository.java` | `@Query("select r from Reservation r join fetch r.member") List<Reservation> findAllWithMember();` 추가 |
| `ReservationRepository.java` | `findAllWithMember()`를 `default` 메서드로 추가(몸통은 `throw new UnsupportedOperationException()`) — 기존 구현체(`InMemoryReservationRepository`, `JdbcReservationRepository`) 무변경으로 컴파일 유지 |
| `JpaReservationRepository.java` | `findAllWithMember()` 구현 — `delegate.findAllWithMember()`로 위임 |

## 2. 겪은 오류 (❌ 흔한 실수 후보 — 주차 마무리 패턴 승격 때 재료로 씀)

1. `@Query`를 메서드가 아니라 인터페이스 선언에 붙임 → 애노테이션 적용 대상 불명확
2. 인터페이스에 추상 메서드 추가 → 대조군 구현체 2개(`InMemoryReservationRepository`, `JdbcReservationRepository`) 컴파일 에러(`is not abstract and does not override abstract method`)
3. `default <Reservation> findAllWithMember()` — 반환 타입 자리에 제네릭 타입 파라미터 문법을 씀(`List<Reservation>` 대신 `<Reservation>`)

## 3. 예측 기록과 판정 (예측→실행→차이설명)

- N+1(`findAll()`): 1차 예측 "3/4/7" 오답, 재설명 후에도 "잘 모르겠다" → 직접 설명 후 실행으로 확인(실측: 0/3/4)
- fetch join(`findAllWithMember()`): 예측 "1번 찍히고 0번" — 정답, 실행으로 확인(실측: 1/0)

## 4. 세션 시작 인출 워밍업 결과 (9/26, 오답재시험 3건 + 도래일 오래된 4건 중 3건만 실제 처리)

- self-invocation(this가 프록시를 우회하는 이유): 두 번 다 불완전/순환논리 → 직접 설명으로 교정, **오답재시험 지속**(9/27)
- `transactionalOuter()`가 `true`인 이유: 방향은 맞았으나 "영속성 컨테이너" 용어 오용 → 정정 후 통과, +2(9/28)
- Entity 기본 생성자 + `final` 필드 컴파일 규칙: 1차 "JPA 런타임 문제"로 오답 → 재시도에서 정확히 답변, 통과, +2(9/28)
- 나머지 4문항(DIP 이유, `@Positive` 클래스 애노테이션, 단위테스트 vs MockMvc, Flyway 불변 규칙)은 사용자가 "이전에 답한 것 같다"고 판단해 이번 세션 스킵 — 과거 세션 기록에서 해당 재시험 근거를 찾지 못해 복습큐 상태는 변경하지 않았다. 다음 세션에서 다시 다룰 것.

## 5. 오늘 개념과 무관하게 발견·해결한 인프라 문제

1. **테스트 격리 버그** — `ReservationControllerHttpTest`에 `@Transactional`이 빠져 있어, 이 클래스가 만든 멤버 없는 Reservation이 공유 인메모리 테스트 DB(`build.gradle.kts`의 `jdbc:h2:mem:testdb;...;DB_CLOSE_DELAY=-1`)에 커밋된 채 남아 `NPlusOneTest`를 오염시킴(`NullPointerException`). `@Transactional` 추가로 해결. **사용자가 절대 규칙("학습자 코드를 대신 짜지 않는다") 예외를 명시적으로 승인**해 AI가 직접 수정 — 원래는 오늘 개념(N+1)이 아니라 별개의 기존 버그였기 때문.
2. **IntelliJ 테스트 실행이 prod DB로 샐 뻔한 문제** — 시스템 환경변수에 남아있던 다른 프로젝트용 프로덕션 Postgres(Neon) 접속 정보를, IntelliJ 기본 테스트 실행이 Gradle의 안전장치(환경변수 오버라이드)를 우회해서 그대로 물려받고 있었다. `app/.idea/gradle.xml`에 `testRunner=GRADLE` 옵션을 추가해 해결. 시스템 환경변수 자체는 다른 프로젝트가 쓸 수 있어 삭제하지 않음(사용자 결정 필요 시 별도 처리).

두 항목 모두 [기술부채.md](../../../기술부채.md)에 발견·해결 기록으로 등록했다.

## 6. 상태 확인

| 시점 | 결과 |
|---|---|
| 세션 시작 | Day18 이월분 처리(기술부채 등록) |
| `NPlusOneTest` 최초 작성 후 전체 스위트 | 25개 중 1개 실패(`NullPointerException`, 테스트 격리 버그) |
| `ReservationControllerHttpTest`에 `@Transactional` 추가 후 | `./gradlew clean test` BUILD SUCCESSFUL, 25/25 |
| fetch join 3파일 작성 후 (1차) | `compileJava` 실패 — `@Query` 위치 오류, 인터페이스 확장 전파, 제네릭 문법 오류 3건 순차 발견·교정 |
| 최종 | `./gradlew test` BUILD SUCCESSFUL, `NPlusOneTest` 2건 모두 통과(4번→1번 실측 확인) |

## 7. [직접 작성] 오늘 배운 것을 내 문장으로

<!-- 아래는 학습자가 직접 채운다. 비워두지 말 것. -->

- N+1 문제가 정확히 어느 시점(코드 줄)에서 발생하는지:
- fetch join이 LAZY와 정반대로 동작하는 지점:
- 인터페이스에 메서드를 추가할 때 고려해야 하는 것:

## 8. 다음 세션 시작점

**Day19 이어서 — 빈칸예제(②)부터 재개.** ①완성예제는 끝났으니, N+1/fetch join을 살짝 다른 조건(예: 다른 연관관계 필드, 또는 단건 조회 `Optional<Reservation>`에 fetch join 적용 등)으로 빈칸 → 독립 변형 → 인출(노트 덮고 재작성) → 복습큐 신규 항목 등록 순으로 이어간다. Velog 포스트는 사용자가 별도로 Opus 세션에서 작성 예정.
