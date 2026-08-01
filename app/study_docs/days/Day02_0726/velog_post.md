# [백엔드 기본기 Day 2] record DTO와 Domain 분리 — 요청 데이터와 비즈니스 상태의 역할 구분

Day 1에는 `ReservationController`가 요청을 받아 문자열 응답을 돌려주는 왕복을 확인했다. Day 2에는 JSON 요청의 모양을 나타내는 `ReservationRequest`와 예약 상태·행동을 가진 `Reservation`을 분리했다. 이 글은 DTO와 Domain의 역할, `record` 접근자, `@RequestBody`의 변환 흐름까지 다루며 입력 검증과 Service·Repository 계층 분리는 이후 Day의 범위로 남긴다.

## 한눈에 보기

- **문제:** HTTP 요청 데이터와 예약 상태 변경 규칙을 한 역할처럼 다루면 API의 입력 형식과 비즈니스 모델의 책임이 섞인다.
- **적용:** 요청 DTO는 `record ReservationRequest`로, 상태와 `confirm()`·`canceled()` 행동은 `class Reservation`으로 나눴다.
- **검증:** `record` 접근자 오답에서 컴파일 오류를 확인했고, 수정 후 예약 취소 요청을 직접 보내 `confirmed`가 `false`인 응답을 확인했다.
- **한계:** 자동화된 HTTP 테스트와 입력 검증은 없었다. 한글 JSON 전송 실패도 인코딩과 셸 인자 해석의 영향을 각각 분리해 검증하지 못했다.

## 1. 문제를 이해하기 위한 이론

### 왜 DTO와 Domain을 나누는가

HTTP 요청은 외부 클라이언트가 보내는 데이터의 형식이다. 반면 Domain 객체는 애플리케이션이 다루는 상태와 그 상태를 바꾸는 규칙을 표현한다. 두 역할을 분리하면 API 입력 형식이 바뀌는 문제와 예약 규칙이 바뀌는 문제를 서로 다른 타입에서 다룰 수 있다.

Day 2 코드에서 `ReservationRequest`는 방 이름과 예약자 이름을 전달한다. `Reservation`은 같은 값을 내부 상태로 가지면서 `confirmed`를 직접 노출하지 않고 `confirm()`을 통해 바꾼다. 즉 DTO는 경계에서 데이터를 운반하고, Domain은 상태 전이 규칙을 캡슐화한다.

### 핵심 용어

- **DTO(Data Transfer Object):** 계층이나 프로세스 경계를 넘어 전달할 데이터의 모양을 표현하는 객체다. 이 예제에서는 HTTP 요청 본문의 구조를 나타낸다.
- **Domain 객체:** 문제 영역의 상태와 행동을 표현하는 객체다. `Reservation`은 예약 정보와 확정·취소라는 상태 변경을 함께 가진다.
- **Java `record`:** 컴포넌트 목록으로 데이터 중심 타입을 선언하는 문법이다. 컴포넌트 필드와 접근자, 표준 생성자, `equals()`·`hashCode()`·`toString()` 등을 제공한다. 접근자는 `getRoomName()`이 아니라 컴포넌트 이름과 같은 `roomName()`이다.
- **불변성의 범위:** `record` 컴포넌트는 생성 후 다른 값으로 재할당할 수 없다. 다만 컴포넌트가 가변 객체를 참조한다면 그 객체 내부까지 불변이 되는 것은 아니다. 따라서 `record` 자체가 깊은 불변이나 무조건적인 스레드 안전성을 보장하지는 않는다.
- **`@RequestBody`:** Spring MVC가 HTTP 요청 본문을 메서드 매개변수로 변환하도록 지시한다. 이 프로젝트에서는 JSON 메시지 컨버터와 Jackson이 `ReservationRequest`를 생성한다.

`record`가 DTO 전용이고 일반 `class`가 Domain 전용이라는 Java 규칙은 아니다. Domain도 불변 클래스로 설계할 수 있고, 값 객체라면 `record`가 Domain에 쓰일 수도 있다. Day 2에서는 “입력 데이터의 모양”과 “상태를 바꾸는 객체”의 차이를 드러내기 위해 이 조합을 선택했다.

### 동작 흐름

