# Day07 (2026-08-02 완료, Week A D7) 문제집 — 저장 계약과 오류 경계

형식: 문제 → <details> 정답/해설

## Q1. 기존 ID를 가진 예약에도 `store.add()`를 호출하면 어떤 문제가 생기는가?

<details>
<summary>정답/해설</summary>

`ArrayList`는 같은 객체 참조의 중복 삽입을 막지 않는다. `findById()`로 꺼낸 예약을 취소한 뒤 다시 `add()`하면 목록에 같은 예약 참조가 한 칸 더 생긴다. 신규 예약은 추가하고 기존 ID는 해당 위치를 교체해야 저장소 크기와 식별자 불변식이 유지된다.
</details>

## Q2. `findById()`가 `null` 대신 `Optional<Reservation>`을 반환하면 Service의 책임은 무엇인가?

<details>
<summary>정답/해설</summary>

Repository는 값이 없을 수 있음을 반환형으로 표현한다. Service는 `orElseThrow()`로 그 부재를 “해당 예약 없음”이라는 업무 의미의 `ReservationNotFoundException`으로 바꾼다. HTTP 404 변환은 웹 경계인 `GlobalExceptionHandler`가 맡는다.
</details>

## Q3. 예상하지 못한 예외의 `getMessage()`를 500 응답에 그대로 보내면 왜 위험한가?

<details>
<summary>정답/해설</summary>

예외 메시지에는 내부 클래스, DB 정보, 파일 경로 같은 구현 세부가 포함될 수 있다. 상세 원인과 stack trace는 서버 로그에 남기고, 클라이언트에는 일반화한 오류 메시지만 반환한다.
</details>

## Q4. `/reservations/cancel/{id}`에서 `id=0`을 Service 전에 막는 흐름은?

<details>
<summary>정답/해설</summary>

Controller의 `@PathVariable` 인수에 `@Positive`를 선언한다. Spring MVC의 메서드 검증이 실패하면 `HandlerMethodValidationException`이 발생하고, `GlobalExceptionHandler`가 이를 400 응답으로 바꾼다.
</details>

## Q5. 단위 테스트에서 예외 처리기 메서드가 404를 반환한 것과 MockMvc로 실제 404를 확인한 것은 무엇이 다른가?

<details>
<summary>정답/해설</summary>

예외 처리기 단위 테스트는 Java 메서드의 반환 객체만 확인한다. MockMvc 테스트는 URL 매핑, Controller→Service 호출, 예외 전파, `@RestControllerAdvice` 선택, JSON 직렬화까지 Spring MVC 흐름을 함께 확인한다.
</details>

## 완료 판정

- 저장 중복 방지와 없는 ID 예외를 Service 단위 테스트로 고정했다.
- 404·안전한 500 응답을 예외 처리기 단위 테스트로 고정했다.
- 유효 요청 200, DTO 검증 400, 없는 예약 404, 경로 ID 검증 400을 MockMvc로 고정했다.
- 전체 테스트 10개가 통과했다.
