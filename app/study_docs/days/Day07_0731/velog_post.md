# [백엔드 기본기 Day 7] 저장은 `add()`가 아니다 — 중복 예약과 `null`을 경계에서 끊기

Day04에 만든 취소 흐름에는 두 개의 숨은 상태가 남아 있었다. 기존 예약을 다시 `save()`하면 같은 객체 참조가 목록에 중복됐고, 없는 번호를 조회하면 `null`이 Service까지 흘러갔다.

> Day07에서는 저장소의 신규·갱신 계약을 분리하고, 값 부재를 `Optional`로 드러낸 뒤 도메인 예외와 HTTP 상태 코드로 단계별 변환했다. 단위 테스트뿐 아니라 MockMvc로 실제 Spring MVC 경계의 200·400·404 응답까지 고정했다.

## 1. 개념 설명

| 용어 | 한줄뜻 | 코드 모습 |
|---|---|---|
| 저장 계약 | 신규와 기존 데이터를 저장할 때의 동작 약속 | ID 없음은 `add`, 기존 ID는 `set` |
| `Optional` | 값이 없을 수 있음을 반환형에 표시 | `Optional<Reservation> findById(...)` |
| 도메인 예외 | 업무 의미를 가진 실패 | `ReservationNotFoundException` |
| 메서드 검증 | Controller 인수의 제약을 요청 처리 전에 검사 | `@PathVariable @Positive Long id` |

`save()`라는 이름만으로 동작은 정해지지 않는다. 메모리 목록에 무조건 추가할 수도 있고, 같은 ID가 있으면 교체할 수도 있다. 호출자가 예측 가능한 저장소가 되려면 신규와 기존 객체를 어떻게 구분하는지 계약으로 정해야 한다.

현재 `Reservation`은 가변 객체다. `findById()`가 목록 안의 객체 참조를 반환하므로 `reservation.cancel()`을 호출하는 순간 목록 안 객체의 `confirmed`도 이미 `false`가 된다. 그 뒤 예전 `save()`가 `add()`를 호출하면 새 값이 저장되는 것이 아니라 **같은 객체 참조가 목록에 한 번 더 들어간다.**

그래서 ID가 없는 객체만 신규로 추가하고, ID가 있으면 같은 ID의 위치를 찾아 교체한다. DB의 INSERT와 UPDATE를 완전히 구현한 것은 아니지만, PK로 신규와 기존 대상을 구분한다는 멘탈 모델은 Week B의 JPA로 이어진다.

값 부재도 계약의 일부다. `null` 반환은 메서드 선언만 봐서는 알 수 없는 숨은 경우다. `Optional<Reservation>`은 “예약이 없을 수 있음”을 타입에 드러내고, Service가 `orElseThrow()`를 통해 업무 의미가 있는 실패로 바꾸게 한다.

예외는 계층을 지나며 의미가 바뀐다.

```text
Repository: Optional.empty()
    → Service: ReservationNotFoundException
        → HTTP 경계: 404 Not Found + 안전한 JSON 본문
```

이 분리는 Repository가 HTTP를 모르고, Service가 JSON을 모르면서도 각 계층이 자기 언어로 실패를 표현하게 한다. 예상하지 못한 예외는 500으로 처리하되 내부 메시지를 그대로 외부에 내보내지 않는다.

경로 변수 검증은 `@Positive`를 Controller 인수에 직접 선언했다. Spring MVC의 내장 메서드 검증이 이를 검사하고 `HandlerMethodValidationException`을 발생시키며, 전역 예외 처리기가 400 응답으로 변환한다.

