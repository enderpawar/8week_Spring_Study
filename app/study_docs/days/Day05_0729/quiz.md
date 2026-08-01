# Day05 (7/29 계획→8/1 완료, Week A D5) 문제집 — IoC·DI·생성자 주입

형식: 문제 → <details> 정답/해설 (접힘)

## Q1. 현재 코드에서 IoC와 DI는 각각 무엇을 뜻하는가?

<details>
<summary>정답/해설</summary>

IoC는 `ReservationController`, `ReservationService`, `InMemoryReservationRepository`의 생성·연결 제어권을 Spring 컨테이너가 갖는 것이다. DI는 Spring이 `ReservationService` 생성자에 `ReservationRepository` 구현 Bean을 전달하는 구체적인 연결 방식이다. "누가 조립하는가"는 IoC, "필요한 부품을 어떻게 전달하는가"는 DI로 구분할 수 있다.

세션에서는 처음에 "잘 모르겠다"고 답한 뒤, 빈칸 교정에서 `IoC`, `DI`를 올바르게 구분했다. 8/2에 힌트 없이 다시 인출한다.
</details>

## Q2. Service가 `new InMemoryReservationRepository()`를 직접 호출하지 않고 생성자에서 `ReservationRepository`를 받는 이유는?

<details>
<summary>정답/해설</summary>

Service가 구체 구현체가 아니라 인터페이스 계약에 의존하면, 메모리·DB·테스트용 가짜 Repository 사이의 교체가 Service와 Controller 수정으로 연쇄 전파되는 것을 줄일 수 있다. 필요한 의존성이 생성자에 드러나며 `final` 필드로 보관할 수도 있다. Spring이 없어도 이 생성자 자체는 평범한 Java 생성자이고, Spring은 객체 그래프 조립을 자동화한다.

학습자 표현: "여러 계층간 코드 수정이 번거롭게 여러번 일어나지 않아도 된다."
</details>

## Q3. `InMemoryReservationRepository`에서 `@Repository`를 제거하면 왜 컴파일은 되고 컨텍스트 시작은 실패하는가?

<details>
<summary>정답/해설</summary>

`implements ReservationRepository`는 그대로이므로 Java 문법과 타입 관계는 정상이다. 하지만 Spring이 구현 객체를 Bean으로 등록하지 않아 `ReservationService` 생성자에 넣을 후보가 사라진다. 실제 실험에서는 `compileJava`까지 성공한 뒤 `contextLoads()`가 `NoSuchBeanDefinitionException`을 원인으로 실패했다.
</details>

## Q4. 같은 `ReservationService` Bean을 두 번 조회해 `==`로 비교하면 왜 `true`인가?

<details>
<summary>정답/해설</summary>

`==`는 참조 비교가 맞다. Spring의 기본 Singleton scope에서는 같은 ApplicationContext가 동일한 Bean 인스턴스를 반환하므로 두 참조가 같다. `Long` 두 개가 서로 다른 Wrapper 인스턴스일 때 `==`가 `false`인 것과 모순되지 않는다. `reservationServiceBeanIsSingleton()`의 `assertSame(first, second)`가 통과했다.
</details>

## Q5. Singleton Service에 요청별 `currentRequesterName`을 필드로 두면 왜 위험한가?

<details>
<summary>정답/해설</summary>

여러 요청 스레드가 같은 Service 인스턴스와 필드를 공유한다. 한 요청이 기록한 값을 다른 요청이 덮어쓰면 첫 요청이 잘못된 이름을 읽는 경쟁 상태가 생길 수 있다. 요청별 값은 메서드 매개변수나 지역변수로 두어 호출마다 분리한다.
</details>

## Q6. 생성자 주입을 독립 작성할 때 확인할 세 가지 문법 조건은?

<details>
<summary>정답/해설</summary>

생성자 이름은 클래스 이름과 같아야 한다. `this.x`의 `x`는 현재 클래스에 선언된 필드여야 한다. 생성자 매개변수 타입은 그 필드에 대입 가능한 타입이어야 한다. 이번 세션에서는 이 세 오류를 컴파일러 메시지 순서대로 수정한 뒤 전체 테스트가 통과했다.
</details>
