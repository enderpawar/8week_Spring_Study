# [백엔드 기본기 Day4] Service/Repository 책임 분리 — 같은 값으로 만든 새 객체는 왜 그 예약이 아닌가

Day3까지 `ReservationController`가 요청을 받는 일부터 예약 객체를 만드는 일까지 전부 하고 있었다. 오늘은 그 일을 `ReservationService`와 `ReservationRepository`로 나누고, 먼저 끝내둔 `reserve()`에 이어 `cancel()`을 같은 패턴으로 옮기는 독립 변형을 마무리했다. 계층을 나누는 것보다, 옮기고 나서 드러난 "취소할 예약을 어떻게 지목하는가"가 오늘의 중심 질문이 됐다. 저장소는 아직 메모리이고 JPA·DB는 이 글의 범위 밖이다.

> `cancel()`을 `reserve()`와 똑같은 패턴으로 옮겼더니, 예약이 취소되는 대신 하나 더 생겼다. 원인은 `new Reservation(...)`이 매번 새 인스턴스를 만드는데 저장소에는 그 둘을 구별할 식별자가 없었다는 것이었고, `Reservation`에 `id`를 넣고 `findById()`로 찾아 고치는 흐름으로 바꿔 curl로 reserve→cancel까지 확인했다. 덤으로 `Long`의 `==` 비교 함정과, 컴파일·테스트가 초록불이어도 요청을 실제로 던져야만 드러나는 `@PathVariable` 불일치를 같이 겪었다.

## 1. 개념 설명

| 용어 | 한줄뜻 | 코드 모습 |
|---|---|---|
| Service 계층 | 비즈니스 로직(상태 변경 규칙)을 수행하며 Controller와 Repository 사이를 조율한다 | `reservationService.cancel(id)` |
| Repository 인터페이스 | 저장소 접근을 "무엇을 할 수 있는지(What)"만 선언하고 "어떻게(How)"는 구현체에 위임 | `interface ReservationRepository { Reservation findById(Long id); }` |
| 단일 책임 원칙(SRP) | 클래스는 변경 이유를 하나만 가져야 한다 | Controller는 HTTP 입출력만, Service는 로직만 |
| 의존성 역전 원칙(DIP) | 상위 모듈이 구체 구현이 아니라 추상화에 의존한다 | `ReservationService(ReservationRepository repository)` |
| 식별자(PK) | 값이 같아 보여도 서로 다른 레코드를 구별하는 고유 값. 없으면 "찾아서 갱신"이 아니라 "새로 추가"만 가능 | `private Long id;` |
| Wrapper 타입 값 비교 | `Long`·`Integer` 같은 객체 타입은 `==`이 아니라 `.equals()`로 비교해야 값이 같은지 알 수 있다 | `r.getId().equals(id)` |
| `@PathVariable` | URL 경로의 일부(`{id}`)를 메서드 파라미터로 매핑 | `@PostMapping("/reservations/cancel/{id}")` |

오늘 만든 취소 요청 하나는 이렇게 흘러간다.

```text
POST /reservations/cancel/1
  → ReservationController            HTTP 입출력 (경로에서 id를 꺼낸다)
  → ReservationService               규칙 (찾아서 상태를 바꾼다)
  → ReservationRepository            저장소 접근 — What만 선언
      └ InMemoryReservationRepository  How를 구현 (지금은 ArrayList)
```

**세 계층을 가르는 기준은 "무엇이 바뀌면 이 파일을 고치게 되는가"다.** 요청 형식이 바뀌면 Controller, 예약 규칙이 바뀌면 Service, 저장 방식이 바뀌면 Repository. 셋을 한 클래스에 두면 서로 다른 세 이유가 같은 파일을 건드리게 되고, 그게 SRP 위반이다.

Repository를 인터페이스로 둔 이유는 그 다음 층이다. 소스 주석에 이렇게 적어뒀다.

> 인터페이스 = "What to do(무엇을 할 수 있는지)"만 약속. "How"는 구현체가 정한다.

`@Override`가 붙은 자리가 그 How를 채우는 자리다. 그래서 `ReservationService` 코드 어디에도 `InMemoryReservationRepository`라는 이름이 나오지 않고, 생성자 파라미터 타입은 인터페이스인 `ReservationRepository`다. 실제 구현체를 넣어주는 건 Spring의 생성자 주입인데, 그게 어떻게 동작하는지는 Day5 주제로 미뤄뒀다.

