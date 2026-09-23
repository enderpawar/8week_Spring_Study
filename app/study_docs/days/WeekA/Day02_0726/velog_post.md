# [백엔드 기본기 Day2] 요청 데이터와 Domain Model의 분리 — record DTO와 Domain class

Day1은 `HelloController` 하나로 GET 요청이 응답이 되는 길을 따라갔다. Day2는 스터디룸 예약을 소재로 `ReservationController`를 만들고, 클라이언트가 **JSON을 보내오는** 방향을 봤다. 다루는 범위는 DTO와 Domain을 다른 타입으로 두는 기준과 `@RequestBody`가 JSON을 객체로 바꾸는 지점까지다. 서비스·저장소 계층과 입력 검증은 범위 밖이다.

> POST 두 개(`/reservations`, `/reservations/cancel`)를 만들어 JSON → record DTO → Domain 객체 → 상태 변경까지의 흐름을 손으로 확인했다. record의 접근자가 `getRoomName()`이 아니라는 것을 `cannot find symbol`로 배웠고, 오타 세 개가 컴파일과 실행을 모두 통과하는 것도 봤다. 검증은 전부 수동 호출이고 자동 테스트는 아직 없다.

## 1. 개념 설명

| 용어 | 한줄뜻 | 현재 프로젝트 적용 지점 |
|---|---|---|
| record (DTO) | 데이터의 모양만 정의하는 불변 타입. 생성자·접근자·`equals`·`hashCode`를 컴파일러가 생성 | `ReservationRequest` |
| record 접근자 이름 규칙 | 컴포넌트 이름 그대로가 접근자. JavaBean의 `getXxx()`를 따르지 않음 | `request.roomName()` |
| Domain 객체 | 상태와 그 상태를 바꾸는 규칙(행동)을 함께 캡슐화하는 객체 | `Reservation.confirm()`, `canceled()` |
| `@RequestBody` | 요청 본문 JSON을 자바 객체로 변환(역직렬화)하라는 지시 | `reserve(@RequestBody ReservationRequest request)` |
| 컴파일러가 못 잡는 오타 | 정의와 호출의 이름이 일치하면 통과. 영어 철자는 검사하지 않음 | `"/rerservations/cancel"`도 컴파일 성공 |

### 시스템 경계의 데이터와 도메인 상태

같은 두 값(`roomName`, `requesterName`)을 담는데도 타입을 둘로 나눈 이유는 두 타입이 서로 다른 질문에 답하기 때문이다.

- `ReservationRequest`: 시스템 경계를 넘어 **들어온 데이터의 모양**은 무엇인가
- `Reservation`: 그 데이터로 만든 예약이 **어떤 상태를 갖고 어떤 규칙으로 바뀌는가**

한 타입으로 두 역할을 맡기면 경계가 흐려진다. 요청 JSON의 모양이 바뀔 때 도메인 규칙을 가진 클래스도 같이 흔들리고, 반대로 `confirmed` 같은 내부 상태가 외부 입력 형식에 섞여 들어갈 여지가 생긴다. 오늘은 이 설계 판단의 근거만 세웠고, 도메인 객체를 요청 본문으로 직접 받는 대조군은 실행하지 않았다(미검증).

DTO는 **Data Transfer Object**, 즉 계층이나 시스템 사이에서 데이터를 옮기는 역할의 이름이다. record는 그 역할을 만드는 Java 문법이다. 처음엔 DTO와 record를 다른 것으로 생각했는데, 소스 주석에 "record가 DTO 그 자체다"라고 적으며 역할과 문법의 관계로 정리했다.

> **정리.** DTO는 역할 이름이고 record는 그 역할을 구현한 문법이다. Domain은 데이터가 아니라 상태와 규칙을 가진 주체다.

### JSON Deserialization 순서

Day1에서는 반환값이 메시지 컨버터를 거쳐 응답 본문이 됐다. `@RequestBody`는 같은 장치를 반대 방향으로 쓴다.