```text
POST /reservations의 JSON 본문
→ Spring MVC의 메시지 컨버터가 ReservationRequest로 역직렬화
→ ReservationController.reserve()가 roomName(), requesterName() 호출
→ 두 값으로 Reservation 생성
→ Reservation.confirm()이 confirmed를 true로 변경
→ Controller가 Reservation 상태를 문자열 응답으로 반환
```

`@RequestBody`가 붙은 매개변수를 보고 Spring MVC가 요청 본문 변환을 시작한다. 변환된 `ReservationRequest`에서 컴포넌트 접근자로 값을 꺼내고, 컨트롤러가 `Reservation`을 생성한다. 상태 변경은 필드를 외부에서 직접 대입하는 대신 Domain 메서드가 담당한다.

### CS 지식과 연결

이 분리는 OOP의 캡슐화와 연결된다. `confirmed`를 `private`으로 감추고 `confirm()`으로만 변경하면 “어떤 상태 변경을 허용할지”를 객체가 통제한다. DTO의 재할당 불가능한 컴포넌트는 불변 값의 장점과 연결된다. 여러 단계로 전달되는 동안 참조가 다른 값으로 바뀌지 않아 입력 스냅샷을 추적하기 쉽다. 다만 앞서 구분했듯 참조 대상 내부까지 자동으로 불변이 되는 것은 아니다.

### 현재 코드에서 찾기

