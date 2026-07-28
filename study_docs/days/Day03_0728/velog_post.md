# [백엔드 기본기 Day3] 전역 오류처리 @RestControllerAdvice / Bean Validation @Valid

> Day3는 요청 값이 잘못 왔을 때 그걸 어떻게 400으로 되돌려줄지를 다뤘다. `@Valid`로 검증을 걸고 `@RestControllerAdvice`로 전역 예외 처리를 붙이는 것 자체는 짧은 개념이었는데, 혼자 예외 처리 클래스를 작성하다가 문법 실수를 세 개나 겹쳐서 내고, 응답 바디를 잘못 예측하기까지 하면서 오히려 개념이 더 또렷해진 하루였다.

## 1. 완성예제 — DTO에 검증 애노테이션, 컨트롤러에 @Valid

먼저 DTO의 각 필드에 검증 규칙을 붙였다.

```java
public record ReservationRequest(
    @NotBlank(message = "방 이름은 비어있을 수 없습니다") String roomName,
    @NotBlank(message = "예약자 이름은 비어있을 수 없습니다.") String requesterName
) {}
```

그리고 컨트롤러 파라미터에 `@Valid`를 붙여서 이 검증이 실제로 실행되게 했다.

```java
@PostMapping("/reservations")
public String reserve(@RequestBody @Valid ReservationRequest request) {
    Reservation reservation = new Reservation(request.roomName(), request.requesterName());
    reservation.confirm();
    return reservation.getRequesterName() + "님이 " + reservation.getRoomName()
        + " 예약 완료 (확정: " + reservation.isConfirmed() + ")";
}
```

`@Valid`가 검증에 실패하면 `reserve()` 메서드 본문은 아예 실행되지 않고, `MethodArgumentNotValidException`이 던져진다. 이 예외를 받아서 400으로 바꿔주는 역할이 `@RestControllerAdvice`가 붙은 전역 예외 처리 클래스다.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
```

이 클래스에는 `ReservationController`를 가리키는 표시가 어디에도 없다. 그런데도 모든 컨트롤러의 검증 실패를 잡아낸다는 게 오늘 배운 "전역"의 의미였다.

## 2. 독립 작성 중 문법 실수 세 개

`GlobalExceptionHandler`를 혼자 작성하다가 컴파일러가 한 번에 11개짜리 에러를 뱉었다. 원인을 뜯어보니 실제로는 세 군데였다.

```text
error: ')' or ',' expected
    public ResponseE ntity<Map<String, String>> handleValidation(...) {
```

`ResponseEntity` 사이에 실수로 공백이 들어가 있었다. 자바 파서는 이걸 `ResponseE`와 `ntity`라는 서로 다른 토큰 두 개로 읽어버려서 타입 선언 자체가 깨졌다.

```text
error: illegal start of expression
    Map<String, String> errors = newLinkedHashMap<>();
```

`new LinkedHashMap<>()`에서 `new`와 `LinkedHashMap` 사이 공백을 빼먹었다. `newLinkedHashMap`이라는 존재하지 않는 식별자로 읽혀서 생성자 호출 자체가 성립하지 않았다.

세 번째는 좀 더 구조적인 실수였다. `Exception` 전체를 처리하는 두 번째 메서드를 추가하려다가, 클래스를 닫는 `}` 뒤에 그대로 이어 붙였다.

```java
    }
}

@ExceptionHandler(Exception.class)
public ResponseEntity<Map<String, String>> handleUnexpected(Exception ex) {
    ...
}
```

클래스의 `}`가 이미 클래스 범위를 닫아버린 뒤라, 그 아래는 파일 최상위(top-level)였다. 최상위에는 class·interface·enum·record만 올 수 있어서 메서드 선언이 그 자리에 있을 수 없었다. 두 번째 메서드를 클래스의 `}` 안쪽으로 옮기고 나서야 빌드가 통과됐다.

## 3. 예측이 틀렸던 지점 — 응답 바디에 안 나온 필드

`roomName`은 빈 문자열, `requesterName`은 정상값으로 요청을 보내면 바디에 두 필드가 다 어떤 형태로든 나올 거라고 예측했다. 실제 결과는 달랐다.

```json
{"roomName":"방 이름은 비어있을 수 없습니다"}
```

`requesterName`은 아예 나타나지 않았다. 처음엔 "먼저 값을 넣었다가 나중에 덮어써서 사라진 것"이라고 생각했는데, 틀린 설명이었다. `ex.getBindingResult().getFieldErrors()`는 DTO의 모든 필드를 도는 게 아니라, **검증에 실패한 필드에 대해서만** `FieldError` 객체를 담고 있는 목록이다. `requesterName`은 `@NotBlank`를 통과했기 때문에 애초에 그 목록에 들어가지 않았고, 그래서 `forEach`가 순회할 대상 자체가 없었던 것이다.

## 스스로 묻고 답한 질문들

### Q. `roomName`이 빈 문자열로 들어오면 요청이 컨트롤러 메서드 본문까지 도달하는가?

도달하지 않는다. `@Valid`가 `@NotBlank` 위반을 발견하면 `reserve()` 메서드 본문이 실행되기 전에 `MethodArgumentNotValidException`을 던진다. `new Reservation(...)` 코드는 아예 실행되지 않고, `GlobalExceptionHandler`가 그 예외를 받아 400으로 응답한다.

### Q. `GlobalExceptionHandler`는 `ReservationController`를 어디에도 명시적으로 지정하지 않는데, 어떻게 그 컨트롤러의 예외까지 잡는가?

Spring 컨테이너는 `@RestControllerAdvice`가 붙은 Bean을 일반 Bean과 별도로 수집해서 전역 예외 처리 후보 목록으로 들고 있다. `DispatcherServlet`이 요청 처리 중 예외를 만나면 `ExceptionHandlerExceptionResolver`가 이 목록을 돌면서 예외 타입이 맞는 `@ExceptionHandler` 메서드를 찾아 실행한다. "Bean으로 등록돼서" 잡히는 게 아니라, "Bean이면서 동시에 `@ControllerAdvice` 계열 애노테이션이 붙어 있어서" 리졸버가 특별 취급하는 것이다. 기본값이 "모든 컨트롤러 대상"인 것뿐이고, `basePackages`나 `assignableTypes` 같은 옵션으로 범위를 좁힐 수도 있다.

## 정리하며

오늘의 핵심은 "`@Valid`로 검증하고, `@RestControllerAdvice`로 전역에서 400을 만든다"라는 한 줄이었지만, 그 한 줄을 코드로 옮기면서 자바 파서가 토큰을 어떻게 읽는지, 클래스 경계 밖에 메서드를 둘 수 없는 이유, 그리고 `getFieldErrors()`가 실패한 필드만 담는다는 사실까지 세 가지를 덤으로 배웠다.

---

오늘 공부한 소스코드: [8week_Spring_Study/app](https://github.com/enderpawar/8week_Spring_Study/tree/master/app)
