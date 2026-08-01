# [백엔드 기본기 Day2] JSON이 객체가 되기까지 — record DTO와 Domain class를 나눈 이유

Day1은 `HelloController` 하나로 GET 요청이 응답이 되는 길을 따라갔다. Day2는 스터디룸 예약을 소재로 `ReservationController`를 새로 만들고, 이번엔 클라이언트가 **JSON을 보내오는** 쪽을 봤다. 다루는 범위는 DTO와 Domain을 왜 다른 타입으로 두는지와 `@RequestBody`가 JSON을 객체로 바꾸는 지점까지고, 서비스·저장소 계층과 입력 검증은 이 글의 범위 밖이다.

> POST 두 개(`/reservations`, `/reservations/cancel`)를 만들어 JSON → record DTO → Domain 객체 → 상태 변경까지의 흐름을 손으로 확인했다. record의 접근자가 `getRoomName()`이 아니라는 걸 컴파일 에러로 배웠고, 오타 세 개가 컴파일과 실행을 모두 통과하는 것도 봤다. 검증은 전부 수동 호출이고 자동 테스트는 아직 없다.

## 1. 개념 설명

오늘 정리한 용어부터.

| 용어 | 한줄뜻 | 코드 모습 |
|---|---|---|
| record (DTO) | 데이터의 모양만 정의하는 불변 타입. 생성자·접근자·`equals`·`hashCode`를 컴파일러가 자동 생성한다 | `public record ReservationRequest(String roomName, String requesterName) {}` |
| record 접근자 이름 규칙 | 필드 이름 그대로가 접근자다. JavaBean의 `getXxx()` 관례를 따르지 않는다 | `request.roomName()` (O) / `request.getRoomName()` (X) |
| Domain 객체 | 상태와, 그 상태를 바꾸는 규칙(행동)을 함께 캡슐화하는 가변 객체 | `reservation.confirm()`이 `confirmed`를 내부에서만 바꾼다 |
| `@RequestBody` | 클라이언트가 보낸 JSON 본문을 자바 객체로 자동 변환(역직렬화)하는 애노테이션 | `reserve(@RequestBody ReservationRequest request)` |
| 컴파일러가 못 잡는 오타 | 정의한 이름과 부르는 이름이 일치하면 통과한다. "영어 철자가 맞는가"는 검사 대상이 아니다 | `"/rerservations/cancel"`도 컴파일 성공 |

**두 타입은 경쟁 관계가 아니라 맡은 역할이 다르다.** `ReservationRequest`는 시스템 경계를 넘어 들어온 데이터의 *모양*이고, `Reservation`은 그 데이터를 받아 규칙을 들고 있는 *주체*다.

그래서 요청 하나는 타입을 한 번 갈아타며 흐른다.

> JSON 본문 → `@RequestBody`(Jackson 역직렬화) → `ReservationRequest`(record) → `new Reservation(...)` → `confirm()` / `canceled()` → 응답 문자열

이 흐름이 표의 용어들을 하나로 엮는다. `@RequestBody`가 붙으면 Jackson이 리플렉션으로 record의 생성자를 호출하는데, record는 필드가 곧 생성자 파라미터라서 **DTO 쪽에는 내가 쓸 코드가 사실상 없다.** 대신 Domain 쪽에는 `confirm()` 같은 행동을 직접 써야 한다.

"Domain은 가변"이라는 말도 전부 바뀐다는 뜻은 아니었다. `Reservation`에서 `roomName`과 `requesterName`은 `final`이고, 규칙에 따라 바뀌는 건 `confirmed` 하나다.

- 불변이어야 하는 값 → `final` 필드로 잠근다
- 규칙에 따라 바뀌는 값 → `private`으로 두고 메서드로만 바꾼다

이게 캡슐화가 코드에 남는 모습이다. 소스에도 그 의도를 주석으로 남겨뒀다 — "상태를 바꾸는 규칙을 객체 안에 캡슐화 - 외부에서 필드를 직접 못 건드리게 하기 위해서."

용어 정리에서 한 가지 뒤집힌 게 있다. 처음엔 DTO와 record를 다른 것으로 생각했는데, **DTO는 역할 이름이고 record는 그 역할을 만드는 자바 문법**이다. 그래서 소스 주석에도 "record가 DTO 그 자체다"라고 적었다.

마지막으로 클래스 밖의 것 하나. 코드를 쓰다 "왜 파일 맨 위에 `package`를 굳이 써주지?"라는 의문이 생겼는데, 이건 자바 파일의 **도로명 주소**였다. 같은 이름의 클래스가 다른 라이브러리에도 있을 수 있으니 `com.example.studyroom.domain.Reservation`처럼 전체 주소를 부여해 충돌을 막고, 컴파일러는 이 선언으로 파일이 `src/main/java/com/example/studyroom/domain/`에 있다고 인식한다. 다른 패키지에서 `import`로 가져다 쓸 수 있는 것도 이 주소가 있기 때문이다.

