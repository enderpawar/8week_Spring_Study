# Day06 (7/30 계획→8/2 완료, Week A D6) 문제집 — 누적시험 A

형식: 문제 → 기록된 결과 → 정답/해설

Day01~03 범위는 `복습큐.md`에서 앞선 +2 인출 통과가 확인되어 예정된 다음 간격으로 유지했다. 이번 누적시험은 Day04~05의 도래 문항과 오답 재시험을 중심으로 진행했다.

## Q1. IoC와 DI는 어떻게 다른가?

- 기록된 결과: 통과

<details>
<summary>정답/해설</summary>

IoC는 Controller·Service·Repository의 생성과 연결을 누가 제어하는지에 관한 원칙이다. 현재 코드에서는 Spring 컨테이너가 객체 그래프를 조립한다. DI는 그 과정에서 필요한 의존 객체를 생성자 같은 통로로 전달하는 구체적인 방식이다.
</details>

## Q2. `Long` 두 개의 값을 비교할 때 왜 `==` 대신 `.equals()`를 쓰는가?

- 기록된 결과: 통과

<details>
<summary>정답/해설</summary>

참조 타입의 `==`는 두 변수가 같은 객체를 가리키는지 비교한다. `.equals()`는 해당 타입이 정의한 논리적 값 동일성을 비교한다. 작은 정수의 boxing 결과는 캐시 때문에 `==`도 우연히 `true`가 될 수 있으므로 식별자 값 비교에 사용하면 안 된다.
</details>

## Q3. 같은 ApplicationContext에서 `ReservationService` Bean을 두 번 조회해 `==`로 비교하면 왜 `true`인가?

- 기록된 결과: 오답 교정 후 통과

<details>
<summary>정답/해설</summary>

Spring Bean의 기본 scope가 Singleton이므로 같은 컨테이너가 같은 Bean 인스턴스의 참조를 반환한다. 값이 같아서가 아니라 실제로 같은 객체를 가리키므로 `assertSame(first, second)`가 통과한다.
</details>

## Q4. Service가 구현체가 아니라 Repository 인터페이스에 의존하는 이유는?

- 기록된 결과: 통과

<details>
<summary>정답/해설</summary>

Service가 `ReservationRepository` 계약에 의존하면 메모리 저장소를 JPA 저장소로 교체할 때 상위 계층까지 수정이 연쇄 전파되는 것을 줄일 수 있다. 생성자에는 필요한 계약이 드러나고, Spring은 그 계약을 구현한 Bean을 연결한다.
</details>

## Q5. 기존 예약을 안전하게 수정하는 데 PK가 필요한 이유는?

- 기록된 결과: 오답 교정 후 통과

<details>
<summary>정답/해설</summary>

방 이름이나 예약자 이름은 중복될 수 있다. 조건 검색은 가능해도 기존 대상 하나를 고유하게 특정해 갱신하려면 식별자가 필요하다. 메모리 저장소의 `id`는 이후 DB 기본키와 같은 식별 역할을 연습한다.
</details>

## Q6. `@PathVariable` 이름과 URL 템플릿이 어긋난 문제는 언제 발견되는가?

- 기록된 결과: 통과

<details>
<summary>정답/해설</summary>

문자열 안의 URL 템플릿과 어노테이션 연결은 자바 컴파일러가 검사하지 않는다. 애플리케이션이 요청을 매핑하는 런타임에 드러난다. Day04에서는 컴파일과 기존 테스트는 통과했지만 실제 HTTP 요청에서 실패했다.
</details>

## Q7. 생성자 주입 코드를 독립 작성할 때 확인할 세 가지는?

- 기록된 결과: 오답 교정 후 통과

<details>
<summary>정답/해설</summary>

생성자 이름은 클래스 이름과 같아야 한다. `this.x`의 `x`는 현재 클래스에 선언된 필드여야 한다. 생성자 매개변수 타입은 그 필드에 대입 가능한 타입이어야 한다.
</details>

## 완료 판정

- 도래한 Day04~05 문항 7개를 모두 통과했다.
- Singleton Bean, PK, 생성자 주입 오답은 교정 후 통과했다.
- Day01~03 문항은 앞선 인출 결과에 따라 8/4 또는 8/8~9의 다음 간격을 유지한다.
- 교정한 핵심은 `explain-log.md`, 다음 +2 일정은 `복습큐.md`에 반영했다.
