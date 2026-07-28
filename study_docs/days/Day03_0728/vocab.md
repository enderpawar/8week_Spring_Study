# Day03 (7/28, Week A D3) 단어장 — 전역 오류처리 + Bean Validation

형식: **용어 | 한줄뜻 | CS 연결 | 코드 모습 | 연관 단어**

| 용어 | 한줄뜻 | CS 연결 | 코드 모습 | 연관 단어 |
|---|---|---|---|---|
| `@Valid` | 요청 DTO에 Bean Validation 검증을 실행하라고 컨트롤러에 지시 | 계약(Contract) 검사 — 잘못된 입력을 계층 초입에서 차단 | `reserve(@RequestBody @Valid ReservationRequest request)` | `@NotBlank`, `MethodArgumentNotValidException` |
| `@NotBlank` | 문자열이 null·빈 문자열·공백만인 경우 검증 실패 처리 | Precondition(사전조건) 검사 | `@NotBlank(message = "...") String roomName` | `@Valid`, Bean Validation |
| `MethodArgumentNotValidException` | `@Valid` 검증이 실패하면 Spring이 던지는 예외 | 예외를 통한 제어 흐름 분기 | `@ExceptionHandler(MethodArgumentNotValidException.class)` | `@Valid`, `BindingResult` |
| `BindingResult` / `getFieldErrors()` | 검증 실패한 필드들만 `FieldError` 객체로 담아 반환 — DTO 전체 필드가 아님 | "성공한 항목은 기록하지 않는" 로그 방식과 유사 | `ex.getBindingResult().getFieldErrors()` | `FieldError`, `@NotBlank` |
| `@RestControllerAdvice` | 특정 컨트롤러 지정 없이 애플리케이션 전역의 예외를 가로채는 컴포넌트 선언 | 관심사 분리(Cross-cutting concern) — 예외 처리를 각 컨트롤러에서 반복하지 않음 | `@RestControllerAdvice public class GlobalExceptionHandler { ... }` | `@ExceptionHandler`, `ExceptionHandlerExceptionResolver` |
| `ExceptionHandlerExceptionResolver` | `DispatcherServlet`이 예외 발생 시 위임하는 리졸버. 컨테이너에서 `@ControllerAdvice`류 Bean을 미리 수집해 전역 후보로 들고 있음 | "Bean으로 등록됨"과 "리졸버가 특별 취급함"은 별개 조건이라는 것 | (프레임워크 내부 동작, 직접 코드 작성 없음) | `@RestControllerAdvice`, DispatcherServlet |
