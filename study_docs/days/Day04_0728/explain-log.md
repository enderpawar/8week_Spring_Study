# Day04 (7/28→7/31, Week A D4) 예측→실행→차이설명

형식: 개념 | 예측 | 실제 결과 | 차이 설명(왜 이렇게 동작하나)

| 개념 | 예측 | 실제 결과 | 차이 설명 |
|---|---|---|---|
| `cancel()`을 `reserve()`와 같은 패턴으로 Service에 옮긴 뒤 실행 | 기존에 저장돼 있던 예약을 찾아서 확정 상태만 `false`로 바뀔 것 | 새 예약이 하나 더 생김(기존 예약은 그대로 `confirmed: true`로 남음) | `ReservationService.cancel()`이 `findById` 없이 `new Reservation(...)`으로 완전히 새 객체를 만들어 저장했기 때문. `roomName`/`requesterName`이 같아도 자바 입장에선 다른 인스턴스이고, 저장소도 이를 구별할 식별자(id)가 없어 그냥 추가만 됨 — "식별자 필요성"을 몸으로 확인한 지점 |
| `Reservation`에 `id` 필드를 추가하고 `InMemoryReservationRepository.findById()` 작성 | (특별한 예측 없이 작성) | 컴파일 에러 2단계: ① `if(r.getId() == id)){` 괄호 개수 불일치 → `illegal start of expression` ② `for` 루프가 못 찾은 경우의 리턴문 누락 → `missing return statement` | 자바 컴파일러는 괄호 짝이 안 맞으면 그 지점부터 문법을 파싱할 수 없다고 판단한다. 두 번째는 메서드의 모든 실행 경로가 값을 리턴해야 한다는 규칙 — 루프가 끝까지 돌고도 못 찾은 경우를 처리하는 코드가 없으면 컴파일 자체가 안 된다 |
| `Controller.cancel()`에 `@PathVariable Long id`를 추가했지만 `@PostMapping` 경로에는 아직 `{id}`를 안 넣은 상태로 실행 | (컴파일이 통과했으니 정상 동작할 것) | `./gradlew compileJava`, `./gradlew test` 둘 다 성공했지만, 실제로 앱을 띄워 `POST /reservations/cancel`을 호출하면 500 에러: `Required URI template variable 'id' for method parameter type Long is not present` | `@PathVariable`과 URL 템플릿의 불일치는 컴파일 타임 문법 오류가 아니라, 실제 요청이 들어와 Spring이 URI에서 값을 채우려 할 때 발생하는 런타임 오류다. 컴파일·테스트가 초록불이어도 "요청이 실제로 그 경로를 타야만" 드러나는 버그가 있다는 것을 확인 |