```text
클라이언트: POST /reservations, Content-Type: application/json
→ DispatcherServlet이 reserve() 매핑을 찾음
→ @RequestBody 파라미터 발견
→ JSON 메시지 컨버터(Jackson)가 본문을 읽어 ReservationRequest 생성
→ reserve(request) 본문 실행
→ new Reservation(...) → confirm()
→ 반환 문자열이 응답 본문으로 변환
```

Jackson은 JSON의 키 이름(`roomName`, `requesterName`)을 record 컴포넌트에 맞춰 생성자를 호출한다. record는 컴포넌트 선언이 곧 생성자 파라미터라서 **DTO 쪽에는 내가 쓸 코드가 사실상 없다.** 대신 Domain 쪽에는 `confirm()` 같은 행동을 직접 써야 한다.

이 순서에서 컨트롤러 메서드 본문은 변환이 끝난 뒤에야 실행된다. 즉 `reserve()`가 받는 것은 이미 만들어진 `ReservationRequest` 객체다. 변환 과정의 내부 동작(Jackson이 record 생성자를 찾는 방식)은 오늘 열어보지 않았다.

### record의 자동 생성 멤버와 접근자 이름

record는 `record ReservationRequest(String roomName, String requesterName) {}` 한 줄로 다음을 만든다.

- `private final` 필드 두 개
- 두 값을 받는 생성자
- 접근자 `roomName()`, `requesterName()`
- `equals()`, `hashCode()`, `toString()`

여기서 오늘 틀린 지점이 접근자 이름이었다.

| 구분 | record | JavaBean 관례 클래스 |
|---|---|---|
| 접근자 이름 | `roomName()` | `getRoomName()` |
| 누가 만드나 | 컴파일러가 자동 생성 | 개발자가 직접 작성 |
| 값 변경 | 불가(필드가 `final`) | setter가 있으면 가능 |

접근자에 `get`이 붙지 않는 것은 record가 "getter를 가진 객체"가 아니라 "데이터 그 자체"라는 설계 의도가 이름에 드러난 것이다. 접근자 이름은 컴포넌트 이름에서 그대로 만들어지므로 대소문자까지 일치해야 한다. `roomname`으로 선언하면 생기는 접근자는 `roomname()`이다.

보장 범위도 구분한다. record의 불변은 **필드 재할당을 막는다는 뜻**이다. 필드가 가리키는 객체 내부까지 얼리지는 않지만, 오늘 컴포넌트는 불변 타입인 `String`뿐이라 이 차이가 드러나지 않았다.

### Domain Model의 Encapsulation

`Reservation`은 일반 `class`다. 소스 주석의 이유는 "record 형태가 아님. 상태가 바뀔 수 있어야하니까"였다. 판단 기준은 필드 개수가 아니라 **바뀌어야 하는 상태가 있는가**다.

다만 "Domain은 가변"이 모든 필드가 바뀐다는 뜻은 아니었다.

```text
new Reservation("301호", "김민준")
→ roomName, requesterName: final로 고정
→ confirmed = false (생성자에서 초기화)
→ confirm() 호출 → confirmed = true
→ canceled() 호출 → confirmed = false
```

- 바뀌면 안 되는 값: `final` 필드로 잠근다
- 규칙에 따라 바뀌는 값: `private`으로 숨기고 메서드로만 바꾼다

외부 코드는 `reservation.confirmed = true`처럼 필드를 직접 바꿀 수 없다. 상태 변경은 반드시 `confirm()`·`canceled()`를 거친다. 나중에 "이미 취소된 예약은 확정할 수 없다" 같은 규칙이 생기면 그 메서드 안에만 추가하면 된다. 소스 주석도 이 의도를 적어뒀다: "상태를 바꾸는 규칙을 객체 안에 캡슐화 - 외부에서 필드를 직접 못 건드리게 하기 위해서."

CS 개념으로는 두 축이 만난다. DTO 쪽은 **불변 값 객체**로, 한 번 만들어진 값이 실수로 바뀌는 버그를 원천 차단한다. Domain 쪽은 **OOP 캡슐화**로, 상태와 그 상태를 바꾸는 행동을 한 객체에 묶는다.

