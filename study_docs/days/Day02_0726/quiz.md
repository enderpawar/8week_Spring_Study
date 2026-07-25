# Day02 (7/26, Week A D2) 문제집 — record DTO vs Domain 분리

형식: 문제 → <details> 정답/해설 (접힘)

## Q1. DTO는 record로, Domain은 일반 class로 만드는 이유는?

<details>
<summary>정답/해설</summary>

DTO는 API가 주고받는 데이터의 "모양"만 정의하면 되고, 불변이어야 값이 실수로 안 바뀐다 → `record`.
Domain은 상태 + 그 상태를 바꾸는 규칙(행동, 예: `confirm()`)을 함께 캡슐화해야 하므로 가변이 필요 → 일반 `class`.
CS 연결: 불변 값 객체(스레드 세이프) vs OOP 캡슐화(상태+행동 결합).
</details>

## Q2. `record`의 접근자 메서드 이름 규칙은 JavaBean과 어떻게 다른가?

<details>
<summary>정답/해설</summary>

JavaBean 관례는 `getRoomName()`처럼 `get` 접두사를 붙이지만, `record`는 필드 이름 그대로 `roomName()`을 생성한다. 이건 record가 "getter 있는 객체"가 아니라 "데이터 그 자체"라는 설계 의도를 이름에도 반영한 것.
(오늘 실수: `request.getRoomName()`이라고 썼다가 컴파일 에러 — record는 `getRoomName()`이 없다.)
</details>

## Q3. 왜 컴파일러는 `"/rerservations/cancel"` 같은 URL 오타나 `cancle` 같은 메서드명 오타를 못 잡는가?

<details>
<summary>정답/해설</summary>

컴파일러는 "영어 철자가 맞는가"를 검사하지 않는다. 메서드명은 정의한 곳과 부르는 곳의 이름이 서로 일치하기만 하면 통과되고(오타를 똑같이 두 곳에 쓰면 문제없음), URL 문자열은 애초에 식별자가 아니라 그냥 데이터라서 컴파일러가 검사할 규칙 자체가 없다. 실제로 잡으려면 엔드포인트를 직접 호출해보거나 테스트 코드로 확인해야 한다.
</details>
