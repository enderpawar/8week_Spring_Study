# StudyRoom API — Spring 백엔드 기본기 학습 기록

빈 Spring Boot 프로젝트에서 출발해 웹 요청 처리부터 데이터 접근, 트랜잭션, 인증, 테스트와 운영까지 단계적으로 구현하는 학습 저장소입니다.

완성된 결과만 모으기보다 **예측 → 구현·실행 → 결과 비교 → 오답 교정** 과정을 코드와 문서로 함께 남기고 있습니다. 이미 아는 CS 개념을 Spring 동작 원리와 연결하고, 학습한 내용은 +2일·+7일·+14일 간격으로 다시 인출합니다.

## 현재 진행 상황

현재 코드와 학습 기록으로 검증된 범위는 **Week A Day 4 — 웹 계층과 책임 분리**까지입니다.

| 단계 | 학습 주제 | 구현·학습 증거 | 상태 |
|---|---|---|---|
| Day 1 | HTTP 요청→응답 왕복, 상태 코드 | [코드 없는 인출 문제](app/study_docs/days/Day01_0725/quiz.md) · [예측과 실제 차이](app/study_docs/days/Day01_0725/explain-log.md) | 완료 |
| Day 2 | record DTO와 가변 Domain 분리 | [학습 문제](app/study_docs/days/Day02_0726/quiz.md) · [실수와 교정 기록](app/study_docs/days/Day02_0726/velog_post.md) | 완료 |
| Day 3 | Bean Validation과 전역 오류 처리 | [학습 문제](app/study_docs/days/Day03_0728/quiz.md) · [복습 인출 점검](app/study_docs/days/Day03_0728/velog_post_review.md) | 완료 |
| Day 4 | Controller·Service·Repository 책임 분리 | [진행 및 검증 기록](app/study_docs/days/Day04_0728/progress.md) · [독립 변형 회고](app/study_docs/days/Day04_0728/velog_post.md) | 완료 |
| Day 5 | IoC·DI, Bean, 생성자 주입 | 현재 코드의 `@Service`·`@Repository`와 생성자 주입을 실험 | 다음 학습 |
| Day 6 | Week A 누적 인출 시험 | [복습큐](app/study_docs/복습큐.md)와 Week A 전 범위 | 예정 |
| Week B | Flyway, JDBC, JPA, 영속성 컨텍스트 | [5주 로드맵](app/study_docs/FUNDAMENTALS_ROADMAP.md) | Week A 완료 후 진행 |

최근 완료한 Day 4에서는 `ReservationRepository` 인터페이스와 메모리 구현체를 분리하고, 예약 생성·취소 흐름을 Controller에서 Service로 옮겼습니다. 식별자 없이 특정 예약을 갱신할 수 없다는 문제를 직접 만나 `id`를 도입했으며, `Long`의 `==` 비교와 저장소 중복 저장 가능성도 오답 및 후속 실험 항목으로 기록했습니다.

## 학습 방식

매일 어려운 개념 하나를 다음 순서로 학습합니다.

1. 동작하는 완성 예제를 읽고 실행합니다.
2. 핵심 부분만 비운 예제를 완성합니다.
3. 요구사항을 조금 바꾼 기능을 독립적으로 구현합니다.
4. 노트와 코드를 덮고 원리를 설명합니다.
5. 실행 결과를 먼저 예측한 뒤 실제 결과와 차이를 기록합니다.
6. 오답을 교정하고 [복습큐](app/study_docs/복습큐.md)에 +2일·+7일·+14일 재시험을 등록합니다.

각 학습일에는 다음 네 종류의 증거를 남깁니다.

- `vocab.md`: 처음 만난 용어를 CS 지식과 연결한 설명
- `quiz.md`: 노트 없이 답하는 인출 문제와 실제 답변
- `explain-log.md`: 예측, 실행 결과, 예상과 달랐던 이유
- `velog_post.md`: 먼저 핵심 이론을 독립적으로 복습할 수 있게 설명하고, 이어서 설계 판단·검증·실수·한계를 정리한 기술 회고. 모든 글은 [기술 블로그 템플릿](app/study_docs/VELOg_POST_TEMPLATE.md)의 사실 검증 및 품질 기준을 따른다

## 현재 코드에서 확인할 수 있는 것

- `ReservationController`: HTTP 요청과 응답 처리
- `ReservationService`: 예약 생성·취소 규칙 조정
- `ReservationRepository`: 저장소 계약을 인터페이스로 분리
- `InMemoryReservationRepository`: 메모리 기반 저장과 식별자 부여
- `ReservationRequest`: `record` DTO와 Bean Validation
- `GlobalExceptionHandler`: 검증 오류의 공통 응답 처리

현재는 `web`, `validation` 의존성만 사용합니다. JPA·H2·Flyway는 데이터 접근 원리를 학습하는 Week B에서 직접 추가하고, Spring Security와 JWT는 Week D에서 추가합니다. 최종 기술 목록을 미리 넣어 완성된 것처럼 보이지 않도록 **현재 구현과 계획을 구분**했습니다.

## 학습 로드맵

| 주차 | 핵심 범위 |
|---|---|
| Week A | HTTP, DTO·Domain 분리, 검증·오류 처리, 계층 분리, IoC·DI |
| Week B | SQL, Flyway, JDBC, JPA, 영속성 컨텍스트와 변경 감지 |
| Week C | 트랜잭션, Spring AOP 프록시, 지연 로딩, N+1 |
| Week D | BCrypt, JWT, Security Filter Chain, 테스트 분류 |
| Week E | 로깅, 디버깅, Docker, CI, 누적 독립 과제 |

상세 일정과 완료 기준은 [백엔드 기본기 로드맵](app/study_docs/FUNDAMENTALS_ROADMAP.md)에 정리했습니다.

## 저장소 구조

```text
8week_Spring_Study/
├─ app/                         # 현재 직접 구현하는 Spring Boot 프로젝트
│  ├─ src/                      # 애플리케이션 코드와 테스트
│  └─ study_docs/
│     ├─ days/                  # 날짜별 단어장·퀴즈·설명 로그·회고
│     ├─ FUNDAMENTALS_ROADMAP.md
│     ├─ spring-core-notes.md
│     ├─ interview-notes.md
│     └─ 복습큐.md
├─ archive/                     # 이전 코드 동결본, 읽기 전용 참고
├─ week_review/                 # 이전 주차 복습 자료
└─ past_docs/                   # 폐기된 과거 계획 보관
```

`archive/`의 완성 코드는 정답을 복사하는 용도로 사용하지 않습니다. 현재 실습은 모두 `app/`에서 진행하며, 막힌 원인을 충분히 좁힌 뒤 필요한 부분만 참고합니다.

## 실행 및 검증

요구 사항은 Java 17입니다.

```bash
cd app
./gradlew test
./gradlew bootRun
```

학습일이 끝나면 코드, 퀴즈, 설명 로그와 복습큐를 함께 커밋해 구현 결과와 이해 과정을 같은 시점의 기록으로 남깁니다.
