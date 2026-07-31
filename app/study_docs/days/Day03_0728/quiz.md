# Day03 (7/28, Week A D3) 문제집 — 전역 오류처리 + Bean Validation

형식: 문제 → <details> 정답/해설 (접힘)

## Q1. `roomName`이 빈 문자열로 들어오면 요청이 컨트롤러 메서드 본문까지 도달하는가?

<details>
<summary>정답/해설</summary>

도달하지 않는다. `@Valid`가 `ReservationRequest`를 검증하다가 `@NotBlank` 위반을 발견하면 컨트롤러 메서드 본문이 실행되기 **전에** `MethodArgumentNotValidException`을 던진다. 이 예외는 `GlobalExceptionHandler`의 `handleValidation`이 잡아서 400과 함께 응답한다. `reserve()` 메서드 안의 `new Reservation(...)` 코드는 실행되지 않는다.
</details>

## Q2. `roomName`만 비우고 `requesterName`은 정상값을 보냈을 때, 응답 바디에 `requesterName`이 왜 안 나오는가?

<details>
<summary>정답/해설</summary>

`ex.getBindingResult().getFieldErrors()`는 DTO의 "모든 필드"를 순회하는 게 아니라 "검증에 **실패한** 필드에 대해서만 만들어진 `FieldError` 객체 목록"을 순회한다. `requesterName`은 `@NotBlank`를 통과했으므로 애초에 `FieldError`가 생성되지 않고, 그래서 `errors` Map에도 들어가지 않는다. (오늘 실수: "먼저 넣고 나중에 덮어쓴다"고 생각했는데, 애초에 목록에 없어서 안 들어간 것이다.)
</details>

## Q3. `GlobalExceptionHandler`는 `ReservationController`를 어디에도 명시적으로 지정하지 않는데, 어떻게 그 컨트롤러의 예외까지 잡는가?

<details>
<summary>정답/해설</summary>

Spring 컨테이너가 `@RestControllerAdvice`가 붙은 Bean을 일반 Bean과 별도로 수집해 전역 예외 처리 후보 목록으로 들고 있다가, `DispatcherServlet`이 예외를 만나면 `ExceptionHandlerExceptionResolver`가 그 목록을 돌면서 예외 타입이 맞는 `@ExceptionHandler` 메서드를 찾아 실행한다. "Bean으로 등록됐다"는 사실만으로는 부족하고, `@ControllerAdvice` 계열 애노테이션이 붙어 있어야 리졸버가 전역 후보로 특별 취급한다. (기본값이 "모든 컨트롤러 대상"인 것뿐이고, `basePackages`·`assignableTypes` 등으로 범위를 좁힐 수도 있다.)
</details>
