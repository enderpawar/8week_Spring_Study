# Day17 (9/25, Week C D3) 인출 기록

## 1. 복습큐 도래분 (6문항, 모두 통과)

| 개념 | 결과 |
|---|---|
| flush는 SQL 전송, commit은 최종 확정 | ✅ (오답재시험 통과) |
| `@Transactional(readOnly=true)`는 조회 의도 힌트, 쓰기 차단 보장 아님 | ✅ (오답재시험 통과) |
| `getBean()`은 Bean 생성 과정에서 준비된 같은 Singleton 프록시를 조회 | ✅ (힌트 후 통과) |
| ApplicationContext(Bean) vs 영속성 컨텍스트(`@Entity`) 관리 대상 구분 | ✅ (동일 혼동 재발 후 교정하여 통과) |
| 트랜잭션 경계 — 조회·상태변경·반영을 하나의 성공/취소 단위로 묶음 | ✅ |
| Spring AOP 프록시 — `@Transactional`은 메타데이터, `TransactionInterceptor`가 실제 처리 | ✅ |

## 2. 세션 예측과 교정 — 트랜잭션 전파

| # | 질문 | 학습자 답 | 판정 | 교정 |
|---|---|---|---|---|
| P1 | REQUIRED에서 outer 실패 시 inner가 저장한 예약이 남는가 | (코드로 직접 실행해 확인) | ✅ | `findAll()`이 비어있음을 확인 — inner도 같은 트랜잭션에 합류해 함께 롤백 |
| P2 | 왜 inner의 저장까지 롤백되는가 | "같은 트랜잭션 범위에 포함되서" | 방향은 맞으나 얕음 | REQUIRED가 "이미 트랜잭션이 있으면 합류"한다는 판단 로직까지 요구 |
| P3 | REQUIRED의 판단 로직을 "있으면 ___, 없으면 ___"로 | "있으면 롤백, 없으면 저장" | ❌ | 질문은 롤백/저장이 아니라 "새 트랜잭션을 만드는가"였음. REQUIRED=합류(안 만듦), REQUIRES_NEW=새로 만듦으로 정정 |
| P4 | `reserve()`를 REQUIRES_NEW로 바꾸면 "D-101" 예약이 남는가 | "남아있을 것. 트랜잭션 범위가 분리됐잖아" | ✅ | REQUIRES_NEW는 기존 트랜잭션을 보류하고 독립된 트랜잭션을 새로 시작하므로, outer 롤백과 무관하게 이미 커밋됨 |
| P5 | REQUIRES_NEW로 커밋된 것이 나중에도 되돌아가지 않는 이유 | "트랜잭션 경계가 다르다, 이미 커밋되어버렸으니 돌아가지 않는다" | ✅ | 정확 |
| P6 | REQUIRES_NEW 실행 중 동시에 필요한 커넥션 수와 위험 | "커넥션 2개, 풀이 꽉 차면 병목" | ✅ | 정확 (추가: 최악의 경우 대기 요청들이 서로 물려 교착까지 갈 수 있음) |

## 3. 디버깅 실측 (전체 스위트 실행 중 발견)

`outerFailureAffectsInnerReservation()`을 단독 실행하면 통과했지만 `./gradlew test` 전체 실행 시 `assertTrue(reservationRepository.findAll().isEmpty())`에서 실패했다. 원인: 다른 테스트(`ReservationServiceTransactionTest` 등)가 테스트 메서드 자체에 `@Transactional`이 없어 실제로 커밋한 예약 데이터가 같은 DB에 누적되어 있었기 때문. `findAll().isEmpty()`는 "DB 전체가 비어있다"는 잘못된 전제였고, `stream().noneMatch(r -> r.getRoomName().equals("D-101"))`처럼 이번에 만든 예약을 특정해서 확인하는 방식으로 교정했다.

## 4. 다음 복습 질문

1. REQUIRED의 판단 로직("있으면 합류, 없으면 새로 시작")을 REQUIRES_NEW와 대비해서 말하기
2. REQUIRES_NEW가 이미 커밋한 내용이 outer 롤백에 영향받지 않는 이유
3. REQUIRES_NEW 동시 실행 시 커넥션이 몇 개 필요하며 왜 위험한지
4. `findAll().isEmpty()` 같은 "전체 상태" assertion이 테스트 격리 문제를 왜 숨기는지

## 5. 복습 일정

Day17 완료일 9/25 기준, 오늘 새로 등록한 REQUIRED/REQUIRES_NEW 두 항목은 +2일 9/27에 먼저 인출한다. 복습큐 도래분 6건은 각각 +2 또는 +7로 복귀했다(상세는 `복습큐.md` 참고).
