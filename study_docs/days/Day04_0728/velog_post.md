# [백엔드 기본기 Day4] Service/Repository 책임 분리, 그리고 "식별자가 왜 필요한가"

> Day4의 핵심은 Controller가 직접 하던 일을 Service·Repository로 나누는 것이었다. `reserve()` 경로는 지난 세션에 먼저 끝내놨고, 오늘은 `cancel()`을 똑같은 패턴으로 옮기는 독립 변형을 마무리했다. 그 과정에서 "저장된 객체를 찾아서 바꾸는 것"과 "새 객체를 만들어서 저장하는 것"이 왜 완전히 다른 일인지, 그리고 컴파일이 통과해도 실제로 요청을 던져봐야만 드러나는 버그가 있다는 것까지 겪었다.

## 1. 독립 변형 — `cancel()`을 Service로 옮기기

`reserve()`가 이미 이런 모양이었다.

```java
@PostMapping("/reservations")
public String reserve(@RequestBody @Valid ReservationRequest request) {
    Reservation reservation = reservationService.reserve(request.roomName(), request.requesterName());
    return "예약 번호" + reservation.getId() + "-" + reservation.getRequesterName() + "님이 " + reservation.getRoomName() + " 예약 완료 (확정: " + reservation.isConfirmed() + ")";
}
```

`cancel()`도 같은 패턴으로 옮기면 될 줄 알았는데, 옮기고 실행해보니 예상과 다른 일이 벌어졌다.

## 2. 예측이 틀렸던 지점 — 취소했는데 예약이 하나 더 생김

`reserve()`로 방을 하나 확정한 다음 같은 `roomName`/`requesterName`으로 취소를 호출하면, 기존 예약이 찾아져서 `confirmed`만 `false`로 바뀔 거라고 예측했다. 실제로는 새 예약이 하나 더 생겼다.

```java
public Reservation cancel(String roomName, String requesterName){
    Reservation reservation = new Reservation(roomName, requesterName); // 새 객체!
    reservation.cancel();
    return reservationRepository.save(reservation);
}
```

원인은 `new Reservation(...)`이 매번 완전히 새로운 인스턴스를 만든다는 것. `roomName`/`requesterName`이 같아도 자바 입장에선 다른 객체고, 저장소(`InMemoryReservationRepository.save()`)도 항상 `store.add(...)`만 하지 "이미 있으면 갱신"이라는 분기가 없다. 즉 **지금 `Reservation`에는 "이게 바로 그 예약이다"라고 지칭할 고유 식별자(id)가 없었다.** DB 이론으로 치면 기본키(PK)가 없는 테이블이나 마찬가지였던 셈.

## 3. 식별자 도입, 그리고 컴파일러가 잡아준 실수들

`Reservation`에 `id` 필드와 `assignId()`/`getId()`를 추가하고, `InMemoryReservationRepository`에 `findById()`를 작성하면서 두 가지 문법 실수를 했다.

```java
if(r.getId()== id)){   // 괄호 개수가 안 맞음 → illegal start of expression
```

```java
public Reservation findById(Long id) {
    for(Reservation r : store){
        if(r.getId().equals(id)){
            return r;
        }
    }
    // 여기서 못 찾은 경우 리턴문이 없어서 → missing return statement
}
```

둘 다 컴파일러가 바로 잡아줬다. 두 번째는 "모든 실행 경로가 값을 리턴해야 한다"는 규칙 — 루프를 다 돌고도 못 찾은 경우를 처리하는 코드가 없으면 컴파일 자체가 안 된다는 걸 다시 확인했다.

## 4. `==`와 `.equals()` — 처음엔 놓쳤던 부분

`findById`를 처음 짤 때 `r.getId() == id`로 비교했다. `Long`은 원시타입(`long`)이 아니라 객체(Wrapper)라서, `==`는 값이 아니라 **참조(메모리 주소)**를 비교한다. `String`을 `==`로 비교하면 안 되는 것과 완전히 같은 이유다. 더 골치아픈 건, 자바가 -128~127 범위의 `Long`을 내부적으로 캐싱해서 재사용하기 때문에 지금처럼 id가 1, 2, 3처럼 작을 때는 `==`가 우연히 통과해버린다는 것 — 작은 데이터로 테스트하면 숨어있다가 값이 커지면 터지는 종류의 버그였다. `.equals()`로 고쳤다.

## 5. 컴파일·테스트가 초록불이어도 안심할 수 없는 버그

`@PathVariable Long id`를 컨트롤러 파라미터에 추가했는데, `@PostMapping` 경로에는 `{id}`를 안 넣은 채로 커밋 직전까지 갔다. `./gradlew compileJava`와 `./gradlew test`는 둘 다 성공이었다. 그런데 실제로 앱을 띄워서 요청을 보내보니:

```
POST /reservations/cancel → 500
{"error":"Required URI template variable 'id' for method parameter type Long is not present"}
```

`@PathVariable`과 URL 템플릿의 불일치는 자바 문법 오류가 아니라, Spring이 요청을 실제로 처리하면서 URI에서 값을 채우려 할 때 나는 런타임 오류다. 그래서 문법 검사·단위 테스트를 다 통과해도 "그 경로로 요청이 실제로 들어와야만" 드러났다. `@PostMapping("/reservations/cancel/{id}")`로 경로를 고치고 나서야 정상 동작했다.

```
POST /reservations {"roomName":"A101","requesterName":"tester"}
→ 예약 번호1-tester님이 A101 예약 완료 (확정: true)

POST /reservations/cancel/1
→ tester님이A101 예약을 취소하셨습니다 (확정 : false)
```

## 스스로 묻고 답한 질문들

### Q. Controller에 HTTP 처리와 비즈니스 로직을 같이 두면 안 되는 이유는?

단일 책임 원칙(SRP) 위반이다 — 요청 형식이 바뀌어도, 비즈니스 규칙이 바뀌어도 같은 클래스를 건드리게 된다. 그리고 비즈니스 로직이 Controller 안에 있으면 테스트할 때마다 가짜 HTTP 요청을 만들어야 하지만, Service로 분리하면 HTTP 없이 순수 로직만 테스트할 수 있다.

### Q. Service가 `InMemoryReservationRepository`라는 구현체 이름을 전혀 모르는 채로 동작하는 이유는?

의존성 역전 원칙(DIP) — Service가 구체적인 구현이 아니라 추상화(인터페이스)에 의존하기 때문에, 나중에 구현체를 JPA 기반으로 바꿔도 Service 코드는 건드릴 필요가 없다. Week B에서 실제로 이 교체를 해볼 예정이다.

## 정리하며

오늘은 "Service/Repository로 나눈다"는 한 줄짜리 개념을 `cancel()`에 실제로 적용하면서, 그 이면에 있던 "식별자 없이는 갱신이 불가능하다"는 문제와 정면으로 부딪혔다. 덤으로 Wrapper 타입의 `==` 함정, 그리고 컴파일·테스트가 잡아주지 못하는 런타임 전용 버그의 존재까지 확인한 하루였다.

---

오늘 공부한 소스코드: [8week_Spring_Study/app](https://github.com/enderpawar/8week_Spring_Study/tree/master/app)
