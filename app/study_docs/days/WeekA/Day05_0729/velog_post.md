# [백엔드 기본기 Day5] IoC와 DI — Service 생성자에 Repository를 넣어주는 건 누구인가

Day4에서 `cancel()`을 Service로 옮기면서 `ReservationService`는 `ReservationRepository` 인터페이스를 생성자로 받는 모양이 됐다. 그런데 Service 코드 어디에도 `new InMemoryReservationRepository()`가 없어서, 생성자 위에 `//원리는 5일 차에서 배우기..`라는 주석을 달아두고 넘어갔었다. 오늘은 그 주석을 지운 날이라, 객체를 누가 만들어 어디에 넣는지까지만 보고 scope 종류 전체나 동시성 재현은 범위 밖으로 뒀다.

> Service가 구현체를 직접 만들지 않는데도 도는 이유를 IoC와 DI로 나눠서 설명해봤다. `@Repository`를 떼고 돌려보니 컴파일은 통과하고 컨텍스트 조립이 `NoSuchBeanDefinitionException`으로 멈췄고, 같은 Service Bean을 두 번 꺼내 비교한 `assertSame`은 통과했다. 공유 인스턴스에서 생길 수 있는 경쟁 상태는 설명만 했고 실행으로 재현하지는 않았다.

## 1. 개념 설명

오늘 정리한 용어부터.

| 용어 | 한줄뜻 | 코드 모습 |
|---|---|---|
| IoC (제어의 역전) | 객체를 생성하고 연결하는 제어권이 애플리케이션 코드가 아니라 Spring 컨테이너에 있는 것 | Spring이 Repository→Service→Controller 순서로 Bean을 조립 |
| DI (의존성 주입) | 객체가 필요한 의존성을 직접 만들지 않고 외부에서 전달받는 것 | `ReservationService(ReservationRepository repository)` |
| Bean | Spring 컨테이너가 생성·보관·연결하는 객체 | `@Service`, `@Repository`, `@RestController` |
| 생성자 주입 | 필요한 객체를 생성자 매개변수로 받는 DI 방식 | `this.reservationRepository = reservationRepository;` |
| DIP (의존성 역전 원칙) | 상위 정책이 구체 구현이 아니라 추상화에 의존하는 원칙 | Service가 `InMemoryReservationRepository`가 아니라 `ReservationRepository`에 의존 |
| Singleton Bean | 기본적으로 ApplicationContext 하나에 인스턴스 하나만 두는 Bean | 같은 타입을 두 번 `getBean()`하면 같은 참조 |
| 무상태(stateless) Service | 요청마다 달라지는 값을 공유 필드에 저장하지 않는 Service | `requesterName`을 메서드 매개변수·지역변수로 사용 |

**표의 용어들은 조립 과정의 서로 다른 단계를 가리킨다.** 앞의 넷은 "객체가 어떻게 연결되는가", 뒤의 셋은 "그렇게 연결하고 나면 어떤 제약이 따라오는가"다.

연결 순서는 이렇게 흐른다.

```text
Component Scan 이 @Repository·@Service·@RestController 붙은 클래스를 Bean 후보로 수집
  → InMemoryReservationRepository Bean 생성
  → 그 참조를 ReservationService(ReservationRepository) 생성자에 전달
  → 만들어진 Service를 ReservationController(ReservationService) 생성자에 전달
```

이 그림에서 IoC와 DI는 서로 다른 층위를 가리킨다.

> - **IoC** — 조립 책임이 어디에 있는가. 화살표 전체를 누가 실행하느냐의 문제다.
> - **DI** — 그 책임을 가진 쪽이 부품을 어떤 통로로 건네는가. 여기서는 생성자 매개변수다.

생성자 주입은 그 통로를 타입으로 선언하는 방식이라, 이 클래스가 없으면 못 도는 의존성이 생성자 시그니처에 그대로 드러난다. 받은 값을 `final` 필드에 넣으면 초기화 이후 바뀌지 않는다는 것도 같이 보장된다.