![클래스 다이어그램. ReservationController가 ReservationRequest를 «use»하고 Reservation을 «create»한다. «record» ReservationRequest는 roomName·requesterName이 둘 다 public에 {readOnly}이고 접근자가 roomName()·requesterName()이라 getRoomName()은 생성되지 않는다. Reservation은 roomName·requesterName이 private {readOnly}이고 confirmed만 가변인데 그마저 private이라, 외부는 confirm()·canceled()로만 상태를 바꿀 수 있다.](../../../assets/day02-dto-domain.png)
<!-- velog 업로드: 이 줄 위 이미지 자리에 app/study_docs/assets/day02-dto-domain.png 파일을 드래그해 교체 -->

그림의 `ReservationController`에는 `reserve()`만 있지만, 같은 커밋의 `cancel()`도 같은 방식으로 `ReservationRequest`를 받고 `Reservation`을 만든다. 한 메서드 안에서 DTO는 `request.roomName()`, Domain은 `reservation.getRoomName()`으로 꺼낸다. 두 명명 규칙이 공존하는 것은 실수가 아니라 두 타입의 성격 차이가 이름에 드러난 결과다.

> **정리.** record와 class를 가르는 기준은 "바뀌어야 하는 상태가 있는가, 있다면 그 규칙을 누가 지키는가"다.

### 이름과 Identifier의 검사 범위

취소 기능을 혼자 만들다 `rerservations`·`cancle`·`cancled` 세 군데에 오타를 냈다. 컴파일도 실행도 통과했다.

| 대상 | 컴파일러가 보는 것 | 보지 않는 것 |
|---|---|---|
| 메서드 이름 | 정의한 이름과 호출한 이름의 일치 | 영어 철자가 맞는가 |
| URL 문자열 | 문자열 리터럴 문법 | 내용 자체(식별자가 아니라 데이터) |

메서드 이름은 오타를 정의와 호출 양쪽에 똑같이 쓰면 문제가 없다. URL은 애초에 문자열 데이터라 컴파일러가 검사할 규칙이 없다. 이것이 정적 타입 검사의 한계다. 컴파일 성공은 "이름이 서로 맞았다"는 뜻이지 "의도대로 동작한다"는 뜻이 아니다.

오늘 오타를 드러낸 수단은 엔드포인트를 직접 호출해본 것이었다. IDE 철자 검사와 자동 테스트도 이 빈틈을 채울 수 있지만 오늘은 쓰지 않았다.

URL·컨트롤러 메서드·도메인 메서드의 이름은 결국 `/reservations/cancel`, `cancel()`, `canceled()`로 층마다 달라졌다. Day1의 "URL 경로와 메서드 이름은 목적이 다르다"가 한 겹 더 늘어난 셈이다. 노트에는 도메인 쪽을 `cancel()`로 적었지만, 커밋의 실제 코드는 `canceled()`다. 커밋을 기준으로 적는다.

### package 선언과 Namespace

코드를 쓰다 "왜 파일 맨 위에 `package`를 굳이 써주지?"라는 의문이 생겼고, 답을 `Reservation.java` 주석에 남겼다. `package`는 클래스의 **전체 이름**을 정한다.

```text
package com.example.studyroom.domain;  +  class Reservation
→ 전체 이름: com.example.studyroom.domain.Reservation
→ 다른 라이브러리의 Reservation과 충돌하지 않음
→ 컨트롤러는 import com.example.studyroom.domain.Reservation; 으로 참조
```

주석에서는 이를 자바 파일의 "도로명 주소"로 비유했다. 이름이 같은 클래스가 여러 곳에 있어도 전체 주소가 다르면 구분된다. 다른 패키지에서 `import`로 가져다 쓸 수 있는 것도 이 주소가 있기 때문이다.

