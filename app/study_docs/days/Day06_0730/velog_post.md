# [백엔드 기본기 Day 6] 같은 `==`인데 왜 Singleton Bean에는 맞고 `Long`에는 위험할까

Week A 누적시험에서 가장 헷갈린 지점은 `==` 자체가 틀린 연산자가 아니라는 사실이었다. `ReservationService` Bean 두 개를 비교할 때는 `==`가 의도에 맞지만, 두 `Long` 식별자의 값을 비교할 때는 같은 연산자가 버그를 숨길 수 있다.

> `==`와 `.equals()`의 선택은 타입 이름만 보고 정하는 규칙이 아니다. 지금 확인하려는 것이 **같은 객체인지**와 **같은 값인지**를 먼저 구분해야 한다. 이번 누적시험에서는 Singleton scope, PK, 생성자 주입까지 이 구분과 연결해 교정했다.

## 1. 개념 설명

| 용어 | 한줄뜻 | 코드 모습 |
|---|---|---|
| 참조 동일성 | 두 변수가 같은 객체를 가리키는지 비교 | `first == second`, `assertSame()` |
| 값 동일성 | 객체가 표현하는 논리적 값이 같은지 비교 | `storedId.equals(requestedId)` |
| Singleton scope | Bean 정의 하나에 같은 인스턴스를 재사용하는 Spring 기본 scope | `ReservationService` Bean 두 번 조회 |
| PK | 데이터 하나를 고유하게 식별하는 값 | `Reservation.id` |

자바에서 참조 타입에 `==`를 사용하면 객체 안의 값을 펼쳐 비교하지 않는다. 두 변수가 같은 참조를 들고 있는지를 확인한다. 그래서 서로 다른 `Long` 객체가 모두 `1000L`을 나타내더라도 `==` 결과를 값 비교로 믿을 수 없다.

작은 정수에서는 더 헷갈린다. Java boxing 규칙 때문에 `-128`부터 `127`까지의 일부 정수 값은 같은 객체가 재사용될 수 있다. 작은 ID로만 실험하면 `==`가 값 비교처럼 동작하는 것처럼 보일 수 있지만, 그것은 연산자의 의미가 바뀐 것이 아니다.

Spring Singleton Bean은 반대다. 같은 ApplicationContext에서 같은 Bean 정의를 두 번 조회하면 기본 Singleton scope가 같은 인스턴스를 반환한다. 여기서 확인하려는 질문이 “두 조회 결과가 실제로 같은 Bean인가?”이므로 참조 동일성을 보는 `==`나 JUnit의 `assertSame()`이 정확하다.

> Singleton은 JVM 전체에 객체가 하나라는 뜻이 아니다. 이 글의 범위에서는 **같은 Spring 컨테이너 안의 같은 Bean 정의**가 같은 인스턴스를 돌려준다는 뜻이다.

PK도 값 동일성과 연결된다. 방 이름과 예약자 이름은 중복될 수 있으므로 기존 예약 하나를 안전하게 갱신할 기준이 되지 못한다. `id`가 같은지를 값으로 비교해야 어느 예약을 취소할지 정할 수 있다.

CS에서 객체의 identity와 equality를 구분하는 문제, DB에서 기본키로 행을 식별하는 문제는 서로 다른 단원처럼 보여도 같은 질문을 공유한다. “이 둘을 같은 대상으로 보려면 어떤 기준이 필요한가?”라는 질문이다.