**타입으로 선언한다는 게 DIP와 이어진다.** Service가 선언한 타입이 인터페이스이므로, 메모리 구현을 DB 구현이나 테스트용 가짜로 바꿔도 Service와 Controller의 코드는 그대로 둘 수 있다. 학습 중에는 이걸 "여러 계층간 코드 수정이 번거롭게 여러번 일어나지 않아도 된다"고 정리했다.

**Singleton은 조립이 끝난 다음의 이야기다.** 기본 scope에서는 컨테이너 하나가 Bean 하나만 만들어 계속 돌려주므로, 여러 요청 스레드가 같은 Service 객체를 함께 쓴다. 그래서 무상태가 취향이 아니라 조건이 된다 — 요청마다 달라지는 값을 공유 필드에 두면 스레드끼리 서로의 값을 덮어쓸 수 있다.

![클래스 다이어그램. «@RestController» ReservationController가 «@Service» ReservationService를, ReservationService가 «interface» ReservationRepository를 각각 생성자 주입으로 참조한다. «@Repository» InMemoryReservationRepository는 그 인터페이스를 «realize»하는데, 화살표가 구현체가 아니라 인터페이스로 향하는 것이 요점이다. Service는 구현체 이름을 모른다. 주석에는 ApplicationContext가 기동 시 Bean을 만들고 생성자 인자 타입에 맞는 Bean을 찾아 넣는다는 것, 기본 scope가 singleton이라 두 번 꺼내도 같은 인스턴스여서 ==가 true이고 그래서 Service가 무상태여야 한다는 것, @Repository를 떼면 넣어줄 Bean이 없어 기동에서 실패한다는 것이 적혀 있다.](../../assets/day05-ioc-di.png)

