# [백엔드 기본기 Day 5] 생성자 주입 — Spring은 객체를 어떻게 찾아 연결하는가

`ReservationController → ReservationService → ReservationRepository`로 책임을 나눈 뒤에도 한 가지 질문이 남았다. Service 코드에는 `new InMemoryReservationRepository()`가 없는데 실제 구현 객체는 어디서 생겨 들어오는가? 이번 학습에서는 현재 생성자 주입 코드를 직접 다시 작성하고, Bean 등록을 제거한 실패 실험과 Singleton 동일성 테스트로 Spring의 조립 과정을 확인했다.

## 한눈에 보기

- **문제:** Service가 Repository 구현체를 직접 만들지 않는데도 동작하는 이유와 IoC·DI의 차이를 설명하지 못했다.
- **적용:** Service는 `ReservationRepository` 인터페이스를 생성자로 받고, Spring이 `@Repository` Bean을 찾아 주입하도록 유지했다.
- **검증:** `@Repository` 제거 시 컴파일은 통과하고 컨텍스트 테스트가 `NoSuchBeanDefinitionException`으로 실패하는 것을 확인했다. 같은 Service Bean 두 번 조회는 `assertSame`을 통과했고, 최종 전체 테스트도 성공했다.
- **한계:** Singleton Service의 변경 상태가 만드는 race는 실행 수치로 재현하지 않았다. 현재 메모리 저장소의 동시성 안전성도 검증 범위 밖이다.

## 1. 평범한 Java 생성자와 Spring의 객체 조립

현재 `ReservationService`의 핵심 코드는 다음과 같다.

```java
@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }
}
```

이 생성자는 Spring 전용 문법이 아니라 평범한 Java 생성자다. Spring이 하는 일은 생성자 문법을 바꾸는 것이 아니라, 애플리케이션 컨텍스트를 만들 때 필요한 객체를 대신 생성하고 연결하는 것이다.

먼저 Component Scan이 `@Repository`가 붙은 `InMemoryReservationRepository`를 Bean 후보로 찾는다. Spring은 이 객체를 만든 뒤 `ReservationService` 생성자의 `ReservationRepository` 타입과 맞는 Bean으로 선택한다. 만들어진 Service는 다시 `ReservationController` 생성자에 전달된다.

객체의 생성·연결 제어권이 Spring으로 넘어간 것이 IoC(Inversion of Control)다. Spring이 생성자 매개변수로 실제 Repository 객체를 전달하는 방식은 DI(Dependency Injection)다. "누가 조립하는가"와 "필요한 부품을 어떻게 전달하는가"를 구분해야 두 용어가 섞이지 않는다.

Service가 다음처럼 구현체를 직접 생성할 수도 있다.

```java
private final InMemoryReservationRepository repository =
        new InMemoryReservationRepository();
```

하지만 이 경우 Service는 메모리 구현의 생성 방법까지 알게 된다. 생성자에서 `ReservationRepository` 인터페이스를 받으면, 구현체 교체가 Controller와 Service 수정으로 연쇄 전파되는 것을 줄일 수 있다. 학습 중에는 이를 "여러 계층간 코드 수정이 번거롭게 여러번 일어나지 않아도 된다"고 정리했다. 이는 상위 정책인 Service가 구체 구현보다 추상화에 의존한다는 DIP와 연결된다.

## 2. 예측 → 실행: `@Repository`를 제거하면 어디서 실패할까

`InMemoryReservationRepository`에서 `@Repository`만 제거하기 전에 "코드 자체는 문제없지만 애플리케이션 실행 단계에서 실패할 것"이라고 예측했다. 실패 단계는 맞았지만, 처음에는 구현 클래스가 저장소 인터페이스 역할을 잃는다고 설명했다.

실제 실행에서는 `compileJava`가 성공했다. `implements ReservationRepository`가 남아 있어 Java 타입 관계와 문법에는 문제가 없었기 때문이다. 이후 `contextLoads()`에서 실패했고 원인 체인의 마지막에는 다음 예외가 있었다.

```text
NoSuchBeanDefinitionException
```

사라진 것은 Java의 인터페이스 구현 관계가 아니라 Spring Bean 등록이었다. Repository Bean이 없으니 Spring이 `ReservationService` 생성자에 넣을 객체를 찾지 못했고, 전체 컨텍스트 조립이 중단됐다. 애노테이션을 복구한 뒤 전체 테스트가 다시 통과했다.

이 실험으로 컴파일러가 확인하는 타입 그래프와 Spring이 런타임에 조립하는 Bean 그래프가 다른 검사 단계라는 점을 구분했다.

## 3. Singleton의 `==`는 왜 `true`였나

같은 ApplicationContext에서 `ReservationService` Bean을 두 번 조회하고 참조를 비교했다.

