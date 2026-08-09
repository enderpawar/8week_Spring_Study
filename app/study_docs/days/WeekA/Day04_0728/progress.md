# Day04 (7/28, Week A D4) — 완료 (7/31 세션에서 마무리)

> `cancel()` 독립 변형까지 끝났고, vocab/quiz/explain-log/velog/복습큐 모두 갱신됨. `./gradlew test` green.

## 오늘 주제
- Full: Service/Repository 책임 분리
- Light: 인터페이스 Repository

## 한 것 (전체)
- `ReservationRepository` 인터페이스 + `InMemoryReservationRepository` 구현체
- `ReservationService.reserve()` / `.cancel()` — Controller가 Service를 거치도록 완성
- `Reservation`에 `id` 필드(`assignId`/`getId`) 추가 — 식별자 도입
- `ReservationRepository.findById()` 추가, `InMemoryReservationRepository`가 `save()` 시 `id` 자동 부여
- `ReservationController.cancel()`을 `@PathVariable Long id` + `/reservations/cancel/{id}`로 변경, `reserve()` 응답에 id 노출
- 컴파일·테스트 green, `bootRun`으로 실제 reserve→cancel 흐름까지 curl로 검증 완료
- vocab.md, quiz.md, explain-log.md, velog_post.md 작성 / 복습큐.md 갱신

## 남겨둔 것 (의도적으로 미해결, 다음 기회에)
- `ReservationService.cancel()`이 `findById`로 찾은 뒤 `save()`를 다시 호출하는데, `InMemoryReservationRepository.save()`가 무조건 `store.add(...)`만 하기 때문에 store에 같은 객체 참조가 중복으로 들어갈 가능성이 있음. `findAll()`을 노출하는 엔드포인트가 없어서 지금은 확인 안 함 — Week B(영속성 컨텍스트·변경 감지)에서 자연스럽게 다시 만날 주제라 보류.
- IntelliJ Gradle 프로젝트 import 이슈(진짜 해결됐는지 미확인 — 다음 세션에서 확인 필요)

## 환경 이슈 (이전 기록, 재확인 필요)
- IntelliJ가 `app/`을 Gradle 프로젝트로 import 안 했던 문제. 해결법: `File → Close Project` → `File → Open...` → `app/` 폴더 선택 → Gradle sync 대기(JDK `ms-17`).
