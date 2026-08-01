# [백엔드 기본기 Day3] @Valid와 @RestControllerAdvice — 잘못된 요청이 400이 되기까지

Day2에서 만든 `POST /reservations`는 `roomName`이 빈 문자열로 들어와도 그대로 예약을 만들어줬다. Day3는 그 요청을 어디에서 막고, 막힌 사실을 어떤 응답으로 되돌려줄지를 다룬다. 검증 규칙은 `@NotBlank` 하나만 쓰고, 오류 응답 포맷 설계나 커스텀 예외 계층은 범위 밖으로 뒀다.

> DTO 필드에 `@NotBlank`를 걸고 컨트롤러 파라미터에 `@Valid`를 붙인 뒤, `@RestControllerAdvice` 클래스에서 `MethodArgumentNotValidException`을 400으로 바꿨다. 빈 값을 넣어 요청해보니 응답 바디에 실패한 필드 하나만 담겨 나왔는데, 이걸 "Map이 덮어써져서"라고 잘못 설명했다가 `getFieldErrors()`가 애초에 실패한 필드만 담는다는 걸로 교정했다. 확인은 전부 `curl` 수동 호출이다.

## 1. 개념 설명

오늘 정리한 용어부터.

| 용어 | 한줄뜻 | 코드 모습 |
|---|---|---|
| `@Valid` | 요청 DTO에 Bean Validation 검증을 실행하라고 컨트롤러에 지시 | `reserve(@RequestBody @Valid ReservationRequest request)` |
| `@NotBlank` | 문자열이 null·빈 문자열·공백만이면 검증 실패 처리 | `@NotBlank(message = "...") String roomName` |
| `MethodArgumentNotValidException` | `@Valid` 검증이 실패하면 Spring이 던지는 예외 | `@ExceptionHandler(MethodArgumentNotValidException.class)` |
| `BindingResult` / `getFieldErrors()` | 검증에 실패한 필드만 `FieldError` 객체로 담아 반환 | `ex.getBindingResult().getFieldErrors()` |
| `@RestControllerAdvice` | 특정 컨트롤러를 지정하지 않고 전역의 예외를 가로채는 컴포넌트 선언 | `@RestControllerAdvice public class GlobalExceptionHandler { ... }` |
| `ExceptionHandlerExceptionResolver` | `DispatcherServlet`이 예외 발생 시 위임하는 리졸버. `@ControllerAdvice` 계열 Bean을 미리 수집해 전역 후보로 들고 있음 | 프레임워크 내부 동작 — 직접 작성하는 코드는 없다 |

이 여섯 개는 요청 하나가 400이 되는 한 줄기 흐름 위에 순서대로 놓인다.

> `@Valid`가 검증을 **실행시키고** → `@NotBlank`가 위반을 **판정하고** → `MethodArgumentNotValidException`이 그 사실을 **예외로 옮기고** → `ExceptionHandlerExceptionResolver`가 처리할 곳을 **찾아주고** → `@RestControllerAdvice`의 핸들러가 `BindingResult`에서 **실패 목록을 꺼내** 400 본문으로 만든다.

여기서 역할이 갈리는 지점이 두 군데 있다. 하나는 `@NotBlank`와 `@Valid`의 관계다. **`@NotBlank`는 DTO에 붙은 규칙 선언일 뿐이고, 그 규칙을 실제로 돌리라고 지시하는 건 컨트롤러 파라미터의 `@Valid`다.** 실제로 import 경로부터 다르다 — `@Valid`는 `jakarta.validation.Valid`, `@NotBlank`는 `jakarta.validation.constraints.NotBlank`다.

다른 하나는 "Bean으로 등록됨"과 "리졸버가 전역 후보로 취급함"이 별개 조건이라는 것이다. `@Service`나 `@Component`도 컨테이너에 올라간 Bean이지만 거기에 `@ExceptionHandler` 메서드를 둔다고 다른 컨트롤러의 예외까지 잡아주지는 않는다. 이 구분은 아래 자문자답에서 다시 다뤘다.

마지막으로 `BindingResult`는 "검증 결과 보고서"이지 "DTO의 사본"이 아니다. 통과한 필드는 기록되지 않으므로, 응답 바디의 길이는 DTO 필드 수가 아니라 **위반 개수**를 따라간다.

