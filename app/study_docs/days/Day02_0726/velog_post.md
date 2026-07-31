# [백엔드 기본기 Day2] record DTO와 Domain을 나눠보며 겪은 오타 소동

> Day2는 스터디룸 예약 도메인으로 DTO와 Domain을 처음 분리해본 날이다. 개념 자체는 짧게 끝났는데, 그 개념을 코드로 옮기는 과정에서 오타·인코딩·컴파일 에러를 연달아 만나면서 오히려 더 오래 기억에 남을 하루가 됐다.

## 1. 완성예제 — DTO는 record, Domain은 class

```java
// DTO — 데이터 모양만, 불변
public record ReservationRequest(String roomName, String requesterName) {}
```
```java
// Domain — 상태 + 행동을 함께, 가변
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
    // ...
}
```

DTO는 "데이터의 모양"만 정의하면 되니 불변(`record`)으로, Domain은 `confirm()`처럼 상태를 바꾸는 규칙(행동)을 함께 들고 있어야 하니 가변 `class`로 나눈다는 게 오늘의 개념이었다. OOP 시간에 배운 캡슐화 원칙 그대로였다.

## 2. record 접근자 함정 — `getRoomName()`이 없다

컨트롤러에서 DTO 값을 꺼내려고 자연스럽게 `request.getRoomName()`이라고 썼다가 컴파일 에러부터 났다.

```
error: cannot find symbol
  symbol: method getRoomName()
```

`record`가 자동 생성하는 접근자는 JavaBean 관례(`getXxx()`)가 아니라 **필드 이름 그대로**(`roomName()`)였다. Domain 클래스에는 내가 직접 `getRoomName()`처럼 전통적인 getter를 써놨으니, 한 파일 안에 두 가지 명명 규칙이 공존하는 셈이었다. record는 "getter 있는 객체"가 아니라 "데이터 그 자체"라는 설계 의도가 이름에도 드러난다는 걸 에러를 통해 배웠다.

여기에 필드명을 `roomname`(소문자 n)으로 오타 낸 것까지 겹쳐서, 같은 종류의 에러를 두 번 연달아 만났다 — Java는 대소문자를 구분하니 `roomName` 컴포넌트여야 `roomName()` 접근자가 생긴다.

## 3. 한글 JSON 전송이 안 되던 이유

`curl.exe -d`로 한글이 섞인 JSON(`"301호"`, `"김민준"`)을 보내니 `400 Bad Request`가 났고, 이어서 curl 자체가 `Malformed input to a URL function` 에러까지 냈다. 원인은 두 가지가 겹친 거였다.

- Windows 콘솔 기본 인코딩이 UTF-8이 아니라서 한글 바이트가 깨짐
- 수동으로 이스케이프한 큰따옴표(`\"`)를 PowerShell이 한 번 더 해석하면서 인자 자체가 깨짐

해결은 curl 대신 PowerShell 네이티브 방식으로 바꾸는 것이었다.

```powershell
$body = @{ roomName = "301호"; requesterName = "김민준" } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/reservations" -Method Post -Body $body -ContentType "application/json; charset=utf-8"
```

PowerShell이 JSON 인코딩을 알아서 처리하게 두니 문제가 한 번에 사라졌다.

## 4. 독립 변형(`/reservations/cancel`)과 오타 3종 세트

`confirm()`과 대칭되는 `cancel()`을 혼자 만들어보라는 과제였는데, 완성하고 보니 오타가 세 군데(`rerservations`, `cancle`, `cancled`)나 있었다. 그런데 **컴파일도 되고 실행도 정상**이었다.

이게 오늘 가장 인상 깊었던 지점이다 — 컴파일러는 "영어 철자가 맞는가"를 검사하지 않는다. 메서드 이름은 정의한 곳과 부르는 곳이 서로 일치하기만 하면 통과되고(오타를 똑같이 두 군데 쓰면 아무 문제 없음), URL 문자열은 애초에 식별자가 아니라 그냥 데이터라서 컴파일러가 검사할 규칙 자체가 없다. 실제로 이런 오타를 잡으려면 엔드포인트를 직접 호출해보거나, 나중에 배울 테스트 코드가 있어야 한다는 걸 체감했다.

오타를 고치고 나서 `/reservations/cancel`을 호출하니 예상대로 나왔다.

```
김민준님이301호 예약을 취소하셨습니다 (확정 : false)
```

## 정리하며

오늘의 핵심은 "DTO는 record, Domain은 class"라는 한 줄이었지만, 그 한 줄을 코드로 옮기는 과정에서 **record 명명 규칙, 인코딩, 컴파일러의 한계**까지 세 가지를 덤으로 배웠다. 특히 "컴파일러가 통과시켜준다고 코드가 맞는 게 아니다"라는 걸 오타로 직접 겪어본 게 오늘 제일 오래 남을 것 같다.

---
#Spring #SpringBoot #Backend #TIL #백엔드기본기로드맵 #record #DTO #Domain

오늘 공부한 소스코드: [8week_Spring_Study/app](https://github.com/enderpawar/8week_Spring_Study/tree/master/app)
