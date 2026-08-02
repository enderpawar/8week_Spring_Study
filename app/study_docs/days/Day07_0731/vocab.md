# Day07 (2026-08-02 완료, Week A D7) 단어장 — 저장 계약과 안전한 취소

| 용어 | 한줄뜻 | CS 연결 | 코드 모습 | 연관 단어 |
|---|---|---|---|---|
| 저장 계약 | 신규·기존 객체를 저장할 때의 동작 약속 | 자료구조의 삽입과 갱신 | ID 없음은 `add`, 기존 ID는 `set` | 불변식 |
| `Optional` | 값의 부재 가능성을 반환형에 드러내는 컨테이너 | 합 타입과 명시적 상태 | `findById()` | `orElseThrow` |
| 도메인 예외 | 업무 의미가 있는 실패를 나타내는 예외 | 오류 상태 모델링 | `ReservationNotFoundException` | 404 |
| 메서드 검증 | Controller 인수에 직접 선언한 제약을 검사하는 MVC 처리 | 입력 경계와 불변식 | `@PathVariable @Positive Long id` | `HandlerMethodValidationException` |
| 오류 정보 분리 | 내부 원인과 외부 응답에 공개할 정보를 나누는 것 | 정보 은닉·보안 경계 | `log.error(..., ex)`와 일반 500 본문 | 공통 오류 DTO |
| MockMvc | 서버 포트를 열지 않고 Spring MVC 요청 흐름을 검증하는 도구 | 통합 경계 테스트 | `post(...).andExpect(...)` | 상태 코드, JSON |
