# Day02 (7/26, Week A D2) 단어장 — record DTO vs Domain 분리

형식: **용어 | 한줄뜻 | CS 연결 | 코드 모습 | 연관 단어**

| 용어 | 한줄뜻 | CS 연결 | 코드 모습 | 연관 단어 |
|---|---|---|---|---|
| record (DTO) | 데이터 모양만 정의하는 불변 타입. 생성자·접근자·equals·hashCode 자동 생성 | 불변 값 객체(immutable value object) — 스레드 세이프, 실수로 값 변경되는 버그 원천 차단 | `record ReservationRequest(String roomName, String requesterName) {}` | Domain, 캡슐화 |
| record 접근자 이름 규칙 | 필드 이름 그대로 (`roomName()`), JavaBean의 `getXxx()` 아님 | "getter가 있는 객체"가 아니라 "데이터 그 자체"라는 설계 의도 | `request.roomName()` (O), `request.getRoomName()` (X) | JavaBean 관례 |
| Domain 객체 | 상태 + 상태를 바꾸는 규칙(행동)을 함께 캡슐화하는 가변 객체 | OOP 캡슐화(encapsulation) 원칙 | `Reservation.confirm()`이 `confirmed` 필드를 내부에서만 바꿈 | record, 불변 |
| @RequestBody | 클라이언트가 보낸 JSON body를 자바 객체(record)로 자동 변환(역직렬화) | HTTP 요청 바디 파싱 — Jackson이 리플렉션으로 record 생성자 호출 | `reserve(@RequestBody ReservationRequest request)` | Jackson, 역직렬화 |
| 컴파일러가 못 잡는 오타 | 메서드명·URL 문자열은 정의/호출이 서로 일치하기만 하면 컴파일러가 통과시킴 — "영어로 맞는 철자인가"는 검사 대상이 아님 | 정적 타입 검사의 한계 — 문자열은 식별자가 아니라 데이터 | `"/rerservations/cancel"` 오타도 컴파일 성공 | 테스트, IntelliJ 철자검사 |
