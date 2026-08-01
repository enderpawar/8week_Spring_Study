# [백엔드 기본기 Day 3] Bean Validation과 `@RestControllerAdvice` — 잘못된 요청을 일관된 400 응답으로 바꾸기

Day 2에서 HTTP 요청 바디를 `ReservationRequest` DTO로 분리했다. 이번에는 DTO가 형식에 맞더라도 필수 문자열이 비어 있을 수 있다는 문제를 다뤘다. 범위는 `@NotBlank`를 이용한 입력 검증과 검증 실패의 전역 처리까지이며, 오류 응답 형식의 표준화나 모든 엔드포인트의 검증까지 완료한 것은 아니다.

## 한눈에 보기

- **문제:** `ReservationRequest`의 필수 값이 비어 있어도 컨트롤러 본문이 실행될 수 있었고, 컨트롤러마다 오류 응답을 만들면 같은 처리가 반복될 수 있었다.
- **적용:** DTO 필드에 `@NotBlank`, 컨트롤러 파라미터에 `@Valid`를 적용했다. 검증 실패로 발생한 `MethodArgumentNotValidException`은 `@RestControllerAdvice`가 붙은 전역 처리기에서 필드별 메시지와 HTTP 400 응답으로 변환했다.
- **검증:** `roomName=""`, `requesterName="김민준"` 요청에서 `400`과 `{"roomName":"방 이름은 비어있을 수 없습니다"}`를 확인했다. 글을 다시 검수한 시점에는 `./gradlew.bat test`도 `BUILD SUCCESSFUL`이었지만, 현재 테스트는 애플리케이션 컨텍스트 기동만 검사한다.
- **한계:** Day 3 코드의 `/reservations/cancel`에는 `@Valid`가 없고, HTTP 실패 경로를 자동화한 테스트도 없다. 모든 예외를 잡는 처리기는 예외 메시지를 그대로 응답하므로 운영용 오류 정책으로 보기 어렵다.

## 1. 문제를 이해하기 위한 이론

### 검증은 왜 컨트롤러 본문보다 앞에서 해야 하는가

`@RequestBody`는 JSON을 자바 객체로 변환하지만, 변환된 값이 비즈니스 규칙에 맞는지까지 보장하지 않는다. 예를 들어 `roomName`이 빈 문자열이어도 `String` 타입이라는 조건은 만족한다. 타입 변환 성공과 유효한 입력은 별개의 문제다.

따라서 요청 처리 계층의 입구에 사전조건을 두었다. 유효하지 않은 요청을 컨트롤러 본문에 넘기지 않으면 `Reservation` 생성과 `confirm()` 호출 전에 실패를 확정할 수 있다. 이는 호출자가 지켜야 할 조건을 먼저 검사하는 **계약(Contract)** 과 연결된다. 차이는 자바 타입 시스템이 `String` 여부까지만 정적으로 확인하는 반면, `@NotBlank`는 null·빈 문자열·공백 문자열이라는 값의 조건을 런타임에 검사한다는 점이다.

### 핵심 용어

- **Bean Validation:** 객체의 필드나 프로퍼티에 선언한 제약 조건을 검사하는 표준 검증 규약이다. Spring Boot 3의 코드에서는 `jakarta.validation` 패키지를 사용한다.
- **`@NotBlank`:** 문자열이 `null`, `""`, 공백 문자만으로 이루어진 경우를 허용하지 않는 제약 조건이다. `@NotNull`이 null만 막는 것과 범위가 다르다.
- **`@Valid`:** 현재 예제에서는 `ReservationRequest`에 선언된 제약 조건 검사를 실행하도록 메서드 인자 처리 과정에 지시한다. DTO에 `@NotBlank`만 붙이고 컨트롤러 인자에 `@Valid`를 붙이지 않으면 이 요청 경로에서 검증이 시작되지 않는다.
- **`MethodArgumentNotValidException`:** MVC 컨트롤러 메서드 인자의 Bean Validation이 실패했을 때 Spring MVC가 발생시키는 예외다.
- **`BindingResult`:** 바인딩과 검증 결과를 담는다. `getFieldErrors()`는 DTO의 모든 필드가 아니라 검증에 실패해 `FieldError`가 만들어진 필드만 반환한다.
- **`@RestControllerAdvice`:** 여러 컨트롤러에 적용할 예외 처리 메서드를 선언하고, 반환값을 응답 본문으로 쓰게 하는 애노테이션이다. 단순히 Bean이기 때문에 예외를 잡는 것이 아니라, `ExceptionHandlerExceptionResolver`가 `@ControllerAdvice` 계열 Bean을 전역 예외 처리 후보로 관리하기 때문에 동작한다.

### 요청에서 오류 응답까지의 흐름