주석은 "컴파일러가 이 선언으로 파일이 `src/main/java/com/example/studyroom/domain/`에 있다고 인식한다"고도 적었다. 이 부분은 범위를 좁혀야 한다. 패키지와 폴더를 일치시키는 것은 관례이고 빌드 도구와 IDE가 이를 전제로 동작하지만, 불일치 자체가 곧 컴파일 에러는 아니다. 실제로 같은 커밋의 `HelloController.java`는 `studyroom/` 폴더에 있으면서 `package com.example.studyroom.controller;`를 선언한 상태였다.

> **더 볼 것**
> - [Records — Java Language Reference (Java 17)](https://docs.oracle.com/en/java/javase/17/language/records.html): 접근자 이름 규칙과 자동 생성 멤버의 근거
> - [@RequestBody — Spring Framework Reference](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/requestbody.html): 요청 본문이 객체로 변환되는 단계
> - 아직 안 본 것 — Jackson이 record를 역직렬화하는 내부 동작, JavaBean 관례 자체, 오타를 잡아줄 테스트 코드와 IDE 철자 검사 설정

## 2. 코드 구현

### 같은 두 필드를 담은 두 타입

```java
// record = 데이터 모양만 정의. 생성자, getter, equals, hashcode 자동 생성.
public record ReservationRequest(String roomName, String requesterName) {
}

public class Reservation{
    private final String roomName;
    private final String requesterName;
    private boolean confirmed;

    public void confirm() { this.confirmed = true; }
    public void canceled() { this.confirmed = false; }
    // 생성자, getRoomName(), getRequesterName(), isConfirmed()
}
```

DTO는 한 줄로 끝나고, Domain은 같은 두 필드를 들고도 `final`과 가변 필드, 행동 메서드를 직접 갖는다. 두 파일의 길이 차이가 곧 두 역할의 차이다.

### `@RequestBody` 파라미터와 DTO→Domain 변환

```java
@PostMapping("/reservations")
public String reserve(@RequestBody ReservationRequest request) {
    Reservation reservation = new Reservation(request.roomName(), request.requesterName());
    reservation.confirm();
    return reservation.getRequesterName() + "님이 " + reservation.getRoomName()
            + " 예약 완료 (확정: " + reservation.isConfirmed() + ")";
}
```

`cancel()`은 같은 구조에서 `confirm()` 대신 `canceled()`를 호출한다. 컨트롤러가 하는 일은 DTO에서 값을 꺼내 Domain을 만들고 행동을 호출하는 것까지다.

### 한글 JSON 전송 실패와 추정 원인

`curl.exe -d`로 한글이 섞인 JSON(`"301호"`, `"김민준"`)을 보내니 요청이 통과하지 않았다. 관찰과 해석을 나눠 적는다.

**확인한 것**

- `400 Bad Request`가 돌아왔고, 이어서 curl 자체가 `Malformed input to a URL function` 에러를 냈다
- 같은 값을 PowerShell 네이티브 방식으로 보내니 정상 처리됐다

**추정한 것 (분리해서 재현하지 않음)**

- Windows 콘솔의 기본 인코딩이 UTF-8이 아니라 한글 바이트가 깨졌다
- 수동으로 이스케이프한 큰따옴표(`\"`)를 PowerShell이 한 번 더 해석하면서 인자가 깨졌다

두 원인을 각각 끄고 켜보지 않았으므로 어느 쪽이 결정적이었는지는 확인하지 못했다. 해결 방법만 확실하다. 해시테이블을 `ConvertTo-Json`에 넘기고 `Invoke-RestMethod`에 `-ContentType "application/json; charset=utf-8"`로 보내자, 이스케이프와 인코딩을 셸이 처리해서 문제가 사라졌다.

### 자동 검증 결과

| 요청 | 결과 | 확인 방법 |
|---|---|---|
| `POST /reservations` | `Invoke-RestMethod` 전환 후 정상 처리(응답 원문 미기록) | 수동 |
| `POST /reservations/cancel` | `김민준님이301호 예약을 취소하셨습니다 (확정 : false)` | 수동 |

두 엔드포인트를 고정하는 **자동 테스트는 없다.** 오늘 코드는 [`975be06` 커밋](https://github.com/enderpawar/8week_Spring_Study/commit/975be06)에 있다.

## 3. 스스로 답한 질문

### Q1. record 접근자 호출의 컴파일 에러

**질문.** `request.getRoomName()`은 왜 컴파일되지 않았을까?

**A1.** Domain에 직접 만든 getter가 `getRoomName()`이었으니 DTO도 당연히 될 거라고 예측했다. 결과는 `cannot find symbol`이었다.

틀린 지점은 "getter는 다 `getXxx()`"라고 뭉뚱그린 것이다. record가 만드는 접근자는 컴포넌트 이름 그대로인 `roomName()`이다.

여기에 컴포넌트명을 `roomname`(소문자 n)으로 쓴 것까지 겹쳐 같은 에러를 한 번 더 만났다. Java는 대소문자를 구분하므로 `roomName`으로 선언해야 `roomName()` 접근자가 생긴다. 재발 방지는 규칙을 외우는 것이 아니라 **선언부를 먼저 보고 접근자 이름을 확인하는 것**으로 잡았다.

### Q2. 오타가 컴파일과 실행을 통과한 원인

**질문.** `rerservations`·`cancle`·`cancled` 오타가 왜 컴파일도 실행도 통과했을까?

**A2.** 이 결과는 예측과 같았다. 정의와 호출에 같은 오타를 썼으니 이름이 일치했고, 컴파일러는 영어 철자를 검사하지 않는다. URL 문자열은 식별자가 아니라 데이터라서 검사 규칙 자체가 없다. 오타를 실제로 드러낸 것은 엔드포인트를 직접 호출해본 것이었다.

### Q3. `package` 선언의 역할

**질문.** 왜 파일 맨 위에 `package`를 굳이 써주지?

**A3.** 오답이 아니라 코드를 쓰다 생긴 의문이라 소스 주석에 답을 남겼다. 클래스 이름 충돌을 막는 전체 주소이고, 다른 패키지에서 `import`로 가져올 수 있게 하는 전제다. 컨트롤러가 `import com.example.studyroom.domain.Reservation;`으로 도메인을 쓸 수 있는 이유가 여기 있다. 폴더 위치와의 관계는 개념 설명에서 범위를 좁혀 정리했다.

## 4. 학습 정리와 다음 범위

오늘 바뀐 것은 "DTO는 record, Domain은 class"라는 문장이 아니라 **그 선택의 기준**이다. 타입을 고를 때 필드 개수나 편의가 아니라 "이 값이 바뀌어야 하는가, 바뀐다면 그 규칙은 누가 지키는가"를 먼저 묻게 됐다. 접근자 이름이 두 타입에서 다른 것도 그 기준이 이름까지 내려온 결과였다.

컴파일 성공의 의미도 좁아졌다. Day1에는 컴파일 에러 네 개를 고치는 게 목표였는데, 오늘은 오타 세 개를 안고도 빌드가 통과했다. 통과했다는 것은 이름이 서로 맞았다는 뜻이지 의도대로 동작한다는 뜻이 아니다.

**아직 남은 것**은 두 가지다. 첫째, 두 엔드포인트를 고정하는 자동 테스트가 없어 오타나 경로 변경이 수동 호출 때만 드러난다. **나중에 고칠 것(Week A D7)**으로 분류했다. 둘째, `cancel` 요청이 취소하는 대상은 방금 `new`로 만든 객체라 취소가 어디에도 남지 않는다. 저장소 계층을 배우는 Day4에서 다룰 **나중에 고칠 것**이다.

남겨두는 질문 — 빈 방 이름을 거부하는 규칙은 DTO가 맡아야 하는가, Domain이 맡아야 하는가?

---

오늘 공부한 소스코드: `app/src/main/java/com/example/studyroom/controller/ReservationController.java`, `domain/Reservation.java`, `dto/ReservationRequest.java` ([`975be06`](https://github.com/enderpawar/8week_Spring_Study/commit/975be06))