> **더 볼 것**
> - [Records — Java Language Reference (Java 17)](https://docs.oracle.com/en/java/javase/17/language/records.html): 접근자 이름 규칙과 자동 생성 멤버의 근거
> - [@RequestBody — Spring Framework Reference](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/requestbody.html): 요청 본문이 객체로 변환되는 단계
> - 아직 안 본 것 — Jackson이 record를 역직렬화하는 내부 동작, JavaBean 관례 자체, 오타를 잡아줄 테스트 코드와 IDE 철자 검사 설정

## 2. 코드 구현

### 같은 두 필드를 서로 다른 타입에 담기

DTO 쪽은 한 줄로 끝났다.

```java
// record = 데이터 모양만 정의. 생성자, getter, equals, hashcode 자동 생성.
public record ReservationRequest(String roomName, String requesterName) {
}
```

Domain 쪽은 같은 두 필드를 들고 있으면서도 형태가 달랐다.

```java
public class Reservation {
    private final String roomName;
    private final String requesterName;
    private boolean confirmed;   // 아직 확인되면 안되니까 기본값은 false

    public void confirm()  { this.confirmed = true; }
    public void canceled() { this.confirmed = false; }
    // getRoomName(), getRequesterName(), isConfirmed() ...
}
```

왜 여기만 record가 아닌지도 주석으로 남겼다 — "record 형태가 아님. 상태가 바뀔 수 있어야하니까." 판단의 기준은 필드 개수가 아니라 **바뀌어야 하는 상태가 있는가**였다.

### `@RequestBody` — JSON이 객체가 되는 자리

```java
@PostMapping("/reservations")
public String reserve(@RequestBody ReservationRequest request) {
    Reservation reservation = new Reservation(request.roomName(), request.requesterName());
    reservation.confirm();
    return reservation.getRequesterName() + "님이 " + reservation.getRoomName()
            + " 예약 완료 (확정: " + reservation.isConfirmed() + ")";
}
```

DTO에서 값을 꺼낼 땐 `request.roomName()`, Domain에서 꺼낼 땐 `reservation.getRoomName()`이다. **한 메서드 안에 두 가지 명명 규칙이 공존**하는데, 이건 실수가 아니라 두 타입의 성격 차이가 이름에 드러난 것이다.

취소 쪽에서는 이름이 층마다 다 달라졌다. URL은 `/reservations/cancel`, 컨트롤러 메서드는 `cancel()`, 도메인 메서드는 `canceled()`다. Day1에 정리한 "URL 경로와 자바 메서드 이름은 목적이 다르다"가 여기서 한 겹 더 늘어난 셈이다. (노트에는 도메인 쪽을 `cancel()`로 적어뒀는데, 이 커밋의 실제 코드는 `canceled()`다. 커밋을 기준으로 적는다.)

### 한글 JSON이 400으로 돌아왔을 때 — 확인한 것과 추정한 것

`curl.exe -d`로 한글이 섞인 JSON(`"301호"`, `"김민준"`)을 보내니 요청이 통과하지 않았다. 여기서는 관찰과 해석을 나눠 적는다.

**확인한 것**

- `400 Bad Request`가 돌아왔고, 이어서 curl 자체가 `Malformed input to a URL function` 에러를 냈다
- 같은 값을 PowerShell 네이티브 방식으로 보내니 요청이 정상 처리됐다

**추정한 것 (분리해서 재현하지 않음)**

- Windows 콘솔의 기본 인코딩이 UTF-8이 아니라 한글 바이트가 깨졌다
- 수동으로 이스케이프한 큰따옴표(`\"`)를 PowerShell이 한 번 더 해석하면서 인자가 깨졌다

두 원인을 각각 끄고 켜보지 않았기 때문에, 둘 중 하나만으로도 실패했는지 둘이 겹쳐야 실패했는지는 오늘 확인하지 못했다. 해결 방법만 확실하다.

```powershell
$body = @{ roomName = "301호"; requesterName = "김민준" } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/reservations" -Method Post `
    -Body $body -ContentType "application/json; charset=utf-8"
```

JSON 문자열을 손으로 조립하지 않고 해시테이블을 `ConvertTo-Json`에 넘기니, 이스케이프와 인코딩을 셸이 처리해서 문제가 사라졌다.

### 오늘 확인한 것

전부 **수동 확인**이고, 이 두 엔드포인트를 고정하는 **자동 테스트는 없다.**

- `POST /reservations` — `Invoke-RestMethod`로 전환한 뒤 정상 처리됨. 응답 본문 원문은 기록해두지 않아 여기 옮기지 않는다
- `POST /reservations/cancel` — 예측대로 `confirmed`가 `false`로 나왔다

```
김민준님이301호 예약을 취소하셨습니다 (확정 : false)
```

오늘 코드는 [`975be06` 커밋](https://github.com/enderpawar/8week_Spring_Study/commit/975be06)에 있다.

## 3. 스스로 답한 질문

### Q. `request.getRoomName()`은 왜 컴파일이 안 됐을까?

DTO에서 값을 꺼내려고 자연스럽게 `request.getRoomName()`이라고 썼다. Domain에 직접 만들어둔 getter가 그 이름이었으니 **DTO도 당연히 될 거라고 예측**했는데 `cannot find symbol`이 났다.

틀린 지점은 "getter는 다 `getXxx()`"라고 뭉뚱그린 것이었다. record가 만들어주는 접근자는 컴포넌트 이름 그대로인 `roomName()`이고, 이건 record가 "getter를 가진 객체"가 아니라 "데이터 그 자체"라는 설계 의도를 이름에 남긴 것이다.

여기에 컴포넌트명을 `roomname`(소문자 n)으로 쓴 것까지 겹쳐 같은 에러를 한 번 더 만났다. 자바는 대소문자를 구분하니 `roomName`으로 선언해야 `roomName()` 접근자가 생긴다. 재발 방지는 규칙을 외우는 쪽이 아니라 **선언부를 먼저 보고 접근자 이름을 확인하는 것**으로 잡았다.

### Q. 오타 세 개가 왜 컴파일도 실행도 통과했을까?

`confirm()`과 대칭되는 취소 기능을 혼자 만들어보는 과제였는데, 다 만들고 보니 `rerservations`·`cancle`·`cancled` 세 군데가 오타였다. 그런데 **컴파일도 되고 실행도 정상**이었고, 이건 예측한 그대로였다.

컴파일러는 "영어 철자가 맞는가"를 검사하지 않는다. 메서드 이름은 정의한 곳과 부르는 곳이 서로 일치하기만 하면 통과되고 — 오타를 똑같이 두 군데에 쓰면 아무 문제가 없다 — URL 문자열은 애초에 식별자가 아니라 그냥 데이터라서 컴파일러가 검사할 규칙 자체가 없다.

**오늘 이 오타를 실제로 드러낸 건 엔드포인트를 직접 호출해본 것이었다.** IDE 철자 검사처럼 컴파일 밖에서 잡아주는 도구도 있고 나중에 배울 테스트도 있지만, 오늘 쓴 수단은 수동 호출 하나였다.

### Q. 왜 파일 맨 위에 `package`를 굳이 써주지?

이건 오답이 아니라 코드를 쓰다 생긴 의문이라 소스 주석에 그대로 남겨뒀다. 답은 개념 설명 절에 정리한 그대로 — 클래스 이름 충돌을 막는 전체 주소이자, 컴파일러가 파일의 물리 경로를 인식하는 근거이자, 다른 패키지에서 `import`로 가져올 수 있게 하는 전제다. 컨트롤러가 `import com.example.studyroom.domain.Reservation;`으로 도메인을 가져다 쓸 수 있는 이유가 여기 있었다.

## 4. 정리하며

오늘 바뀐 건 "DTO는 record, Domain은 class"라는 문장 자체가 아니라 **그 선택의 기준**이다. 타입을 고를 때 필드 개수나 편의가 아니라 "이 값이 바뀌어야 하는가, 바뀐다면 그 규칙은 누가 지키는가"를 먼저 묻게 됐다. 접근자 이름이 두 타입에서 다른 것도 그 기준이 이름까지 내려온 결과였다.

두 번째로 남는 건 컴파일 성공의 의미가 좁아졌다는 것이다. 어제는 컴파일 에러 네 개를 고치는 게 목표였는데, 오늘은 오타 세 개를 그대로 안고도 빌드가 통과했다. 통과했다는 건 이름이 서로 맞았다는 뜻이지 의도한 대로 동작한다는 뜻이 아니었다.

남은 것도 있다. 첫째, 오늘 만든 두 엔드포인트를 고정하는 자동 테스트가 없다. 지금은 오타나 경로 변경이 생겨도 빌드는 통과하고 수동 호출 때만 드러나므로, **예약된 부채**로 두고 테스트를 제대로 배우는 시점에 갚는다. 둘째, `cancel` 요청이 취소하는 대상은 방금 `new`로 만든 객체다. 저장된 예약을 찾아 취소하는 게 아니라서 취소가 어디에도 남지 않는데, 이건 저장소 계층을 배워야 제대로 풀리는 문제라 역시 **예약된 부채**다.

남겨두는 질문 — record에 검증 규칙(예: 빈 방 이름 거부)을 넣어야 한다면 그건 DTO의 일인가 Domain의 일인가?

오늘 공부한 소스코드: [8week_Spring_Study/app](https://github.com/enderpawar/8week_Spring_Study/tree/master/app)