```java
ReservationService first = context.getBean(ReservationService.class);
ReservationService second = context.getBean(ReservationService.class);

assertSame(first, second);
```

실행 전에는 `==`가 참조 비교이므로 `false`일 것이라고 예측했다. 앞서 서로 다른 `Long` Wrapper 객체를 `==`로 비교했던 경험을 그대로 적용한 것이다. 하지만 `reservationServiceBeanIsSingleton()`은 통과했다.

참조 비교라는 원리는 바뀌지 않았다. 두 `Long` 변수가 서로 다른 객체를 가리켰던 것과 달리, Spring의 기본 Singleton scope는 같은 컨텍스트에서 동일한 Service 인스턴스를 반환한다. 비교 대상이 정말 같은 객체였기 때문에 `true`였다. 여기서 Singleton은 JVM 전체에서 무조건 하나라는 뜻이 아니라 ApplicationContext를 기준으로 이해해야 한다.

같은 Service 객체를 여러 요청이 공유한다면 요청별 변경 상태를 필드에 두면 위험하다. 진우 요청이 `currentRequesterName`에 값을 쓴 뒤 민수 요청이 덮어쓰면, 진우 요청이 민수의 값을 읽는 경쟁 상태가 생길 수 있다. 요청마다 달라지는 값은 메서드 매개변수와 지역변수에 두고, Service 필드에는 주입받은 의존성처럼 공유 가능한 값만 두는 편이 안전하다.

## 4. 독립 작성에서 컴파일러가 보여준 생성자 조건

완성 예제와 한 줄 완성, Controller 생성자 전체 작성을 거친 뒤 `ReservationService` 생성자를 독립적으로 다시 작성했다. 첫 코드는 생성자 이름을 `ReservationRepository`로 적어 다음 오류가 발생했다.

```text
invalid method declaration; return type required
```

생성자 이름이 현재 클래스 이름과 다르면 컴파일러는 반환 타입이 없는 일반 메서드처럼 해석한다. 이름을 고친 다음에는 존재하지 않는 `this.reservationService` 필드를 사용해 `cannot find symbol`이 발생했다. 마지막으로 `ReservationService` 타입 매개변수를 `ReservationRepository` 필드에 넣어 `incompatible types`가 발생했다.

컴파일러 메시지를 따라 클래스 이름, 실제 필드 이름, 필요한 의존 타입을 하나씩 맞춘 뒤 생성자는 다음 구조가 됐다.

```java
public ReservationService(ReservationRepository reservationRepository) {
    this.reservationRepository = reservationRepository;
}
```

이후 전체 테스트가 `BUILD SUCCESSFUL`로 끝났다. 생성자 주입은 애노테이션 암기보다 "이 클래스가 실제로 필요로 하는 객체가 무엇인가"를 타입으로 정확히 선언하는 작업이었다.

## 5. 검증 근거와 현재 한계

| 검증 대상 | 검증 방법 | 확인한 결과 |
|---|---|---|
| Bean 등록 누락 | `@Repository` 제거 후 컨텍스트 테스트 | 컴파일 성공, `contextLoads()` 실패, `NoSuchBeanDefinitionException` |
| Singleton 동일성 | `reservationServiceBeanIsSingleton()` | `assertSame(first, second)` 통과 |
| 최종 객체 그래프 | 생성자 복구 후 전체 테스트 | `BUILD SUCCESSFUL` |

Singleton Service의 변경 필드가 만드는 경쟁 상태는 실행으로 재현하지 않았다. 이번에는 공유 인스턴스의 가능한 실행 순서만 설명했으므로, 측정하지 않은 횟수나 결과값은 남기지 않는다. 또한 `InMemoryReservationRepository`는 실제 저장 상태를 `ArrayList`에 보관하지만 동시성 안전성은 검증하지 않았다.

> 면접에서 다시 답해볼 질문
>
> - IoC와 DI는 현재 Repository→Service 연결에서 각각 무엇을 뜻하는가?
> - Singleton Service에 요청별 상태를 필드로 두면 왜 위험한가?

## 정리하며

처음에는 생성자 주입을 Spring의 별도 생성자 방식처럼 생각했지만, 실제로는 평범한 Java 생성자를 Spring이 호출해 객체 그래프를 조립하는 구조였다. `@Repository` 제거 실험은 Java 타입 관계와 Bean 등록을 분리해서 보게 했고, Singleton 테스트는 `==`의 결과가 연산자 이름이 아니라 실제 참조 관계에 의해 결정된다는 점을 확인시켰다.

다음 시작점은 Week A D6 누적시험이다. 그 전에 힌트가 필요했던 IoC·DI 구분, Singleton 동일성, 생성자 구조를 8/2에 다시 인출한다.
