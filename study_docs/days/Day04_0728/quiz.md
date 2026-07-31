# Day04 (7/28→7/31, Week A D4) 문제집 — Service/Repository 책임 분리

형식: 문제 → <details> 정답/해설 (접힘)

## Q1. `ReservationController`가 하던 일을 `Service`와 `Repository`로 나눴다. 각각 무슨 책임을 지며, Controller에 다 몰아넣으면 안 되는 이유는?

<details>
<summary>정답/해설</summary>

Controller는 HTTP 요청/응답 처리(라우팅, DTO 변환)만 담당하고, Service는 비즈니스 로직(상태 변경 규칙)을, Repository는 저장소 접근(지금은 메모리, 나중엔 DB)을 담당한다. 세 가지를 Controller 하나에 몰아넣으면 단일 책임 원칙(SRP) 위반이다 — "요청 형식이 바뀌어도", "비즈니스 규칙이 바뀌어도" 같은 클래스를 건드리게 된다. 실무적으로는 테스트 용이성도 중요한 이유: 비즈니스 로직이 Controller 안에 있으면 테스트할 때마다 가짜 HTTP 요청을 만들어야 하지만, Service로 분리하면 HTTP 없이 순수 로직만 테스트할 수 있다.

(오늘 실수: Repository의 책임을 "인터페이스 역할 수행"이라고 답했는데, 인터페이스인 것은 구현 방식이지 책임 자체가 아니다. 진짜 책임은 "저장소 접근을 담당"하는 것이다.)
</details>

## Q2. `ReservationRepository`는 인터페이스이고 실제 구현은 `InMemoryReservationRepository`가 한다. 그런데 `ReservationService` 코드 어디에도 `InMemoryReservationRepository`라는 이름이 안 나온다. 왜 이렇게 설계하는 게 좋은가?

<details>
<summary>정답/해설</summary>

의존성 역전 원칙(DIP) — Service가 "구체적인 구현"이 아니라 "추상화(인터페이스)"에 의존하기 때문에, 나중에 `InMemoryReservationRepository`를 JPA 기반 구현체로 바꿔도 Service 코드는 한 줄도 안 건드려도 된다(Week B에서 실제로 이 교체를 해볼 예정). 인터페이스와 메서드로 "무엇을 할 수 있는지(What)"만 정의하고 "어떻게(How)"는 구현체에 위임하면 유지보수가 간결해진다.
</details>

## Q3. `findById`에서 `r.getId() == id` 대신 `r.getId().equals(id)`로 고쳤다. `==`가 `Long` 같은 Wrapper 타입에서 왜 위험한가?

<details>
<summary>정답/해설</summary>

`Long`은 원시타입(`long`)이 아니라 객체(Wrapper)다. 객체끼리의 `==`는 "값이 같다"가 아니라 "같은 메모리 주소(참조)를 가리킨다"를 검사한다. `String`을 `==`로 비교하면 안 되는 것과 완전히 같은 이유. 다만 자바는 -128~127 범위의 `Long`을 내부적으로 캐싱해서 재사용하는 특이 케이스가 있어서, 작은 id 값에서는 `==`가 우연히 통과할 수도 있다 — 그래서 더 위험하다(작은 데이터로 테스트하면 버그가 숨어 있다가 값이 커지면 터짐).

(오늘 실수: "null값 탐지를 위해"라고 답했는데, `.equals()`를 쓰는 이유는 null 탐지가 아니라 참조 동일성과 값 동일성의 차이 때문이다. `복습큐.md`에 +2일로 등록해서 8/2에 다시 확인한다.)
</details>