![시퀀스 다이어그램. 클라이언트가 roomName이 빈 문자열인 JSON을 POST하면 DispatcherServlet이 자기 자신에게 @RequestBody 변환과 @Valid 검사를 수행한다. alt 프레임의 첫 분기(검증 통과)에서는 ReservationController.reserve()가 호출되고 200 OK가 돌아간다. 두 번째 분기(검증 실패, MethodArgumentNotValidException)에서는 GlobalExceptionHandler.handleValidation()이 호출돼 400과 roomName 키만 담긴 Map이 반환되는데, 이 분기에서 ReservationController 생명선은 한 번도 닿지 않는다.](../../assets/day03-validation-flow.png)

> **더 볼 것**
> - [Exceptions — Spring Framework Reference](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-exceptionhandler.html): `@ExceptionHandler` 지원이 `DispatcherServlet`의 `HandlerExceptionResolver` 위에 있다는 근거
> - [Controller Advice — Spring Framework Reference](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-advice.html): `basePackages`·`assignableTypes`로 적용 범위를 좁히는 방법
> - [Validation — Spring MVC Config](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-config/validation.html): Bean Validation 구현체가 클래스패스에 있으면 글로벌 `Validator`가 등록된다는 부분
> - 아직 안 본 것 — `FieldError`의 나머지 정보(거부된 값, 코드), `@Validated`, `ProblemDetail` 형식의 오류 응답

## 2. 코드 구현

### 규칙은 DTO에, 실행 지시는 컨트롤러에

먼저 `record` DTO의 각 필드에 규칙과 메시지를 붙였다.

```java
public record ReservationRequest(
    @NotBlank(message = "방 이름은 비어있을 수 없습니다") String roomName,
    @NotBlank(message = "예약자 이름은 비어있을 수 없습니다.") String requesterName
) {}
```

`message`에 적은 문자열은 나중에 `error.getDefaultMessage()`로 꺼내져 그대로 응답 본문의 값이 된다. 즉 이 자리는 개발자 메모가 아니라 **클라이언트가 실제로 보게 될 문구**다.

그리고 컨트롤러 파라미터에 `@Valid`를 붙였다. 이때 소스에 예측을 주석으로 적어뒀다.

```java
@PostMapping("/reservations")
public String reserve(@RequestBody @Valid ReservationRequest request) { ... }
//아마 "" 로 해버리면 @Valid 유효성 검사 들어가서 400 BadRequest 뜨지 않을까 싶은데..
```

같은 파일의 `cancel()`은 같은 DTO를 받으면서 `@RequestBody`만 붙어 있다. 검증은 타입이 아니라 **파라미터 선언에** 걸리는 것이라, 같은 DTO여도 경로마다 검증 여부가 달라진다는 게 코드에 그대로 남았다.

