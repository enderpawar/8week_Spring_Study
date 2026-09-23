# 제목·목차 핵심 개념어 영문 표기 명세 (2026-09-23, v2)

> v1(`한국어(English)` 병기)은 사용자 의도와 달라 폐기한다. v2는 핵심 개념어를 **영어 명칭으로 대체**한다.

## 1. 목적

Velog 글의 제목(H1)과 목차(H2·H3·H4)에서 핵심 개념어를 업계 표준 영어 명칭으로 쓴다. 채용 담당자가 목차만 봐도 작성자가 개념의 공식 이름으로 사고한다는 것을 알 수 있게 하는 것이 목적이다.

```text
이전: [백엔드 기본기 Day5] 제어의 역전과 의존성 주입 — 생성자 주입과 싱글톤 Bean
이후: [백엔드 기본기 Day5] IoC와 DI — Constructor Injection과 Singleton Bean
```

## 2. 표기 규칙

1. **괄호 병기를 쓰지 않는다.** 아래 용어집의 한국어 개념어는 제목·목차에서 영어 표기로 **대체**한다. v1에서 넣은 `한국어(English)` 형태는 모두 `English`만 남기고 정리한다.
2. **모든 목차에 일관되게 적용한다.** v1의 "처음 나오는 곳에만" 규칙은 폐지한다. 같은 개념은 글 전체의 모든 목차에서 같은 영어 표기로 쓴다.
3. **조사는 영어 발음에 맞춘다.** 받침 발음이 있으면 `과/은/을/이`, 없으면 `와/는/를/가`를 쓴다.
   - `DI와`, `IoC와`, `DTO와`, `ORM과`, `DIP와`, `SRP와`
   - `Constructor Injection과`, `Singleton Bean과`, `Dirty Checking과`, `Persistence Context와`, `First-Level Cache와`, `Checksum과`, `Transaction Boundary와`
4. 그 밖의 설명어는 한국어로 둔다. 번역하지 않는 예: `저장 계약`, `오답 교정`, `버퍼`, `누적시험`, `필요성`, `동작 순서`, `검증 범위`, `경계`, `구분`
5. **이미 영어인 용어는 그대로 둔다.** 예: `flush`, `commit`, `@Transactional`, `ResponseEntity`, `record`, `Bean`, `JDBC`, `CHECK`
6. 명사형 목차 규칙과 의미는 유지한다. 개념어를 영어로 바꾸는 것과 그에 따른 조사 조정 외에는 문구를 바꾸지 않는다.
7. **대상은 `#`로 시작하는 줄뿐이다.** 본문, 표, 코드, 대체텍스트, 주석은 수정하지 않는다.
8. **목차가 영어 명사만 나열된 문장이 되지 않게 한다.** 한 목차에서 영어로 바꾸는 개념어는 핵심어 2~3개까지로 한다.

## 3. 표준 용어집 (이 표기를 강제한다)

| 한국어 | 영어 표기 |
|---|---|
| 제어의 역전 | IoC |
| 의존성 주입 | DI |
| 의존성 역전 원칙 / 의존성 역전 | DIP |
| 단일 책임 원칙 | SRP |
| 생성자 주입 | Constructor Injection |
| 싱글톤 Bean / 싱글톤 스코프 | Singleton Bean / Singleton Scope |
| 무상태 | Stateless |
| 책임 분리 / 관심사 분리 | Separation of Concerns |
| 요청과 응답 | Request-Response |
| 상태코드 / HTTP 상태 코드 | HTTP Status Code |
| 데이터 전송 객체 | DTO |
| 도메인 객체 / 도메인 모델 | Domain Model |
| 역직렬화 | Deserialization |
| 캡슐화 | Encapsulation |
| 입력 검증 | Input Validation |
| 메서드 검증 | Method Validation |
| 전역 오류 처리 / 전역 예외 처리 | Global Exception Handling |
| 전역 예외 처리기 | Global Exception Handler |
| 요청 매핑 | Request Mapping |
| 경로 변수 | Path Variable |
| URI 템플릿 변수 | URI Template Variable |
| 식별자 | Identifier |
| 참조 동일성 | Reference Equality |
| 값 동일성 / 값 동등성 / 동등성 | Value Equality |
| 값 부재 | Absence of Value |
| 컴파일 시점 / 실행 시점 | Compile Time / Runtime |
| 이름 공간 | Namespace |
| 단위 테스트 / 통합 테스트 | Unit Test / Integration Test |
| 스키마 버전 관리 | Schema Migration |
| 마이그레이션 | Migration |
| 체크섬 | Checksum |
| DB 제약 / 무결성 제약 | Integrity Constraint |
| CHECK 제약 | CHECK Constraint |
| 네이밍 전략 | Naming Strategy |
| 관계형 DB 접근 | Relational Data Access |
| 커넥션 풀 | Connection Pool |
| 파라미터 바인딩 | Parameter Binding |
| 커서 | Cursor |
| 객체-관계 매핑 | ORM |
| 영속성 컨텍스트 | Persistence Context |
| 관리 상태 | Managed State |
| 1차 캐시 | First-Level Cache |
| 엔티티 동일성 | Entity Identity |
| 변경 감지 | Dirty Checking |
| 스냅샷 / 로드 스냅샷 | Snapshot |
| 트랜잭션 경계 | Transaction Boundary |
| 원자성 | Atomicity |
| 트랜잭션 전파 | Transaction Propagation |
| AOP 프록시 / 프록시 | AOP Proxy / Proxy |

용어집에 없는 개념어는 Spring·Hibernate·Java 공식 문서의 명칭이 확실할 때만 영어로 바꾼다. 확신이 없으면 한국어로 둔다.

## 4. 작업 분담

| 담당 | 대상 파일 (`app/study_docs/` 기준) |
|---|---|
| A | `days/WeekA/Day01_0725` ~ `Day05_0729`의 `velog_post.md` |
| B | `days/WeekA/Day06_0730`, `Day07_0731`, `days/WeekB/Day08_0801` ~ `Day10_0807`의 `velog_post.md` |
| C | `days/WeekB/Day11_0822` ~ `Day14_0822`, `days/WeekC/Day15_0920`, `Day16_0920`의 `velog_post.md`, `velog/week-a-identity-storage-error-boundary.md`, `velog/week-b-persistence-context-and-dirty-checking.md` |

## 5. 금지 사항

- `#`로 시작하지 않는 줄은 수정하지 않는다. 담당 외 파일을 수정하지 않는다. 커밋하지 않는다.

## 6. 완료 보고

1. 파일별로 바꾼 목차의 전후 목록 (`이전 → 이후`)
2. 용어집 밖에서 영어로 바꾼 용어와 그 근거
3. 편집 후 담당 파일에 괄호 병기 `한국어(English)` 형태가 남지 않았다는 확인