```text
POST /reservations
→ Spring MVC가 JSON을 ReservationRequest로 변환
→ @Valid가 roomName과 requesterName의 @NotBlank를 검사
→ 실패 시 MethodArgumentNotValidException 발생
→ ExceptionHandlerExceptionResolver가 처리 가능한 @ExceptionHandler 탐색
→ GlobalExceptionHandler.handleValidation() 실행
→ 실패 필드와 메시지를 담은 Map을 HTTP 400 본문으로 반환
```

검증이 통과하면 `ReservationController.reserve()` 본문으로 들어간다. 반대로 하나라도 실패하면 본문에 진입하지 않으므로 `new Reservation(...)`과 `reservation.confirm()`은 실행되지 않는다. 예외는 평상시 반환 경로를 건너뛰고 별도 처리 경로로 제어를 옮긴다는 점에서, CS에서 배운 예외 기반 제어 흐름의 한 사례다.

### Day 3 코드에서 확인한 위치

- [`ReservationRequest.java`](https://github.com/enderpawar/8week_Spring_Study/blob/306100f660de477643481eea8debd0a8b5de4e84/app/src/main/java/com/example/studyroom/dto/ReservationRequest.java): 각 요청 필드의 `@NotBlank` 계약을 선언한다.
- [`ReservationController.reserve()`](https://github.com/enderpawar/8week_Spring_Study/blob/306100f660de477643481eea8debd0a8b5de4e84/app/src/main/java/com/example/studyroom/controller/ReservationController.java): `@RequestBody @Valid ReservationRequest`로 변환 뒤 검증을 요청한다.
- [`GlobalExceptionHandler.handleValidation()`](https://github.com/enderpawar/8week_Spring_Study/blob/306100f660de477643481eea8debd0a8b5de4e84/app/src/main/java/com/example/studyroom/exception/GlobalExceptionHandler.java): 실패 필드만 모아 400 응답으로 바꾼다.

## 2. 설계 선택과 판단

검증 규칙은 DTO 필드에 두고, 요청 경로에서는 `@Valid`로 실행했다. 이렇게 하면 “방 이름과 예약자 이름은 공백일 수 없다”는 입력 계약이 DTO 선언 가까이에 남는다. 컨트롤러는 검증 세부 절차보다 정상 요청을 `Reservation`으로 바꾸는 흐름에 집중할 수 있다.

검증 예외를 각 컨트롤러 메서드 안에서 직접 응답으로 바꾸지 않고 `GlobalExceptionHandler`에 모았다. 같은 예외를 여러 요청 경로에서 같은 형태로 처리할 수 있고, 정상 흐름과 실패 응답 변환의 책임도 분리된다. 다만 전역이라는 범위는 자동으로 좋은 정책을 보장하지 않는다. 어떤 예외를 어떤 상태 코드와 공개 가능한 메시지로 바꿀지는 별도로 설계해야 한다.

응답에는 `getFieldErrors()`가 제공한 실패 필드 이름과 기본 메시지만 넣었다. 학습 예제로는 어느 필드가 계약을 위반했는지 바로 확인할 수 있지만, 오류 코드·타임스탬프·요청 식별자 등을 가진 일관된 오류 스키마는 아직 없다.

## 3. 코드로 적용하기

입력 계약과 검증 시작점은 다음 두 부분이다.

```java
public record ReservationRequest(
    @NotBlank(message = "방 이름은 비어있을 수 없습니다") String roomName,
    @NotBlank(message = "예약자 이름은 비어있을 수 없습니다.") String requesterName
) {}

@PostMapping("/reservations")
public String reserve(@RequestBody @Valid ReservationRequest request) {
    Reservation reservation = new Reservation(request.roomName(), request.requesterName());
    reservation.confirm();
    return reservation.getRequesterName() + "님이 " + reservation.getRoomName()
        + " 예약 완료 (확정: " + reservation.isConfirmed() + ")";
}
```

`@RequestBody`가 JSON을 `ReservationRequest`로 변환한 뒤 `@Valid`가 record 컴포넌트의 제약 조건을 검사한다. 두 필드가 모두 통과한 경우에만 메서드 본문이 실행된다.

실패 경로는 전역 처리기로 옮겼다.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
```

`@ExceptionHandler`의 예외 타입이 처리 대상을 정한다. `getFieldErrors()`를 순회하면서 실패한 필드만 `Map`에 넣고, `ResponseEntity`로 본문과 `400 Bad Request`를 함께 반환한다.

## 4. 예측 → 실행 → 차이 설명

### 실패하지 않은 필드도 응답에 포함될까

| 구분 | 기록 |
|---|---|
| 실행 전 예측 | `roomName=""`, `requesterName="김민준"`이면 400이고, 응답 바디에는 정상인 `requesterName`도 나올 것이라고 답했다. |
| 실행 | 해당 JSON으로 `POST /reservations` 요청을 보냈다. |
| 실제 결과 | 상태 코드는 `400`, 본문은 `{"roomName":"방 이름은 비어있을 수 없습니다"}`였고 `requesterName`은 없었다. |
| 차이의 원인 | `getFieldErrors()`가 DTO 전체 필드를 순회한다고 생각한 것이 원인이었다. 실제로는 제약 조건을 위반한 필드의 `FieldError`만 반환하므로 정상 필드는 응답 Map에 들어갈 기회가 없다. |

처음에는 정상 필드가 “먼저 들어갔다가 덮어써져서 사라졌다”고 설명했지만, Map 갱신 문제가 아니라 순회 대상에 포함되지 않은 것이 정확한 원인이었다.

### 전역 처리기는 Bean이기만 하면 동작할까

| 구분 | 기록 |
|---|---|
| 실행 전 예측 | 명시적인 연결 없이 예외를 잡는 이유를 “Spring Bean 관리를 통해서”라고 답했다. |
| 실행·확인 | `GlobalExceptionHandler`가 `ReservationController`를 참조하지 않는 코드와 실제 400 응답을 확인하고, Spring MVC의 예외 처리 흐름을 대조했다. |
| 실제 결과 | Bean 등록은 필요한 조건이지만 그것만으로 충분하지 않았다. `@ControllerAdvice` 계열 Bean을 `ExceptionHandlerExceptionResolver`가 전역 후보로 특별 취급한다. |
| 차이의 원인 | 컨테이너의 객체 관리와 MVC 리졸버의 역할을 하나로 뭉뚱그렸다. `@Service`나 일반 `@Component` Bean이 컨트롤러 예외를 자동으로 처리하지 않는다는 반례로 두 역할을 구분할 수 있다. |

## 5. 검증 근거

| 검증 대상 | 검증 방법 | 확인한 결과 |
|---|---|---|
| Day 3 정상 컴파일 | 독립 작성 중 발생한 컴파일 오류 세 곳을 교정한 뒤 빌드 | 타입 이름의 공백, 생성식의 공백, 클래스 바깥 메서드 문제를 고친 뒤 빌드 통과 |
| 검증 실패 경로 | `roomName=""`, `requesterName="김민준"`으로 `POST /reservations` | `400`, `{"roomName":"방 이름은 비어있을 수 없습니다"}` |
| 현재 컨텍스트 회귀 검사 | 글 재검수 시 `./gradlew.bat test` 실행 | `BUILD SUCCESSFUL`; `contextLoads()`만 있으므로 HTTP 검증을 대신하지는 않음 |

Day 3의 변경 범위는 [`306100f` 커밋](https://github.com/enderpawar/8week_Spring_Study/commit/306100f660de477643481eea8debd0a8b5de4e84)에서 확인할 수 있다. 당시 저장소에는 HTTP 응답을 자동 검증하는 테스트가 없으므로, 위 400 응답은 학습 기록에 남은 수동 요청 결과다.

## 6. 막힌 지점과 오답 교정

### 연쇄 컴파일 오류의 실제 원인은 세 곳이었다

독립 작성한 `GlobalExceptionHandler`에서 컴파일 오류 11개가 한꺼번에 표시됐다. 그러나 각각을 별도 문제로 보기보다 첫 구문 오류부터 추적하니 원인은 세 곳으로 좁혀졌다.

- `ResponseE ntity`의 중간 공백 때문에 하나의 타입 이름이 두 토큰으로 분리됐다.
- `newLinkedHashMap<>()`는 `new LinkedHashMap<>()`가 아니므로 객체 생성식으로 해석되지 않았다.
- 두 번째 `@ExceptionHandler` 메서드를 클래스를 닫는 `}` 뒤에 작성해 파일 최상위에 메서드가 놓였다.

특히 마지막 오류는 단순 철자보다 자바의 구조에 관한 문제였다. 클래스 닫는 중괄호 뒤에는 독립 메서드를 선언할 수 없다. 교정할 때는 최초 오류 위치의 토큰, 생성식의 `new` 문법, 중괄호가 만드는 클래스 범위를 차례로 확인했다. 컴파일러가 뒤쪽에서 연쇄 오류를 많이 보고하더라도 최초 구문 오류와 코드 블록 경계를 먼저 확인하는 것이 재발 방지 기준이 됐다.

### `getFieldErrors()`에 대한 오답

정상 필드가 응답에서 사라진 이유를 Map의 덮어쓰기로 설명한 것은 잘못이었다. 실제 원인은 `BindingResult.getFieldErrors()`가 실패한 필드만 제공한다는 점이었다. 이후에는 컬렉션을 가공한 결과가 예상과 다르면 `put()`만 볼 것이 아니라, 그보다 앞선 컬렉션이 어떤 원소를 제공하는지부터 확인해야 한다.

## 7. 현재 한계와 다음 개선

- **검증 적용 범위:** Day 3의 `reserve()`에는 `@Valid`가 있지만 `cancel()`에는 없다. 같은 DTO를 받아도 요청 경로에 따라 검증 여부가 달라질 수 있다.
- **자동화된 실패 경로 검증 부재:** 현재 테스트는 `contextLoads()`뿐이다. 이후에는 MockMvc 등으로 빈 문자열 요청의 상태 코드와 JSON 본문을 고정해야 회귀를 잡을 수 있다.
- **오류 응답 스키마 부재:** 필드와 메시지만 담은 Map은 단순하지만, 클라이언트가 안정적으로 분기할 오류 코드가 없다. 한 필드에 여러 위반이 생기면 같은 Map 키에 마지막 메시지가 덮어써질 수도 있다.
- **포괄 예외 처리:** Day 3 코드의 `@ExceptionHandler(Exception.class)`는 `ex.getMessage()`를 그대로 500 응답에 담는다. 내부 정보 노출 가능성이 있으므로 운영 환경에서는 외부 메시지와 내부 로그를 분리해야 한다.
- **다음 단계:** 로드맵의 다음 학습은 Service와 Repository의 책임 분리다. 오류 응답 자동화와 표준 스키마는 현재 코드에 남은 후속 과제다.

## 8. 복습을 위한 인출 질문

### Q1. `@RequestBody` 변환에 성공했는데도 `@Valid`가 필요한 이유는 무엇인가?

<details>
<summary>답 확인</summary>

JSON이 `String` 필드를 가진 DTO로 변환됐다는 사실은 값이 비어 있지 않다는 조건까지 보장하지 않는다. `@Valid`는 DTO에 선언한 `@NotBlank` 같은 런타임 제약 조건 검사를 시작해 타입 검사만으로 표현하지 못한 입력 계약을 확인한다.

</details>

### Q2. `roomName`만 검증에 실패했을 때 정상인 `requesterName`이 오류 응답에 없는 이유는 무엇인가?

<details>
<summary>답 확인</summary>

`BindingResult.getFieldErrors()`가 DTO의 모든 필드가 아니라 검증에 실패해 `FieldError`가 생성된 필드만 반환하기 때문이다. 정상 필드는 순회 대상이 아니므로 오류 Map에도 추가되지 않는다.

</details>

### Q3. `@RestControllerAdvice`가 컨트롤러를 직접 참조하지 않아도 예외를 처리하는 흐름은 무엇인가?

<details>
<summary>답 확인</summary>

요청 처리 중 예외가 발생하면 `DispatcherServlet`의 예외 해결 과정에서 `ExceptionHandlerExceptionResolver`가 처리 메서드를 찾는다. 이 리졸버는 `@ControllerAdvice` 계열 Bean을 전역 후보로 관리하고, 예외 타입과 맞는 `@ExceptionHandler` 메서드를 호출한다. 단순 Bean 등록만으로 전역 처리가 되는 것은 아니다.

</details>

### Q4. 컴파일러가 11개 오류를 표시했을 때 실제 원인이 더 적을 수 있는 이유는 무엇인가?

<details>
<summary>답 확인</summary>

앞쪽의 잘못된 토큰이나 중괄호가 파서의 문법 해석을 깨뜨리면 뒤쪽의 정상 코드도 잘못된 위치에 있는 것처럼 연쇄 진단될 수 있다. 따라서 최초 오류 위치와 구조 경계를 먼저 고쳐 다시 컴파일해야 한다.

</details>

## 정리하며

처음에는 `@Valid`와 `@RestControllerAdvice`를 “검증하고 전역에서 잡는다”는 한 문장으로 이해했다. 실제 요청을 대조하면서 검증 실패는 컨트롤러 본문 전에 제어 흐름을 바꾸고, `getFieldErrors()`는 실패한 필드만 제공하며, 전역 처리는 단순 Bean 등록이 아니라 MVC 예외 리졸버의 탐색 규칙으로 성립한다는 수준까지 구분하게 됐다.

동시에 현재 구현의 경계도 확인했다. 검증은 모든 요청 경로에 적용되지 않았고, 400 응답은 수동으로만 확인했으며, 포괄 예외 처리의 메시지 공개 정책도 안전하지 않다. 다음 학습으로 책임 분리를 진행하되, 이 실패 경로는 자동화된 HTTP 테스트와 일관된 오류 스키마가 필요한 후속 과제로 남는다.