- [`ReservationRequest.java`](https://github.com/enderpawar/8week_Spring_Study/blob/975be06/app/src/main/java/com/example/studyroom/dto/ReservationRequest.java): 요청 JSON과 대응하는 두 컴포넌트를 선언한다.
- [`Reservation.java`](https://github.com/enderpawar/8week_Spring_Study/blob/975be06/app/src/main/java/com/example/studyroom/domain/Reservation.java): 예약 상태와 `confirm()`·`canceled()` 행동을 캡슐화한다.
- [`ReservationController.java`](https://github.com/enderpawar/8week_Spring_Study/blob/975be06/app/src/main/java/com/example/studyroom/controller/ReservationController.java): DTO를 HTTP 경계에서 받고 Domain 객체로 옮기는 흐름을 보여준다.

## 2. 설계 선택과 trade-off

| 선택지 | 장점 | 단점·현재 판단 |
|---|---|---|
| 요청 DTO를 `record`, Domain을 `class`로 분리 | 입력 계약과 상태 변경 책임이 타입으로 구분되고 DTO의 반복 코드가 줄어든다. | 타입과 변환 코드가 늘어난다. 작은 예제에서는 중복처럼 보일 수 있다. |
| 하나의 일반 `class`로 요청과 Domain을 함께 표현 | 파일과 변환 코드가 적다. | 외부 입력 형식과 내부 상태 변경 책임이 결합된다. API 변경이 Domain에 직접 영향을 줄 수 있다. |

Day 2에서는 분리 자체를 관찰하는 것이 목적이므로 첫 번째 방식을 택했다. `record`를 사용한 이유도 “DTO는 반드시 record여야 한다”가 아니라, 두 문자열로 구성된 입력 계약을 간결하고 재할당 불가능하게 표현하기에 맞았기 때문이다.

## 3. 코드로 적용하기

```java
public record ReservationRequest(
        String roomName,
        String requesterName
) {}
```

`roomName`과 `requesterName`은 record 컴포넌트다. 값을 읽을 때는 `request.roomName()`과 `request.requesterName()`을 호출한다.

```java
public class Reservation {
    private final String roomName;
    private final String requesterName;
    private boolean confirmed;

    public Reservation(String roomName, String requesterName) {
        this.roomName = roomName;
        this.requesterName = requesterName;
        this.confirmed = false;
    }

    public void confirm() {
        this.confirmed = true;
    }
}
```

요청으로 받은 이름은 생성 시 정하고, 예약 확정 여부는 Domain 메서드가 바꾼다. 이 구조에서는 컨트롤러가 `confirmed` 필드를 직접 수정하지 않는다.

```java
@PostMapping("/reservations")
public String reserve(@RequestBody ReservationRequest request) {
    Reservation reservation = new Reservation(
            request.roomName(),
            request.requesterName()
    );
    reservation.confirm();

    return reservation.getRequesterName() + "님이 "
            + reservation.getRoomName() + " 예약 완료 (확정: "
            + reservation.isConfirmed() + ")";
}
```

컨트롤러는 HTTP 요청을 DTO로 받은 뒤 Domain 생성에 필요한 값만 전달한다. Day 2에는 Service나 Repository가 아직 없어서 객체 생성과 응답 문자열 조립까지 컨트롤러가 담당했다.

## 4. 예측 → 실행 → 차이 설명

| 구분 | 기록 |
|---|---|
| 실행 전 예측 | Domain getter처럼 `request.getRoomName()`도 동작할 것이라고 예상했다. |
| 실행 또는 테스트 | 해당 접근자로 컴파일한 뒤, record 컴포넌트 접근자로 수정했다. |
| 실제 결과 | `cannot find symbol: method getRoomName()` 컴파일 오류가 발생했다. `request.roomName()`으로 바꾸면 컴파일됐다. |
| 차이의 원인 | 일반 클래스에 직접 작성한 JavaBean 스타일 getter와 record가 생성하는 컴포넌트 접근자를 같은 규칙으로 생각했다. record 접근자는 컴포넌트 이름을 그대로 사용한다. |

두 번째 예측에서는 `roomname`과 `roomName`의 대소문자가 달라도 동작할 것으로 생각했지만 다시 `cannot find symbol`이 발생했다. Java 식별자는 대소문자를 구분하며, record에는 선언한 컴포넌트 이름과 정확히 같은 접근자만 생성된다.

반대로 철자가 틀린 메서드명과 URL 문자열도 정의와 호출이 맞으면 컴파일될 것이라는 예측은 실제와 같았다. 컴파일러는 타입과 심볼의 일관성을 검사하지만 식별자가 자연어상 올바른 단어인지, URL이 의도한 API 계약인지까지 판단하지 않는다.

## 5. 검증 근거

| 검증 대상 | 검증 방법 | 확인한 결과 |
|---|---|---|
| record 접근자 | `request.getRoomName()`과 대소문자가 다른 접근자로 컴파일 | 두 경우 모두 `cannot find symbol` 발생 |
| 예약 취소 정상 경로 | PowerShell에서 UTF-8 JSON 본문으로 `POST /reservations/cancel` 직접 호출 | `김민준님이301호 예약을 취소하셨습니다 (확정 : false)` 응답 확인 |
| 한글 JSON 전송 실패 경로 | `curl.exe -d`로 한글 JSON 전송 | `400 Bad Request`와 이어진 `Malformed input to a URL function` 기록 |
| 오타가 있는 식별자·경로 | 같은 오타를 정의부와 호출부에 사용해 컴파일·실행 | 컴파일과 실행이 통과해 정적 타입 검사의 범위를 확인 |

이 결과들은 [Day 2 학습 커밋](https://github.com/enderpawar/8week_Spring_Study/commit/975be06)과 당시 `explain-log.md`에 남긴 수동 검증 기록을 근거로 한다. 엔드포인트 동작을 고정하는 자동화 테스트는 작성하지 않았다.

## 6. 막힌 지점과 오답 교정

### JavaBean getter와 record 접근자를 같은 것으로 본 오답

증상은 `getRoomName()`을 찾을 수 없다는 컴파일 오류였다. 처음에는 Domain 객체에서 쓰던 getter 규칙이 record에도 적용된다고 생각했다. 하지만 이는 프레임워크 문제가 아니라 Java 타입과 메서드 이름에 대한 멘탈모델의 문제였다.

`roomName()`으로 고쳐 record의 컴포넌트 접근자 규칙을 확인했다. 대소문자 오류까지 이어졌기 때문에, 재발 방지 기준은 “getter처럼 보이는 이름을 추측하지 말고 record 선언의 컴포넌트 이름을 그대로 호출한다”로 정리했다. 이런 오류는 컴파일러가 즉시 잡아준다.

### 컴파일 성공을 API 계약의 정확성으로 해석할 수 없는 이유

`rerservations`, `cancle`, `cancled`처럼 의도와 다른 철자를 일관되게 사용했을 때도 컴파일과 실행은 통과했다. 문자열 경로에는 타입 수준의 계약이 없고, 메서드 식별자는 선언과 호출이 일치하면 유효하기 때문이다.

URL은 직접 호출해 확인했고 최종 Day 2 커밋에서는 `/reservations/cancel`로 남았다. 다만 Domain 메서드 이름은 `canceled()`로 커밋돼 있었다. 동작에는 문제가 없지만 “취소 명령”을 나타내는 이름으로는 `cancel()`이 더 정확하다. 이런 계약 오류를 지속적으로 막으려면 이후 HTTP 테스트가 필요하다.

### 한글 JSON 실패에서 확인한 것과 추정한 것을 구분하기

`curl.exe -d` 호출에서는 `400 Bad Request`와 curl의 URL 입력 오류가 기록됐고, `Invoke-RestMethod`에서 해시테이블을 `ConvertTo-Json`으로 변환해 보내자 정상 응답을 받았다. 당시에는 콘솔 인코딩과 PowerShell의 따옴표 해석이 함께 원인이라고 설명했다.

하지만 인코딩만 바꾼 실험과 quoting만 바꾼 실험을 따로 수행한 기록은 없다. 따라서 확실히 말할 수 있는 범위는 “PowerShell에서 수동 JSON 문자열을 curl 인자로 전달한 방식에 문제가 있었고, 네이티브 JSON 변환 방식으로 해결했다”까지다.

## 7. 현재 한계와 다음 개선

- **입력 검증 부재:** 빈 문자열이나 누락된 값도 DTO 변환 이후 코드로 들어올 수 있었다. 이 영향은 Day 3의 Bean Validation과 전역 오류 처리에서 다룬다.
- **자동화된 HTTP 테스트 부재:** 수동 호출은 그 시점의 동작만 확인한다. 경로 오타나 응답 계약의 회귀를 자동으로 감지하지 못한다.
- **컨트롤러의 많은 책임:** 컨트롤러가 Domain 생성, 상태 변경, 응답 문자열 조립을 모두 담당한다. Service와 Repository 책임 분리는 Day 4 범위다.
- **메모리 안의 일회성 객체:** 요청마다 새 `Reservation`을 만들 뿐 저장하거나 식별하지 않는다. 취소 요청도 기존 예약을 찾는 동작이 아니라 새 객체의 `false` 상태를 반환하는 예제였다.

## 8. 복습을 위한 인출 질문

### Q1. DTO와 Domain을 분리한 이유를 변경 원인의 관점에서 설명하면?

<details>
<summary>답 확인</summary>

DTO는 외부 요청의 형식이 바뀔 때 변경되고, Domain은 비즈니스 상태와 규칙이 바뀔 때 변경된다. 타입을 분리하면 HTTP 입력 계약의 변경이 내부 모델에 직접 퍼지는 것을 줄일 수 있다.

</details>

### Q2. `request.getRoomName()`이 아니라 `request.roomName()`인 이유는?

<details>
<summary>답 확인</summary>

Java record는 각 컴포넌트와 같은 이름의 접근자를 생성한다. `roomName` 컴포넌트의 접근자는 JavaBean 스타일의 `getRoomName()`이 아니라 `roomName()`이다.

</details>

### Q3. `record`를 사용하면 참조 대상까지 모두 불변인가?

<details>
<summary>답 확인</summary>

아니다. record 컴포넌트 자체는 다른 값으로 재할당할 수 없지만, 컴포넌트가 가변 객체를 참조한다면 그 객체 내부 상태는 바뀔 수 있다. 깊은 불변은 별도로 설계해야 한다.

</details>

### Q4. URL 오타가 컴파일을 통과할 수 있는 이유와 이를 잡을 방법은?

<details>
<summary>답 확인</summary>

URL은 문자열 데이터이므로 Java 컴파일러가 의도한 API 계약과 맞는지 판단하지 않는다. 실제 HTTP 호출이나 컨트롤러 테스트로 경로와 응답 계약을 검증해야 한다.

</details>

## 정리하며

처음에는 DTO와 Domain의 차이를 `record`와 `class`라는 문법 차이로만 정리했다. 실제 요청을 연결하고 접근자 오류를 교정하면서, 핵심은 문법이 아니라 외부 입력 계약과 내부 상태 변경 책임을 분리하는 데 있다는 점을 확인했다.

동시에 컴파일 성공은 타입과 심볼의 일관성을 보장할 뿐 URL 계약이나 자연어 철자까지 보장하지 않는다는 경계도 확인했다. 다음 단계에서는 입력 검증과 전역 오류 처리를 붙여 잘못된 요청이 어떤 HTTP 응답으로 바뀌는지 확인한다.
