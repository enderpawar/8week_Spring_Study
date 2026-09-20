# Day15 (9/20, Week C D1) 예측→실행→차이 기록

주제: Service 트랜잭션 경계에서 commit과 rollback 관찰

## 실험 1 — 정상 반환하면 변경 감지가 commit되는가

- 코드: `ReservationService.cancel()`에 `@Transactional` 적용, 명시적 `repository.save()` 제거
- 예측: 테스트가 통과하고 `UPDATE reservation`이 출력됨
- 실행: `cancelCommitsChangedState()` 통과. `INSERT → SELECT → UPDATE → SELECT` 확인
- 판정: ✅ 결론 일치. 다만 `@Transactional`이 변경 객체를 영속성 컨텍스트에 넣는다는 표현은 교정했다. 조회 시 반환된 Entity가 영속 상태이고, flush 때 스냅샷과 현재 상태를 비교한다.

## 실험 2 — flush 전 예외

- 코드: 영속 Entity에 `cancel("강제 실패")`를 호출한 직후 `RuntimeException` 발생
- 실행: `INSERT → SELECT → ROLLBACK → SELECT`; `UPDATE`는 출력되지 않음
- 결과: 재조회한 값은 `confirmed=true`, `cancelReason=null`
- 차이 교정: 컬럼 반영 여부가 아니라 예외가 자동 flush보다 먼저 발생했기 때문에 변경 SQL이 전송되지 않았다.

## 실험 3 — UPDATE 후 rollback

- 코드: `cancel()` 뒤 `entityManager.flush()`로 `UPDATE`를 강제하고 그 다음 `RuntimeException` 발생
- 예측: 학습자는 `UPDATE`가 나오지 않을 것으로 예측
- 실행: `INSERT → SELECT → UPDATE → ROLLBACK → SELECT`. `runtimeExceptionRollsBackChangedState()` 통과
- 결과: SQL은 실행됐지만 commit되지 않았으므로 재조회 값은 `confirmed=true`, `cancelReason=null`
- 핵심: **flush는 SQL 전송이고 commit은 최종 확정이다.** SQL 로그에 보였다는 사실만으로 commit됐다고 판단할 수 없다.

## 검증 근거

- `src/test/java/com/example/studyroom/service/ReservationServiceTransactionTest.java`
- 대상 테스트 2개: failures 0, errors 0
- 강제 flush 롤백 테스트 단독 실행: `BUILD SUCCESSFUL`

## [직접 작성] 오늘 배운 것을 내 문장으로

- 트랜잭션 경계를 Service 메서드에 두는 이유:
- SQL의 `UPDATE` 출력과 commit이 같은 뜻이 아닌 이유:

## 다음 시작점

Week C D2 — Spring AOP 프록시와 self-invocation 관찰.