식별자는 그 아래에서 나왔다. `Reservation`은 record가 아니라 class인데, 주석에 이유를 적어놨다 — **"Domain = 상태 + 규칙(business rule)을 함께 가진다. record 형태가 아님. 상태가 바뀔 수 있어야하니까."** `confirmed`를 바꾸는 통로도 필드 직접 접근이 아니라 `confirm()`·`cancel()` 메서드로 캡슐화했다.

그런데 "상태를 바꾼다"가 성립하려면 **바꿀 대상을 지목할 수 있어야 한다.** 그 지목 수단이 식별자다.

- `id`는 `final`이 아니다 — "저장되기 전에는 아직 값이 없음"이기 때문이다
- 그래서 `save()`는 `getId() == null`일 때만 번호를 부여한다
- 식별자가 없으면 저장소는 "이미 있는 것"과 "새 것"을 구별할 근거가 없다

DB로 치면 기본키(PK)가 하는 일과 같은 자리다. JPA에서는 `@GeneratedValue`가 이 역할을 맡는다고 한다(Week B 예정).

식별자를 넣는 순간 **"id가 같은가"를 판정하는 문제**가 따라온다. 여기서 참조 동일성과 값 동일성이 갈린다. `Long`은 원시타입 `long`이 아니라 객체라서, `==`는 값이 같은지가 아니라 같은 메모리 주소를 가리키는지를 본다. 재미있게도 `InMemoryReservationRepository` 한 파일 안에 두 비교가 나란히 있다 — `getId() == null`(참조 비교가 맞는 자리)과 `getId().equals(id)`(값 비교가 맞는 자리).

마지막으로 취소할 대상을 클라이언트가 지목하려면 id를 보낼 통로가 필요했다. 그래서 URL 경로에 실어 `@PathVariable`로 받고, `reserve()` 응답에는 `예약 번호1-...`처럼 부여된 id를 노출해서 다음 요청에 쓸 수 있게 했다.

![객체 다이어그램 두 장. 위는 이름으로 취소했을 때로, store의 [0]과 [1]이 각각 r1과 r2라는 서로 다른 Reservation 인스턴스를 가리킨다. 둘 다 roomName이 "301호"로 값은 같지만 r1은 confirmed=true, r2는 false다. 취소한 건 r2뿐이라 기존 예약 r1은 그대로다. 아래는 id로 취소한 뒤로, 인스턴스는 id=1인 r1 하나인데 store의 [0]과 [1] 두 링크가 모두 그 하나를 가리킨다. save()가 ID 유무와 무관하게 store.add()를 실행하기 때문인데, 원소 수를 세는 테스트도 findAll() 엔드포인트도 없어 실제로 그런지는 확인하지 않았다.](../../../assets/day04-identity-store.png)

