# StudyRoom API — Spring 백엔드 기본기 학습 기록

빈 Spring Boot 프로젝트에서 출발해 HTTP 요청 처리부터 데이터 접근, 트랜잭션, 인증, 테스트와 운영까지 한 계층씩 직접 구현하는 학습 저장소입니다.

완성 코드만 전시하지 않습니다. **예측 → 구현·실행 → 결과 비교 → 오답 교정** 과정을 코드, 테스트, 인출 문제와 기술 글로 함께 남깁니다. 학습한 개념은 +2일·+7일·+14일에 다시 설명하며, 실행하지 않은 결과나 완성하지 않은 기능을 성과로 적지 않습니다.

## 현재 진행 상황

실제 코드와 Day 산출물로 확인된 완료 지점은 **Week A Day 4 — 웹 계층의 책임 분리**입니다. 다음 학습은 **Day 5 — IoC·DI와 생성자 주입**입니다.

| 단계 | 학습 주제 | 구현·학습 증거 | 상태 |
|---|---|---|---|
| Day 1 | HTTP 요청→응답 왕복, 상태 코드 | [기술 글](study_docs/days/Day01_0725/velog_post.md) · [예측과 실제 차이](study_docs/days/Day01_0725/explain-log.md) | 완료 |
| Day 2 | record DTO와 Domain 분리 | [기술 글](study_docs/days/Day02_0726/velog_post.md) · [인출 문제](study_docs/days/Day02_0726/quiz.md) | 완료 |
| Day 3 | Bean Validation과 전역 오류 처리 | [기술 글](study_docs/days/Day03_0728/velog_post.md) · [복습 점검](study_docs/days/Day03_0728/velog_post_review.md) | 완료 |
| Day 4 | Controller·Service·Repository 책임 분리 | [기술 글](study_docs/days/Day04_0728/velog_post.md) · [검증 기록](study_docs/days/Day04_0728/progress.md) | 완료 |
| Day 5 | IoC·DI, Bean, 생성자 주입 | 현재 코드의 `@Service`·`@Repository`와 생성자 주입 실험 | 다음 학습 |
| Week B | Flyway, JDBC, JPA, 영속성 컨텍스트 | [5주 로드맵](study_docs/FUNDAMENTALS_ROADMAP.md) | Week A 완료 후 진행 |

Day 4에서는 `ReservationRepository` 인터페이스와 메모리 구현체를 분리하고, 예약 생성·취소 흐름을 Controller에서 Service로 옮겼습니다. 식별자 없이 특정 예약을 갱신할 수 없다는 문제를 만나 `id`를 도입했으며, Wrapper 비교와 저장소 중복 저장 가능성은 오답 및 후속 실험으로 남겼습니다.

## 현재 코드에서 확인할 수 있는 구조

```text
HTTP 요청
   ↓
ReservationController      HTTP 매핑·요청 검증·응답
   ↓
ReservationService         예약 생성·취소 흐름
   ↓
ReservationRepository      저장소 계약
   ↓
InMemoryReservationRepository
```

- `ReservationRequest`: 요청 경계의 `record` DTO와 Bean Validation
- `Reservation`: 예약 상태와 상태 변경 행동을 가진 Domain 객체
- `GlobalExceptionHandler`: 검증 실패를 공통 400 응답으로 변환
- `ReservationRepository`: 구현체 교체를 위한 저장소 인터페이스

현재 의존성은 Spring Web과 Validation뿐입니다. JPA·H2·Flyway는 Week B에서 직접 추가하고, Spring Security와 JWT는 Week D에서 추가합니다. 계획된 기술을 현재 구현처럼 표시하지 않습니다.

## 학습 방법과 증거

매일 어려운 개념 하나를 다음 순서로 학습합니다.

1. 도래한 복습 문제를 코드와 노트 없이 답합니다.
2. 동작하는 완성 예제를 읽고 실행합니다.
3. 핵심 부분만 비운 예제를 완성합니다.
4. 요구사항을 바꾼 기능을 독립적으로 구현합니다.
5. 결과를 예측한 뒤 실제 실행과 차이를 설명합니다.
6. 틀린 답을 교정하고 [복습큐](study_docs/복습큐.md)에 다음 인출일을 등록합니다.

각 Day 폴더에는 다음 기록이 함께 남습니다.

- `vocab.md`: 용어와 CS 지식의 연결
- `quiz.md`: 노트 없이 답하는 인출 문제와 실제 답변
- `explain-log.md`: 실행 전 예측, 실제 결과, 차이가 생긴 원인
- `velog_post.md`: 이론, 설계 판단, 검증 근거, 오답, 현재 한계를 연결한 기술 글

기술 글은 [복습·포트폴리오 겸용 템플릿](study_docs/VELOg_POST_TEMPLATE.md)의 사실 검증 및 품질 기준을 따릅니다.

## 5주 로드맵

| 주차 | 핵심 범위 |
|---|---|
| Week A | HTTP, DTO·Domain 분리, 검증·오류 처리, 계층 분리, IoC·DI |
| Week B | SQL, Flyway, JDBC, JPA, 영속성 컨텍스트와 변경 감지 |
| Week C | 트랜잭션, Spring AOP 프록시, 지연 로딩, N+1 |
| Week D | BCrypt, JWT, Security Filter Chain, 테스트 분류 |
| Week E | 로깅, 디버깅, Docker, CI, 누적 독립 과제 |

상세 일정과 세션 재개 체크포인트는 [백엔드 기본기 로드맵](study_docs/FUNDAMENTALS_ROADMAP.md)에 있습니다.

## 학습 세션 재개 규칙

`오늘 학습 시작`, `week N 시작`, `week N 공부 시작`이라고 말하면 에이전트는 다음 순서로 확인합니다.

1. [로드맵 진행 체크리스트](study_docs/FUNDAMENTALS_ROADMAP.md) — 마지막 완료 Day와 다음 필수 Day
2. [복습큐](study_docs/복습큐.md) — 오늘 도래한 문제와 전날 오답
3. 해당 `study_docs/days/DayNN_MMDD/` — 기존 학습 증거와 중단 지점
4. 현재 소스와 테스트 — 실제 구현 상태
5. [기술 블로그 템플릿](study_docs/VELOg_POST_TEMPLATE.md) — 당일 글의 작성·검수 기준

달력상 다음 주차가 시작됐더라도 미완료 필수 유닛을 건너뛰지 않습니다. 새 세션을 위한 세부 지침은 [AGENTS.md](AGENTS.md)에 있습니다.

## 저장소 구조

```text
.
├─ AGENTS.md
├─ src/                         # Spring Boot 코드와 테스트
├─ study_docs/
│  ├─ days/                    # 날짜별 학습 증거
│  ├─ FUNDAMENTALS_ROADMAP.md  # 로드맵과 진행 체크리스트
│  ├─ VELOg_POST_TEMPLATE.md   # 기술 블로그 작성·검수 기준
│  ├─ 복습큐.md
│  ├─ spring-core-notes.md
│  └─ interview-notes.md
├─ build.gradle.kts
└─ gradlew / gradlew.bat
```

## 실행 및 검증

요구 사항은 Java 17입니다.

```bash
./gradlew test
./gradlew bootRun
```

Windows PowerShell에서는 `./gradlew.bat test`, `./gradlew.bat bootRun`을 사용할 수 있습니다. 명령 자체의 암기보다 실행 결과가 어떤 개념을 검증하는지 기록하는 데 집중합니다.
