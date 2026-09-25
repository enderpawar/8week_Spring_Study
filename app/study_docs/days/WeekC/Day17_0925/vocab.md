# Day17 (9/25, Week C D3) 용어

주제: 트랜잭션 전파(`REQUIRED` / `REQUIRES_NEW`)

| 용어 | 한줄뜻 | 오늘 코드와 관찰 |
|---|---|---|
| 트랜잭션 전파(Propagation) | 이미 진행 중인 트랜잭션이 있을 때 새 트랜잭션 경계가 그것과 어떤 관계를 맺을지 정하는 규칙 | `@Transactional(propagation = ...)` |
| `REQUIRED`(기본값) | 진행 중인 트랜잭션이 있으면 새로 만들지 않고 그 트랜잭션에 합류, 없으면 새로 시작 | `PropagationInnerService.reserve()`가 처음엔 `REQUIRED`로 outer의 트랜잭션에 합류 |
| `REQUIRES_NEW` | 진행 중인 트랜잭션을 잠시 보류(suspend)하고 완전히 독립된 새 트랜잭션을 시작, 종료 후 원래 트랜잭션으로 복귀 | `reserve()`에 적용 후 outer 실패에도 예약이 살아남음 |
| 트랜잭션 합류 | 새 경계가 기존 트랜잭션 안에 흡수되어, 하나의 커밋/롤백 단위로 묶이는 것 | REQUIRED에서 outer 예외 → inner의 저장도 같이 롤백 |
| 트랜잭션 보류(suspend) | REQUIRES_NEW 시작 시 기존 트랜잭션을 잠깐 멈춰두는 것(끝나면 재개) | REQUIRES_NEW 동작의 전제 |
| 커넥션 풀 고갈 | 동시에 필요한 DB 커넥션 수가 풀 크기를 넘어서 요청이 커넥션을 기다리며 병목·정체되는 상태 | REQUIRES_NEW 실행 중엔 outer+inner용 커넥션 2개가 동시에 필요 |
| `@Import({A.class, B.class})` | 테스트 전용 static nested class 여러 개를 한 번에 Spring Bean으로 등록 | `TransactionPropagationTest`의 `PropagationOuterService`/`PropagationInnerService` |
| `Stream.noneMatch` / `anyMatch` | 조건을 만족하는 원소가 하나도 없는지 / 하나라도 있는지 확인 | `findAll().stream().anyMatch(r -> r.getRoomName().equals("D-101"))` |

## 핵심 호출 경로

```text
REQUIRED
outer() 트랜잭션 A 시작
→ inner() 호출, 이미 A가 있음 → 새로 안 만들고 A에 합류
→ outer()에서 예외 발생 → A 전체 롤백 (inner의 저장도 함께 취소)
```

```text
REQUIRES_NEW
outer() 트랜잭션 A 시작
→ inner() 호출, REQUIRES_NEW → A를 보류하고 새 트랜잭션 B 시작 (커넥션 2개 동시 사용)
→ inner()가 B를 커밋하고 종료 → A 재개
→ outer()에서 예외 발생 → A만 롤백, B는 이미 커밋되어 영향 없음
```