> **더 볼 것**
> - [Mapping Requests — Spring Framework Reference](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-requestmapping.html): URI 템플릿 변수와 `@PathVariable`의 관계
> - [JLS 5.1.7 Boxing Conversion](https://docs.oracle.com/javase/specs/jls/se17/html/jls-5.html#jls-5.1.7): `-128`~`127` 박싱 결과가 `==`로 같아지는 근거
> - 아직 안 본 것 — 생성자 주입이 동작하는 원리(Day5), REST에서의 자원 식별 규칙, 구현체를 JPA로 교체하기(Week B)

## 2. 코드 구현

### `cancel()`을 `reserve()`와 같은 패턴으로 옮기기

`reserve()` 경로는 이미 이 모양으로 분리해둔 상태였다.

```java
@PostMapping("/reservations")
public String reserve(@RequestBody @Valid ReservationRequest request) {
    Reservation reservation = reservationService.reserve(request.roomName(), request.requesterName());
    return "예약 번호" + reservation.getId() + "-" + reservation.getRequesterName() + "님이 "
            + reservation.getRoomName() + " 예약 완료 (확정: " + reservation.isConfirmed() + ")";
}
```

`cancel()`도 같은 모양으로 옮기면 될 줄 알았다.

### 예측이 틀렸던 지점 — 취소했는데 예약이 하나 더 생김

방을 하나 확정한 다음 같은 `roomName`/`requesterName`으로 취소를 호출하면, 기존 예약을 찾아서 `confirmed`만 `false`로 바뀔 거라고 예측했다. 실제로는 새 예약이 하나 더 생겼고, 원래 예약은 `confirmed: true` 그대로 남았다.

```java
public Reservation cancel(String roomName, String requesterName){
    Reservation reservation = new Reservation(roomName, requesterName); // 새 객체
    reservation.cancel();
    return reservationRepository.save(reservation);
}
```

원인은 `new Reservation(...)`이 매번 완전히 새 인스턴스를 만든다는 것이다. 값이 같아도 자바 입장에선 다른 객체고, `save()`에도 "이미 있으면 갱신"이라는 분기 없이 `store.add(...)`만 있다. 즉 **`Reservation`에는 "이게 바로 그 예약이다"라고 지칭할 수단이 없었다.** 기본키가 없는 테이블과 같은 상태였던 셈이다.

### 식별자를 넣자 컴파일러가 먼저 막았다

`Reservation`에 `id`와 `assignId()`/`getId()`를 추가하고 `findById()`를 쓰는데, 컴파일이 안 됐다.

```java
public Reservation findById(Long id) {
    for (Reservation r : store) {
        if (r.getId().equals(id)) {
            return r;
        }
    }
    // 루프가 끝까지 돌고 못 찾은 경우 → missing return statement
}
```

"모든 실행 경로가 값을 리턴해야 한다"는 규칙에 걸렸다. 여기서 `return null`을 넣으면 문법 조건은 채워지지만, **예약이 없을 때 무엇을 할지는 여전히 안 정한 것이다.** 지금 코드는 `return null`로 두고 넘어갔고, 이건 아래 한계에 적었다.

### 컴파일·테스트가 초록불이어도 안심할 수 없는 버그

`@PathVariable Long id`를 컨트롤러 파라미터에 추가했는데, `@PostMapping` 경로에는 `{id}`를 안 넣은 채로 커밋 직전까지 갔다. `./gradlew compileJava`와 `./gradlew test`는 둘 다 성공이었다. 그런데 앱을 띄워 요청을 보내니 이랬다.

```
POST /reservations/cancel → 500
{"error":"Required URI template variable 'id' for method parameter type Long is not present"}
```

`@PathVariable`과 URL 템플릿의 불일치는 자바 문법 오류가 아니라, Spring이 요청을 처리하면서 URI에서 값을 채우려 할 때 나는 런타임 오류다. 그래서 문법 검사와 테스트를 다 통과해도 **그 경로로 요청이 실제로 들어와야만** 드러났다. `@PostMapping("/reservations/cancel/{id}")`로 고치고 나서야 정상 동작했다.

### 오늘 확인한 것

**자동 테스트** — `./gradlew compileJava`, `./gradlew test` 모두 성공. 다만 지금 테스트는 `contextLoads()` 하나여서 경로 바인딩이나 응답 본문이 깨져도 초록불이 나온다. 위 `@PathVariable` 버그를 실제로 못 잡았다.

**수동 확인** — `bootRun`으로 앱을 띄우고 curl로 흐름을 확인했다.

```
POST /reservations {"roomName":"A101","requesterName":"tester"}
→ 예약 번호1-tester님이 A101 예약 완료 (확정: true)

POST /reservations/cancel/1
→ tester님이A101 예약을 취소하셨습니다 (확정 : false)
```

**미검증** — 취소 뒤 **저장소 리스트의 원소 수는 검증하지 않았다.** `findAll()`을 노출하는 엔드포인트가 없어서, `save()`가 같은 참조를 한 번 더 넣었는지 확인할 방법이 지금 없다. 컨트롤러에는 빈 문자열을 보내면 `@Valid`가 400을 낼 것 같다는 추측 주석이 아직 남아 있는데, 그 요청은 보내보지 않았다. 응답 본문은 추측하지 않는다.

오늘 코드는 [`e22bb34` 커밋](https://github.com/enderpawar/8week_Spring_Study/commit/e22bb34)에 있다. 앞선 `reserve()` 경로 분리는 [`d6320e2`](https://github.com/enderpawar/8week_Spring_Study/commit/d6320e2)다.

## 3. 스스로 답한 질문

### Q. Repository의 책임은 무엇인가?

처음엔 **"인터페이스 역할 수행"** 이라고 답했다. 인터페이스인 것은 구현 방식이지 책임 자체가 아니다. 진짜 책임은 **저장소 접근을 담당하는 것**이고, 지금은 메모리에 두지만 나중에 DB로 바뀌어도 이 책임 자체는 그대로다.

Controller에 셋을 다 몰아넣으면 안 되는 이유도 여기서 나온다. SRP 위반이라는 원칙 이야기 말고 실무적으로 체감되는 건 테스트다. 비즈니스 로직이 Controller 안에 있으면 테스트할 때마다 가짜 HTTP 요청을 만들어야 하지만, Service로 분리하면 HTTP 없이 순수 로직만 테스트할 수 있다.

### Q. Service가 `InMemoryReservationRepository`라는 이름을 전혀 모르는 채로 동작하는 이유는?

의존성 역전 원칙(DIP)이다. Service가 구체 구현이 아니라 추상화에 의존하니, 구현체를 JPA 기반으로 바꿔도 Service 코드는 안 건드려도 된다. Week B에서 실제로 이 교체를 해볼 예정이라, 그때 "정말 한 줄도 안 바뀌는지"가 이 주장의 시험대가 된다.

### Q. `findById`에서 `==` 대신 `.equals()`를 쓴 이유는?

처음엔 **"null값 탐지"** 를 위해서라고 답했다. 틀렸다. `.equals()`를 쓰는 이유는 null 탐지가 아니라 참조 동일성과 값 동일성의 차이 때문이다.

`Long`은 원시타입이 아니라 객체라서 `==`는 "값이 같다"가 아니라 "같은 주소를 가리킨다"를 검사한다. `String`을 `==`로 비교하면 안 되는 것과 **같은 함정이다**(다만 이유가 완전히 같지는 않다 — `String`은 컴파일타임 상수 인터닝이고, `Long`은 `valueOf`의 캐시다).

더 골치아픈 건 자바가 `-128`~`127` 범위를 캐싱해서 재사용한다는 것이다. 지금처럼 id가 1, 2, 3일 때는 `==`가 우연히 통과해버린다. **작은 데이터로 테스트하면 숨어있다가 값이 커지면 터지는 종류의 버그**라 오히려 더 위험하다. 이 항목은 오답이었어서 복습큐에 +1일 재시험으로 등록해뒀다.

## 4. 정리하며

오늘 바뀐 건 "Service/Repository로 나눈다"를 규칙으로 외우던 상태에서, **나눈 뒤에 무엇이 필요해지는지**를 본 것이다. 계층을 나누면 Service는 "찾아서 바꾼다"를 해야 하는데, 그러려면 찾을 대상을 지목할 식별자가 있어야 한다. 식별자가 생기면 곧바로 "같은 id인가"를 판정해야 하고, 거기서 참조 동일성과 값 동일성이 갈린다. 계층 분리 → 식별자 → 동일성이 따로 배우는 세 개가 아니라 한 줄로 이어져 있었다.

두 번째로 남은 건 초록불의 범위다. 컴파일러는 "모든 경로가 리턴하는가"까지는 잡아줬지만 "못 찾았을 때 무엇을 리턴해야 하는가"는 묻지 않았고, 테스트는 `@PathVariable`과 URL이 어긋난 걸 통과시켰다. 빌드 성공은 문법과 지금 등록된 테스트가 묻는 것에만 답한 결과였고, API가 계약대로 동작한다는 답까지는 아니었다.

아직 남은 것 두 가지는 둘 다 오늘 코드에 있는 결함이다. 첫째, `InMemoryReservationRepository.save()`가 id 유무와 관계없이 `store.add()`를 호출해서, `cancel()`이 찾아온 기존 객체를 다시 저장할 때 같은 참조가 중복으로 들어갈 수 있다. 위에 적었듯 지금은 원소 수를 확인할 수단이 없어 재현도 못 한 상태다. 둘째, `findById()`가 없는 id에 `null`을 반환하는데 `ReservationService.cancel()`이 확인 없이 `reservation.cancel()`을 호출한다. 존재하지 않는 id로 취소를 부르면 그 자리에서 터진다. 둘 다 저장 계약과 "없음"의 표현을 정하는 문제라 **바로 고칠 것(Week A D7)** 으로 분류했다.

면접에서 물어보면 답이 궁금한 질문 하나 — 메모리 저장소에서는 찾아온 객체를 고치기만 해도 `store` 안의 값이 바뀌는데, JPA로 바꾸면 `save()`를 다시 부르는 이 코드는 무엇이 달라지는가.

오늘 공부한 소스코드: [8week_Spring_Study/app](https://github.com/enderpawar/8week_Spring_Study/tree/master/app)