### 어떤 컨트롤러도 가리키지 않는 예외 처리 클래스

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(Exception ex) { ... }
}
```

이 클래스 안에는 `ReservationController`를 가리키는 표시가 하나도 없다. 그런데도 그 컨트롤러의 검증 실패를 잡아낸다는 것이 오늘 배운 "전역"의 실체였다.

이 클래스를 혼자 작성하다가 컴파일 에러가 11개 났는데, 원인은 세 군데였다. 타입 이름 중간에 들어간 공백(`ResponseE ntity`), `new`와 클래스 이름이 붙어버린 `newLinkedHashMap<>()`, 그리고 두 번째 `@ExceptionHandler` 메서드를 클래스 닫는 `}` **뒤**에 쓴 것이다. 앞의 둘은 오타지만 세 번째는 규칙 문제였다 — 클래스를 닫은 뒤는 파일 최상위라서 class·interface·enum·record만 올 수 있고, 메서드 선언이 놓일 자리가 아니다.

### 오늘 확인한 것

앱을 재시작한 뒤 `roomName`은 빈 문자열, `requesterName`은 `"김민준"`으로 `POST /reservations`를 호출했다.

| 확인 항목 | 결과 |
|---|---|
| 상태코드 | `400 Bad Request` |
| 응답 바디 | `{"roomName":"방 이름은 비어있을 수 없습니다"}` |
| 컨트롤러 본문 도달 여부 | 도달하지 않음 (`new Reservation(...)` 미실행) |

전부 `curl` 수동 확인이다. 지금 테스트는 `contextLoads()` 하나여서 이 상태코드와 본문을 자동으로 검증해주지는 않는다. 오늘 코드는 [`306100f` 커밋](https://github.com/enderpawar/8week_Spring_Study/commit/306100f660de477643481eea8debd0a8b5de4e84)에 있다.

## 3. 스스로 답한 질문

### Q. `requesterName`은 왜 응답 바디에 나타나지 않았을까?

두 필드가 어떤 형태로든 다 나올 거라고 예측했는데 `roomName` 하나만 나왔다. 처음 내놓은 설명은 **"먼저 들어갔다가 덮어써져서 사라졌다"**였다. 틀린 방향이었다. 그 설명이 맞으려면 `forEach`가 두 필드를 모두 순회하면서 같은 키에 값을 다시 넣어야 하는데, `errors.put(error.getField(), ...)`의 키는 필드 이름이라 서로 다른 키다.

교정된 답은 순회 대상 자체가 달랐다는 것이다. `getFieldErrors()`는 DTO의 모든 필드를 도는 게 아니라 **검증에 실패한 필드에 대해 만들어진 `FieldError` 목록**을 돌려준다. `requesterName`은 `@NotBlank`를 통과했으므로 `FieldError`가 생성되지 않았고, `forEach`가 만날 원소 자체가 없었다.

재발 방지로 얻은 기준은 이렇다. **비어 있는 결과를 봤을 때 "지워졌나"부터 의심하지 말고 "애초에 들어갔나"를 먼저 확인한다.** 덮어쓰기가 아예 없는 얘기는 아니어서, 한 필드에 위반이 여러 개 걸리면 같은 키에 값이 다시 들어가 실제로 덮어써질 수 있다. 다만 오늘 관찰한 현상의 원인은 그게 아니었다.

### Q. `GlobalExceptionHandler`는 `ReservationController`를 어디에도 지정하지 않는데, 어떻게 그 예외까지 잡는가?

처음 답은 **"Spring Bean 관리를 통해서"** 잡는다는 것이었다. 부분 정답이었다. Bean 등록은 필요조건이지만 그것만으로는 부족하다는 게 빠져 있었고, `@Service`나 `@Component`로 등록된 Bean은 전역 예외 처리에 참여하지 않는다는 반례로 바로 확인된다.

교정된 답은 특별 취급의 주체를 짚는 쪽이다. `DispatcherServlet`이 예외를 만나면 `ExceptionHandlerExceptionResolver`에 위임하고, 이 리졸버는 컨테이너에서 **`@ControllerAdvice` 계열 애노테이션이 붙은 Bean만** 따로 수집해 전역 후보 목록으로 들고 있다가, 예외 타입이 맞는 `@ExceptionHandler` 메서드를 찾아 실행한다. "모든 컨트롤러 대상"은 이 애노테이션의 기본값일 뿐이고, `basePackages`나 `assignableTypes`로 범위를 좁힐 수도 있다.

### Q. 검증에 실패한 요청이 컨트롤러 메서드 본문까지 도달하는가?

`@Valid`를 붙이면서 소스 주석에 "400 BadRequest 뜨지 않을까"라고만 적어뒀는데, 정작 언제 멈추는지는 안 적었다. 도달하지 않는다. `@Valid`가 `@NotBlank` 위반을 발견하면 메서드 본문이 실행되기 **전에** `MethodArgumentNotValidException`이 던져지므로, `new Reservation(...)`도 `confirm()`도 실행되지 않는다. 잘못된 값이 도메인 객체로 옮겨가기 전에 계층 초입에서 끊긴다는 뜻이다.

## 4. 정리하며

오늘 바뀐 건 "검증을 어떻게 켜는가"보다 **검증 결과를 무엇으로 보는가**였다. 처음엔 `BindingResult`를 DTO의 사본처럼 생각해서 모든 필드가 어떤 형태로든 담겨 있을 거라고 봤는데, 실제로는 위반 사실만 적힌 보고서였다. 응답 바디가 짧았던 이유도, `@RestControllerAdvice`가 전역인 이유도 결국 "누가 무엇을 모아두는가"를 묻는 같은 종류의 질문이었다.

남은 것 둘. `@ExceptionHandler(Exception.class)`가 `ex.getMessage()`를 그대로 500 본문에 실어 보내는데, 예상 못 한 예외가 터지면 내부 구현 정보가 클라이언트에 노출될 수 있다. 외부 메시지와 내부 로그를 분리해야 해서 **바로 고칠 것(Week A D7)**으로 잡아뒀다. 그리고 오늘 확인은 전부 수동 호출이라 이 400과 본문이 다음 커밋에서도 유지된다는 보장이 없다. 테스트를 제대로 배울 때 갚을 **나중에 고칠 것(Week D D5)**다.

면접에서 받으면 답이 갈릴 질문 하나를 남긴다 — 검증 실패 응답을 필드-메시지 Map으로 주는 것과 오류 코드·요청 식별자를 포함한 고정 스키마로 주는 것 중, 어느 쪽이 어떤 클라이언트에게 유리한가.

---

오늘 공부한 소스코드: [8week_Spring_Study/app](https://github.com/enderpawar/8week_Spring_Study/tree/master/app)