> **더 볼 것**
> - [Dependency Injection — Spring Framework Reference](https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html): 생성자 기반 DI와 생성자 인자 타입 매칭
> - [Bean Scopes — Spring Framework Reference](https://docs.spring.io/spring-framework/reference/core/beans/factory-scopes.html): singleton이 "JVM당 하나"가 아니라 "컨테이너당 하나"라는 근거
> - 아직 안 본 것 — singleton 외의 scope, OCP, 경쟁 상태를 코드로 재현하는 방법

## 2. 코드 구현

### Day4가 남긴 `//원리는 5일 차에서 배우기..` 지우기

현재 `ReservationService`의 앞부분이다.

```java
@Service
public class ReservationService{

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository){
        this.reservationRepository = reservationRepository;
    }
```

Day4까지는 이 생성자 위에 `//생성자 주입 : Spring이 자동으로 InMemoryReservationRepository를 찾아 넣어준다.`와 `//원리는 5일 차에서 배우기..` 두 줄이 붙어 있었고, 오늘 커밋에서 지웠다.

지우고 보니 남는 건 **Spring 문법이 하나도 없는 평범한 Java 생성자**다. Spring이 바꾸는 건 생성자의 모양이 아니라 그걸 누가 언제 호출하느냐다. 대신 Service가 이렇게 썼다면,

```java
private final InMemoryReservationRepository repository = new InMemoryReservationRepository();
```

Service가 "무엇이 필요한가"뿐 아니라 "그걸 어떻게 만드는가"까지 알게 된다. 생성자로 받는 쪽을 고른 건 그 두 가지를 분리하기 위해서였다.

### `@Repository`를 떼면 어디서 멈추는가

`InMemoryReservationRepository`에서 애노테이션만 제거하기 전에 "코드 자체는 문제없지만 애플리케이션 실행 단계에서 실패할 것"이라고 예측했다. 실패하는 단계는 맞췄는데, 이유는 "구현 클래스가 저장소 인터페이스 역할을 잃는다"고 설명했다.

실제로는 `compileJava`가 성공했다. `implements ReservationRepository`가 그대로 남아 있으니 Java 타입 관계에는 아무 문제가 없었다. 멈춘 곳은 `contextLoads()`였고, 원인 체인 마지막에 이게 있었다.

```text
NoSuchBeanDefinitionException
```

사라진 건 Java의 구현 관계가 아니라 Spring Bean 등록이었다. Repository Bean이 없으니 Service 생성자에 넣을 후보가 없고, 그 자리에서 컨텍스트 조립 전체가 중단된다. **컴파일러가 검사하는 타입 그래프와 Spring이 런타임에 조립하는 Bean 그래프는 서로 다른 검사 단계**라는 걸 이 실험으로 갈라냈다. 애노테이션을 되돌린 뒤 전체 테스트는 다시 통과했다.

### 생성자를 힌트 없이 다시 써봤을 때 나온 오류 세 개

완성 예제 읽기 → 대입문 한 줄 채우기 → Controller 생성자 전체 작성을 거친 뒤, `ReservationService` 생성자를 혼자 다시 썼다. 컴파일러가 세 번 잡아줬다.

```text
invalid method declaration; return type required   ← 생성자 이름을 ReservationRepository로 적음
cannot find symbol                                 ← 없는 this.reservationService 필드를 사용
incompatible types                                 ← ReservationService 타입 매개변수를 Repository 필드에 대입
```

세 오류가 각각 다른 조건을 가리켰다. 생성자 이름은 클래스 이름과 같아야 하고, `this.x`의 `x`는 그 클래스에 실제로 선언된 필드여야 하며, 매개변수 타입은 그 필드에 대입 가능해야 한다. 원인은 하나였다 — Controller 생성자의 모양을 옮겨오면서 클래스명·필드명·의존 타입을 Service 쪽으로 바꾸지 않은 것이다.

### 오늘 확인한 것

소스에 남은 변경은 Service·Controller 생성자 정리와 테스트 한 개 추가다.

```java
@Test
void reservationServiceBeanIsSingleton() {
    ReservationService first = applicationContext.getBean(ReservationService.class);
    ReservationService second = applicationContext.getBean(ReservationService.class);

    assertSame(first, second);
}
```

| 확인한 것 | 방법 | 결과 |
|---|---|---|
| Bean 등록이 빠지면 어디서 멈추는가 | 수동 확인 — `@Repository` 제거 후 `compileJava`·`test`, 확인 뒤 복구했으므로 커밋에 남지 않음 | 컴파일 성공, `contextLoads()` 실패, 원인 `NoSuchBeanDefinitionException` |
| 같은 Service Bean 두 번 조회 | 자동 테스트 — `reservationServiceBeanIsSingleton()` | `assertSame(first, second)` 통과 |
| 최종 객체 그래프 | 자동 테스트 — `./gradlew test` 전체 | `BUILD SUCCESSFUL` |

**미검증** — 공유 Service 필드가 만드는 경쟁 상태, `InMemoryReservationRepository`의 `ArrayList` 동시성. 둘 다 실행으로 재현하지 않았다.

오늘 코드는 [`4a0219a` 커밋](https://github.com/enderpawar/8week_Spring_Study/commit/4a0219a)에 있다.

## 3. 스스로 답한 질문

### Q. IoC와 DI는 지금 코드에서 각각 뭘 가리키나?

처음 답은 **"잘 모르겠다"**였다. 두 용어를 같은 것의 다른 이름처럼 묶어서 외워둔 상태라, 하나를 설명하려 하면 다른 하나의 설명이 나왔다.

빈칸을 채우며 다시 세운 답은 이렇다. IoC는 제어권이 어디로 옮겨갔는가의 문제로, 지금은 Controller·Service·Repository 세 객체의 생성과 연결을 Spring 컨테이너가 맡고 있다. DI는 그 제어권을 가진 쪽이 부품을 건네는 구체적인 방식이고, 여기서는 "Repository 구현 Bean을 Service 생성자 매개변수로 넘긴다"가 그것이다.

다만 힌트를 받은 직후에 맞춘 거라 진짜 인출인지 확신이 없어서, 8/2 재시험 항목으로 걸어뒀다.

### Q. 같은 Bean을 두 번 꺼내 `==`로 비교했는데 왜 `true`인가?

실행 전 예측은 `false`였다. 며칠 전 서로 다른 `Long` Wrapper 객체를 `==`로 비교했다가 틀렸던 경험을 그대로 가져다 붙였다. 실제로는 `assertSame(first, second)`가 통과했다.

틀린 건 `==`의 의미가 아니라 비교 대상에 대한 가정이었다. `==`는 여전히 참조 비교이고, 다만 두 번의 `getBean()`이 정말 같은 인스턴스를 돌려줬다. 서로 다른 `Long`은 애초에 다른 객체였고 Singleton Bean은 같은 객체였으니, 두 결과는 서로 모순되지 않는다.

여기서 하나 더 좁혀 잡았다. Singleton은 JVM 전체에 하나라는 뜻이 아니라 **ApplicationContext 하나에 하나**다. 앞으로 `==` 결과를 예측할 땐 연산자만 보지 말고 "두 변수가 같은 객체를 가리킬 경로가 있는가"를 먼저 확인하기로 했다.

### Q. Singleton Service에 요청별 이름을 필드로 두면 뭐가 문제인가?

처음 답은 **"요청에 혼선이 생길 수 있다"**였다. 방향은 맞는데 무엇이 공유되는지가 빠져 있었다.

공유되는 건 Service 인스턴스이고, 따라서 그 인스턴스의 필드도 요청 스레드들이 함께 쓴다. `currentRequesterName` 같은 필드에 진우 요청이 값을 쓴 뒤 민수 요청이 덮어쓰면, 진우 요청이 이어서 그 필드를 읽을 때 민수의 값을 읽을 수 있다. 그래서 요청마다 달라지는 값은 메서드 매개변수·지역변수에 두고, 필드에는 주입받은 의존성처럼 호출과 무관한 값만 둔다.

이 순서는 설명으로 짚은 것이고 코드로 재현하지는 않았다. 그래서 "몇 번 중 몇 번" 같은 수치는 여기 쓰지 않는다.

## 4. 정리하며

시작할 때는 생성자 주입을 "Spring이 제공하는 특별한 생성자"쯤으로 생각했다. 확인한 건 반대였다 — 생성자는 그대로 평범한 Java 생성자이고, 달라진 건 그걸 호출하는 주체다. `@Repository` 제거 실험이 그 경계를 눈으로 보여줬다. 컴파일러는 `implements` 관계까지만 보고, 그 뒤에 Bean 그래프를 조립하는 일은 완전히 다른 단계에서 벌어진다.

`==` 예측이 틀린 것도 같은 종류의 교정이었다. 연산자의 의미는 처음부터 맞게 알고 있었고, 몰랐던 쪽은 컨테이너가 같은 인스턴스를 돌려준다는 조립 규칙이었다. Singleton을 "하나만 만든다"로 외우는 것과 "그래서 필드를 공유한다"까지 잇는 건 다른 일이라는 것도 여기서 붙었다.

남은 것도 있다. 공유 인스턴스의 경쟁 상태와 `InMemoryReservationRepository`의 `ArrayList` 동시성은 오늘 설명만 하고 재현하지 않았는데, 동시성은 이 5주 트랙에서 다루지 않기로 한 **고치지 않을 것**라 미검증 표시만 남긴다. 그리고 오늘 늘어난 자동 테스트는 Bean 동일성 하나뿐이라 HTTP 응답 계약은 여전히 회귀를 잡지 못한다 — 이건 Week D D5에 갚기로 한 **나중에 고칠 것**다.

다음은 Week A D6 누적시험이다. 그 전에 오늘 힌트가 필요했던 IoC·DI 구분, Singleton 동일성, 생성자 구조를 8/2에 다시 인출한다.

오늘 공부한 소스코드: [8week_Spring_Study/app](https://github.com/enderpawar/8week_Spring_Study/tree/master/app)