> **더 볼 것**
> - [Spring MVC Validation](https://docs.spring.io/spring-framework/reference/6.2/web/webmvc/mvc-controller/ann-validation.html): `@RequestBody` 검증과 메서드 인수 검증의 차이
> - [Spring Controller Advice](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-advice.html): 전역 `@ExceptionHandler`가 Controller에 적용되는 방식
> - [Java Optional API](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Optional.html): 값 부재를 표현하고 `orElseThrow()`로 변환하는 API
> - 아직 안 본 것 — RFC 9457 `ProblemDetail` 기반 공통 오류 응답은 Week D·E의 오류 계약에서 검토한다.

## 2. 코드 구현

### 신규는 추가하고 기존 ID는 교체한다

저장소의 첫 분기는 ID 유무다.

```java
if (reservation.getId() == null) {
    reservation.assignId(nextId++);
    store.add(reservation);
    return reservation;
}
```

ID가 있다면 목록을 순회해 같은 ID의 위치를 바꾼다.

```java
for (int index = 0; index < store.size(); index++) {
    if (store.get(index).getId().equals(reservation.getId())) {
        store.set(index, reservation);
        return reservation;
    }
}

throw new IllegalArgumentException(
        "저장소에 없는 예약 번호입니다: " + reservation.getId());
```

ID가 있는데 저장소에는 없다면 조용히 신규 데이터로 넣지 않는다. 호출 흐름의 모순이므로 예외로 드러낸다. 이 분기가 없으면 외부에서 임의 ID를 붙인 객체가 신규 데이터처럼 섞일 수 있다.

### `null`을 Repository 경계에서 끝낸다

Repository 계약은 값 부재를 명시한다.

```java
Optional<Reservation> findById(Long id);
```

구현체는 못 찾았을 때 `Optional.empty()`를 반환하고, Service가 업무 예외로 바꾼다.

```java
Reservation reservation = reservationRepository.findById(id)
        .orElseThrow(() -> new ReservationNotFoundException(id));

reservation.cancel();
reservationRepository.save(reservation);
```

이제 `null.cancel()`에서 우연히 발생한 `NullPointerException`과 “예약 번호가 없음”을 구분할 수 있다. Controller나 예외 처리기는 구체적인 도메인 예외만 404로 매핑한다.

### 입력 오류·업무 오류·서버 오류를 나눈다

Controller는 0 이하의 ID를 Service에 보내지 않는다.

```java
public String cancel(
        @PathVariable
        @Positive(message = "예약 번호는 1 이상이어야 합니다") Long id) {
    Reservation reservation = reservationService.cancel(id);
    // 응답 문자열 생성
}
```

전역 예외 처리기는 실패 종류를 구분한다.

```java
@ExceptionHandler(ReservationNotFoundException.class)
public ResponseEntity<Map<String, String>> handleNotFound(
        ReservationNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", ex.getMessage()));
}

@ExceptionHandler(Exception.class)
public ResponseEntity<Map<String, String>> handleUnexpected(Exception ex) {
    log.error("처리하지 못한 예외", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", "요청 처리 중 오류가 발생했습니다."));
}
```

404에는 사용자가 조치할 수 있는 예약 부재를 알리고, 예상하지 못한 500에서는 내부 메시지를 제거한다. 상세 원인은 stack trace와 함께 서버 로그에 남는다.

### MockMvc로 HTTP 경계까지 고정한다

Service 단위 테스트만으로는 `@PathVariable`, `@Positive`, `@RestControllerAdvice`, JSON 직렬화가 실제로 함께 동작하는지 알 수 없다. 그래서 네 가지 HTTP 계약을 추가했다.

```java
mockMvc.perform(post("/reservations/cancel/{id}", 0L))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0]")
                .value("예약 번호는 1 이상이어야 합니다"));
```

같은 방식으로 유효 예약 200, 빈 DTO 400, 없는 예약 404를 확인한다. 실제 포트를 열지는 않지만 Spring MVC의 요청 매핑과 예외 리졸버를 통과한다.

### 오늘 확인한 것

- 전체 자동 테스트 10개가 통과했다.
- Service 단위 테스트 2개: 취소 뒤 목록 크기 1, 없는 ID의 도메인 예외.
- 예외 처리기 단위 테스트 2개: 404 응답 객체, 500 내부 메시지 비노출.
- MockMvc 테스트 4개: 예약 200, DTO 검증 400, 없는 예약 404, 경로 ID 검증 400.
- Spring context 테스트 2개: 컨텍스트 기동, Singleton Service 동일성.
- 수정 전 코드를 대상으로 실패 테스트를 실행한 기록은 없으므로 TDD의 red-green 순서를 수행했다고 주장하지 않는다.
- 현재 작업은 커밋 전 검증 상태이므로 게시 글의 commit permalink는 커밋 후 추가한다.

## 3. 스스로 답한 질문

### Q. 객체를 이미 취소했는데 왜 다시 `save()`할 때 중복되는가?

처음에는 `save()`가 현재 상태를 저장한다는 이름만 보고 목록의 기존 값이 갱신될 것으로 생각하기 쉽다. 그러나 기존 구현은 ID 유무와 관계없이 `store.add(reservation)`만 호출했다.

`ArrayList`는 같은 참조의 중복을 금지하지 않는다. 객체 상태는 `cancel()`에서 이미 바뀌었지만, 목록의 칸 수는 `add()` 때문에 늘어난다. 저장 계약을 읽을 때는 메서드 이름이 아니라 신규·기존 분기와 자료구조 연산을 확인해야 한다.

### Q. `Optional`을 반환하면 오류 처리가 끝난 것인가?

아니다. `Optional`은 값 부재를 타입에 드러낼 뿐, 그것이 어떤 업무 실패인지는 정하지 않는다. Service가 `orElseThrow()`로 예약 부재를 도메인 예외로 바꾸고 웹 계층이 404로 매핑해야 클라이언트 계약까지 완성된다.

재발 방지 기준은 계층별 질문이다. Repository는 “값이 있는가?”, Service는 “업무상 무슨 실패인가?”, 웹 계층은 “어떤 HTTP 응답인가?”를 결정한다.

### Q. 모든 예외 메시지를 그대로 응답하면 디버깅이 쉬워지지 않나?

클라이언트에는 쉬워 보이지만 내부 구현 정보까지 공개될 수 있다. 디버깅에 필요한 상세 stack trace는 서버 로그에 남기고, 외부 응답은 안정적인 계약으로 제한한다.

이번 테스트에서는 `jdbc:password=secret`이라는 내부 메시지를 가진 예외를 처리기에 전달한 뒤 500 본문에 그 문자열이 포함되지 않는지 확인했다. 로그와 응답의 목적을 분리한 것이다.

## 4. 정리하며

이번 수정 전에는 `save()`, `findById()`, 전역 예외 처리기가 각각 동작하지만 서로의 실패 계약이 연결되지 않았다. 지금은 ID가 신규와 기존을 구분하고, `Optional`이 부재를 드러내며, 도메인 예외가 그 의미를 HTTP 404까지 운반한다.

또한 코드가 그럴듯해 보이는 것과 HTTP 계약이 검증된 것은 다르다. Service와 예외 처리기 단위 테스트에 MockMvc 테스트를 더하면서 URI 매핑·검증·예외 변환·JSON 본문을 한 경로로 확인했다.

남은 한계는 두 가지다. 오류 코드·타임스탬프·요청 식별자를 포함한 공통 오류 DTO는 아직 없으므로 나중에 고칠 것(Week D D5 또는 Week E D1)으로 남긴다. `ArrayList`와 `nextId`의 동시성 안전성은 이번 5주 범위에서 고치지 않으며, 메모리 데이터가 재시작 후 사라지는 문제는 Week B D3의 영속성 교체에서 해결한다.

<!-- 선택 복습 메모: 게시 화면에는 노출하지 않는다.
### 선택 추가 설명

[직접 작성] 신규 예약의 저장과 기존 예약의 갱신을 `id`로 어떻게 구분하는지, 그리고 `Optional`이 `null`보다 실패 경계를 어떻게 선명하게 만드는지 설명한다.
-->
