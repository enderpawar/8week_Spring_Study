# StudyRoom API — Spring 백엔드 기본기 학습 기록

빈 Spring Boot 프로젝트에서 출발해 HTTP 요청 처리부터 데이터 접근, 트랜잭션, 인증, 테스트와 운영까지 한 계층씩 직접 구현하는 학습 저장소입니다.

완성 코드만 전시하지 않습니다. **예측 → 구현·실행 → 결과 비교 → 오답 교정** 과정을 코드, 테스트, 인출 문제와 기술 글로 함께 남깁니다. 학습한 개념은 +2일·+7일·+14일에 다시 설명하며, 실행하지 않은 결과나 완성하지 않은 기능을 성과로 적지 않습니다.

## 현재 진행 상황

실제 코드와 Day 산출물로 확인된 완료 지점은 **Week B Day 10 — Entity 매핑과 Spring Data JPA 기본 CRUD**입니다. 다음 학습은 **Day 11 — 영속성 컨텍스트·1차 캐시·동일성**입니다.

| 단계 | 학습 주제 | 구현·학습 증거 | 상태 |
|---|---|---|---|
| Week A · Day 1~7 | HTTP, DTO·Domain, 검증·오류, 계층 분리, IoC·DI | [Week A 기록](study_docs/days/WeekA/) · [주차 마무리 글](study_docs/velog/week-a-identity-storage-error-boundary.md) | 완료 |
| Day 8 | Flyway 스키마 버전 관리 | [기술 글](study_docs/days/WeekB/Day08_0801/velog_post.md) · [실험 기록](study_docs/days/WeekB/Day08_0801/explain-log.md) | 완료 |
| Day 9 | JDBC 직접 구현과 저장 계약 | [기술 글](study_docs/days/WeekB/Day09_0802/velog_post.md) · [실험 기록](study_docs/days/WeekB/Day09_0802/explain-log.md) | 완료 |
| Day 10 | Entity 매핑과 Spring Data JPA CRUD | [기술 글](study_docs/days/WeekB/Day10_0807/velog_post.md) · [검증 기록](study_docs/days/WeekB/Day10_0807/progress.md) | 완료 |
| Day 11 | 영속성 컨텍스트·1차 캐시·동일성 | [5주 로드맵](study_docs/FUNDAMENTALS_ROADMAP.md) | 다음 학습 |

Day 10까지 `ReservationRepository` 경계는 유지하면서 저장 구현을 InMemory → JDBC → Spring Data JPA 어댑터로 교체했습니다. Flyway가 스키마를 관리하고 Hibernate는 Entity 매핑을 통해 CRUD SQL을 실행합니다. JPA 통합 테스트는 신규 저장·조회와 기존 ID 갱신·중복 방지를 검증합니다.

## 현재 코드에서 확인할 수 있는 구조

```text
HTTP 요청
   ↓
ReservationController      HTTP 매핑·요청 검증·응답
   ↓
ReservationService         예약 생성·취소 흐름
   ↓
ReservationRepository              애플리케이션 저장소 계약
   ↑ implements
JpaReservationRepository           Spring Data 어댑터
   ↓ delegates
SpringDataReservationRepository    런타임 구현 생성
   ↓
Hibernate → Flyway V1 스키마
```

- `ReservationRequest`: 요청 경계의 `record` DTO와 Bean Validation
- `Reservation`: `@Entity`로 매핑된 예약 Domain 객체
- `GlobalExceptionHandler`: 검증 실패를 공통 400 응답으로 변환
- `ReservationRepository`: 구현체 교체를 위한 저장소 인터페이스
- `JpaReservationRepository`: 애플리케이션 계약을 Spring Data JPA에 연결하는 어댑터

현재 의존성은 Spring Web·Validation·JDBC·Flyway·Spring Data JPA·H2입니다. Spring Security와 JWT는 Week D에서 추가합니다. 계획된 기술을 현재 구현처럼 표시하지 않습니다.

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

코드 작성 형태를 인출할 때는 [패턴 드릴](study_docs/PATTERN_DRILLS.md)을 먼저 풀고, 막히거나 틀린 뒤에 [코드 패턴 참조서](study_docs/CODE_PATTERNS.md)와 실제 `src/` 코드를 대조합니다. 두 파일은 Day 10까지 P1~P17과 D1~D17을 담고 있으며, UML 스타일 관계도와 실행 시퀀스는 여러 계층이 연결되는 패턴에만 사용합니다.

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
3. 해당 `study_docs/days/WeekX/DayNN_MMDD/` — 기존 학습 증거와 중단 지점
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
│  ├─ CODE_PATTERNS.md         # 검증된 코드 골격·판단·실제 오류 참조서
│  ├─ PATTERN_DRILLS.md        # 정답 없는 빈칸·판정·독립 드릴
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