> **더 볼 것**
> - [JLS 5.1.7 Boxing Conversion](https://docs.oracle.com/javase/specs/jls/se17/html/jls-5.html#jls-5.1.7): 작은 정수 boxing 결과의 참조 재사용 규칙
> - [Spring Bean Scopes](https://docs.spring.io/spring-framework/reference/core/beans/factory-scopes.html): Singleton scope의 범위와 의미
> - 아직 안 본 것 — DB 기본키 생성과 JPA Entity 식별은 Week B에서 연결한다.

## 2. 코드 구현

### 같은 Bean인지 확인할 때는 `assertSame`

Day05에 추가한 테스트는 같은 ApplicationContext에서 `ReservationService`를 두 번 가져온다.

```java
ReservationService first = applicationContext.getBean(ReservationService.class);
ReservationService second = applicationContext.getBean(ReservationService.class);

assertSame(first, second);
```

`assertSame`은 `.equals()`를 호출하지 않고 같은 참조인지 검사한다. 테스트가 통과했다는 것은 두 Service의 필드 값이 우연히 같다는 뜻이 아니라, 컨테이너가 같은 인스턴스를 반환했다는 뜻이다.

이 사실 때문에 Singleton Service에는 요청별 변경 상태를 필드로 두지 않는다. 여러 요청이 같은 Service 인스턴스를 공유하므로 `currentRequesterName` 같은 값을 필드에 넣으면 다른 요청이 덮어쓸 수 있다. 현재 `reserve()`는 요청 값을 매개변수와 지역변수로만 사용한다.

### 식별자의 값이 같은지는 `.equals()`

메모리 저장소가 예약을 찾을 때는 질문이 달라진다.

```java
for (Reservation reservation : store) {
    if (reservation.getId().equals(id)) {
        return Optional.of(reservation);
    }
}
```

여기서는 두 `Long` 변수가 같은 객체인지가 중요하지 않다. 저장된 예약 번호와 URL로 받은 예약 번호가 같은 숫자를 나타내는지가 중요하다. 그래서 `.equals()`가 저장소 계약에 맞다.

현재 코드는 저장된 예약의 ID가 `null`이 아닌 상태에서만 목록에 들어간다는 전제를 가진다. 신규 예약은 `save()`에서 먼저 ID를 부여한 뒤 `store`에 추가한다.

### 생성자 주입도 “같은 역할”이 아니라 “맞는 타입”을 연결한다

누적시험에서 생성자 주입 문법도 다시 교정했다.

```java
private final ReservationRepository reservationRepository;

public ReservationService(ReservationRepository reservationRepository) {
    this.reservationRepository = reservationRepository;
}
```

생성자 이름은 클래스 이름과 같아야 하고, `this.reservationRepository`는 실제 필드여야 하며, 매개변수는 그 필드에 대입 가능한 타입이어야 한다. “저장소처럼 보이는 객체”가 아니라 Java 타입 시스템이 허용하는 객체가 연결된다.

### 오늘 확인한 것

- 자동 테스트: 전체 10개 테스트가 통과했다.
- `reservationServiceBeanIsSingleton()`이 같은 ApplicationContext의 Service 두 조회가 같은 참조임을 검증했다.
- `ReservationServiceTest`가 ID 기반 조회와 취소 흐름을 검증했다.
- 누적 인출: IoC/DI, `Long` 비교, 인터페이스 의존, PK, 경로 변수, 생성자 주입을 재확인했다.
- 별도 성능 측정이나 동시성 race 재현은 하지 않았다.
- 현재 작업은 커밋 전 검증 상태이므로 게시 글의 commit permalink는 커밋 후 추가한다.

## 3. 스스로 답한 질문

### Q. Singleton Bean을 `==`로 비교한 최초 답은 왜 틀렸나?

처음 기록은 “객체 상으로는 동일해 보이지만 값이 다르기 때문”이었다. 여기에는 값 비교와 참조 비교가 섞여 있었다. `==`는 값이 다른지를 판단하지 않고, 두 변수가 같은 객체를 가리키는지를 본다.

교정된 답은 “기본 Singleton scope 때문에 같은 컨테이너가 동일한 Bean 인스턴스를 반환하므로 `true`”다. 앞으로는 연산자를 고르기 전에 비교 목적을 “참조인가, 값인가”로 먼저 말한다.

### Q. `id`가 없어도 수정할 수 있지 않나?

기존 기록에는 `id` 없이도 수정할 수 있다는 설명이 있었다. 이름이나 방 번호로 조건 검색을 하는 것 자체는 가능하다. 하지만 값이 중복되면 어느 하나를 수정할지 결정할 수 없으므로 안전한 단건 갱신 계약이 되지 못한다.

교정 뒤에는 PK를 “조회 기능을 가능하게 하는 값”이 아니라 “대상 하나를 고유하게 식별하는 값”으로 설명했다. Day07에서는 이 기준을 실제 저장소의 추가와 교체 분기에 사용했다.

### Q. `this.x`의 `x`는 메서드인가?

기존 세션 기록에서는 `this.x`의 대상을 메서드라고 답했다가 교정했다. `this`는 현재 객체이고 `this.x`는 그 객체의 필드를 가리킨다. 생성자 매개변수의 값을 현재 객체 필드에 대입하는 코드다.

재발 방지 기준은 세 가지다. 생성자명과 클래스명, `this` 뒤의 실제 필드명, 필드와 매개변수의 대입 가능한 타입을 순서대로 확인한다.

## 4. 정리하며

이번 누적시험 전에는 `==`를 “객체에서 쓰면 안 되는 것”처럼 외우기 쉬웠다. 지금은 같은 연산자가 Singleton Bean 확인에는 정확하고 `Long` 값 비교에는 부정확한 이유를 비교 목적과 객체 조건으로 나눠 설명할 수 있다.

이 구분은 PK와 생성자 주입에도 이어졌다. 객체나 데이터가 비슷해 보인다는 사실이 동일한 대상을 보장하지 않는다. 고유 식별자와 타입 계약처럼, 시스템이 판정할 수 있는 기준이 필요하다.

남은 한계는 Singleton Service의 공유 필드 경쟁 상태를 실제 여러 스레드로 재현하지 않았다는 점이다. 이번 5주 트랙에서는 고치지 않을 것으로 분류했고, 요청별 값은 필드가 아닌 지역변수에 둔다는 규칙만 현재 코드에 적용한다.

<!-- 선택 복습 메모: 게시 화면에는 노출하지 않는다.
### 선택 추가 설명

[직접 작성] Singleton Bean 비교에서는 `==`가 의미 있고, `Long` 식별자 값 비교에서는 `.equals()`가 필요한 이유를 한 문단으로 설명한다.
-->
