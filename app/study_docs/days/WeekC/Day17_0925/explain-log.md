# Day17 (9/25, Week C D3) 예측→실행→차이 기록

주제: 트랜잭션 전파 REQUIRED와 REQUIRES_NEW

## 실험 1 — REQUIRED: outer 실패가 inner의 저장에 미치는 영향

- 코드: `PropagationOuterService.reserveThenFail()`(`@Transactional`)이 `PropagationInnerService.reserve()`(`@Transactional`, propagation 미지정=REQUIRED)를 호출한 뒤 예외를 던짐
- 예측: 같은 트랜잭션에 합류하므로 inner의 저장도 같이 롤백될 것
- 실행: `assertThrows`로 예외 확인 통과, `reservationRepository.findAll()`에 "D-101" 예약 없음 확인
- 교정 필요했던 부분: 처음엔 `findAll().isEmpty()`로 검증했는데, 전체 테스트 스위트로 돌리면 다른 테스트가 커밋해둔 데이터 때문에 실패했다. "DB 전체가 비어있다"가 아니라 "내가 만든 이 예약이 없다"로 assertion을 좁혀야 했다 (`stream().noneMatch(r -> r.getRoomName().equals("D-101"))`).

## 실험 2 — REQUIRES_NEW로 전환

- 변경: `reserve()`에 `@Transactional(propagation = Propagation.REQUIRES_NEW)` 적용
- 예측: outer가 실패해도 inner의 저장은 독립된 트랜잭션이라 남아있을 것
- 실행: `reservationRepository.findAll().stream().anyMatch(r -> r.getRoomName().equals("D-101"))`가 `true` — 예측대로 살아남음
- 부작용: 같은 `reserve()`를 공유하던 기존 REQUIRED 테스트(`outerFailureAffectsInnerReservation`)가 이제 반대 결과를 주장하게 되어 깨짐 → 중복 제거하고 테스트 하나(`requiresNewSurvivesOuterFailure`)로 정리, 주석도 REQUIRED 기준 설명에서 REQUIRES_NEW 기준으로 갱신

## 판단 로직 교정 과정

- 1차 답: "같은 트랜잭션 범위에 포함되서" — 방향은 맞지만 REQUIRED가 왜 같은 범위에 들어가는지 메커니즘 설명 없음
- 2차 답(오답): "트랜잭션이 있으면 롤백, 없으면 저장" — REQUIRED의 판단 기준을 "새 트랜잭션을 만드는가"가 아니라 "롤백/저장 여부"로 잘못 이해
- 교정: REQUIRED = 있으면 합류(새로 안 만듦) / 없으면 새로 시작. REQUIRES_NEW = 있어도 무시하고 항상 새로 시작(기존 것은 잠깐 보류)

## 검증 근거

- `src/test/java/com/example/studyroom/service/TransactionPropagationTest.java`
- `./gradlew test` 전체 스위트, BUILD SUCCESSFUL 재확인 (REQUIRED 검증 단계, 격리 버그 수정 단계, REQUIRES_NEW 전환 단계 각각 실행)

## [직접 작성] 오늘 배운 것을 내 문장으로

- REQUIRED와 REQUIRES_NEW의 판단 로직 차이:
- REQUIRES_NEW가 위험할 수 있는 이유 (커넥션 관점):

## 다음 시작점

Week C D4 — 연관관계 + Hibernate LAZY 프록시.
