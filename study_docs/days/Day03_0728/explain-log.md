# Day03 (7/28, Week A D3) 예측→실행→차이설명

형식: 개념 | 예측 | 실제 결과 | 차이 설명(왜 이렇게 동작하나)

| 개념 | 예측 | 실제 결과 | 차이 설명 |
|---|---|---|---|
| `GlobalExceptionHandler` 독립 작성 중 컴파일 | (특별한 예측 없이 작성) | 11개 컴파일 에러: `ResponseE ntity`(공백 오타), `newLinkedHashMap<>()`(`new ` 누락), 두 번째 `@ExceptionHandler` 메서드가 클래스 닫는 `}` 뒤(클래스 바깥)에 위치 | 자바 파서는 토큰을 정확히 매칭해야 함 — 타입 이름 중간 공백은 별개 토큰 두 개로 갈라져 "`)` or `,` expected" 에러가 남. 메서드는 반드시 클래스의 `{ }` 안에 있어야 하며, 클래스를 닫는 `}` 이후는 파일 최상위라 메서드 선언이 올 자리가 아님(class/interface/enum/record만 가능) |
| `roomName=""`, `requesterName="김민준"`으로 `POST /reservations` 요청 | 400 Bad Request, 바디에 `requesterName`도 정상적으로 나올 것 | `400`, 바디는 `{"roomName":"방 이름은 비어있을 수 없습니다"}` 뿐 — `requesterName`은 아예 없음 | `getBindingResult().getFieldErrors()`는 검증 **실패한** 필드만 `FieldError`로 담는다. `requesterName`은 `@NotBlank`를 통과해 애초에 그 목록에 없으므로 응답에도 나타나지 않는다(Q2 참고) |
| `@RestControllerAdvice`가 `ReservationController`와 명시적 연결 없이 전역으로 잡히는 이유 | "Spring Bean 관리를 통해 연결 표시 없이도 잡아내는 것" | 부분 정답 — Bean 등록만으로는 부족하고, `DispatcherServlet`의 `ExceptionHandlerExceptionResolver`가 `@ControllerAdvice` 계열 Bean을 별도로 수집해 전역 후보로 특별 취급하기 때문 | "모든 Bean이 전역 예외를 잡지는 않는다"는 반례(`@Service`, `@Component`)로 구분 가능. 특정 애노테이션 + 특정 리졸버의 조합이 핵심 |
