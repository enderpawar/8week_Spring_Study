# [백엔드 기본기 Day6] 1주차 누적시험 — Reference Equality와 Identifier 기준

Week A D6는 새 개념을 배우는 날이 아니라 Day01~05를 노트 없이 다시 꺼내 보는 누적시험이었다. 계획은 7월 30일이었지만 실제로는 8월 2일에 D7과 함께 진행했다. 이 글은 시험에서 다시 설명한 개념, 즉 `==`와 `.equals()`, Singleton Bean, PK, 생성자 주입을 교과서처럼 한 번 더 정리하고 실제 오답 세 개를 교정 순서대로 남긴다.

> `==`가 틀린 연산자가 아니라, 비교 목적이 **같은 객체인가**인지 **같은 값인가**인지에 따라 맞는 연산자가 달라진다는 점을 다시 잡았다. 같은 ApplicationContext에서 두 번 조회한 `ReservationService`는 `assertSame`이 통과하는 같은 참조였고, 예약 번호 `Long`은 `.equals()`로 값을 비교한다. 도래한 Day04~05 문항 7개는 모두 통과했으며, 그중 Singleton Bean·PK·생성자 주입 세 문항은 오답을 교정한 뒤 통과했다.

## 1. 개념 설명

| 용어 | 한줄뜻 | 현재 프로젝트 적용 지점 |
|---|---|---|
| 참조 동일성 | 두 변수가 같은 객체를 가리키는지 비교하는 것 | `reservationServiceBeanIsSingleton()`의 `assertSame(first, second)` |
| 값 동일성 | 서로 다른 객체라도 논리적으로 같은 값을 나타내는지 비교하는 것 | `InMemoryReservationRepository.findById()`의 `r.getId().equals(id)` |
| Singleton scope | Bean 정의 하나당 컨테이너가 같은 인스턴스를 재사용하는 기본 scope | `@Service ReservationService` |
| PK(기본키) | 데이터 하나를 고유하게 식별하는 값 | `Reservation.id`, `assignId(nextId++)` |
| 요청 매핑 | URI·본문 같은 HTTP 입력을 Controller 인수와 연결하는 처리 | `@PostMapping("/reservations/cancel/{id}")`, `@PathVariable Long id` |
| 생성자 주입 | 필수 의존성을 생성자 매개변수로 전달받는 방식 | `ReservationService(ReservationRepository reservationRepository)` |
| DIP | 상위 정책이 구체 구현보다 추상 계약에 의존하는 원칙 | `private final ReservationRepository reservationRepository` |

이 용어들은 서로 다른 Day에서 나왔지만, 시험에서는 한 질문으로 모였다. **두 대상을 같은 것으로 판정하는 기준이 무엇인가**라는 질문이다.

`==`와 `.equals()`의 차이는 결국 Stack의 변수가 Heap의 어느 객체를 가리키는지의 문제이므로, Singleton Bean과 `Long`에도 그대로 적용되는 이 구조를 가장 흔한 String 예시로 먼저 한 장으로 보면 다음과 같다(`a == c`는 true, `b == d`는 false, `.equals()`는 모두 true).

![Stack 영역의 변수 a, b, c, d가 Heap 영역의 객체를 가리키는 그림. a와 c는 Heap 안 String Pool의 "apple" 객체 하나를 함께 가리키고, b와 d는 new String("apple")로 만든 서로 다른 두 객체를 각각 가리킨다. 따라서 a == c는 같은 참조라 true, b == d는 다른 참조라 false이며, 값은 모두 "apple"이다.](https://raw.githubusercontent.com/enderpawar/8week_Spring_Study/master/app/study_docs/assets/day06-overview-reference-equality.png)

*출처: [[자바] 문자열 비교하기 ==와 equals의 차이](https://velog.io/@beneficial/%EC%9E%90%EB%B0%94-%EB%AC%B8%EC%9E%90%EC%97%B4-%EB%B9%84%EA%B5%90%ED%95%98%EA%B8%B0-%EC%99%80-equals%EC%9D%98-%EC%B0%A8%EC%9D%B4) — Romy(velog @beneficial). 저작권은 원저작자에게 있습니다.*

### 1) Reference Equality와 Value Equality의 구분

Java의 참조 타입 변수에는 객체 자체가 아니라 객체를 가리키는 참조값이 들어 있다. 그래서 비교 연산을 고를 때는 "참조를 비교할 것인가, 참조가 가리키는 객체의 값을 비교할 것인가"를 먼저 정해야 한다.

이 구분이 없으면 이 프로젝트의 `findById()`가 흔들린다. 저장소 안의 `Reservation.id`와 URL에서 들어온 `id`는 각각 다른 경로로 만들어진 `Long` 참조다. 둘이 같은 숫자를 나타내도 같은 객체라는 보장은 없다.

```text
a == b
→ 두 변수에 든 참조값을 비교
→ 같은 인스턴스를 가리킬 때만 true

a.equals(b)
→ a의 클래스가 재정의한 equals() 실행
→ Long.equals(): 인수가 null이 아니고 Long이며 같은 long 값이면 true
```

`==`는 객체 안을 들여다보지 않는다. 반면 `.equals()`는 클래스가 정한 "같다"의 정의를 따른다. `Long`은 담긴 `long` 값이 같으면 같다고 정의한다.

| 비교 수단 | 비교 대상 | 이 프로젝트에서 맞는 자리 |
|---|---|---|
| `==` | 참조값 | `getId() == null`, Singleton Bean 두 조회 |
| `.equals()` | 클래스가 정의한 논리적 값 | 두 `Long` 예약 번호 |
| `assertSame` / `assertEquals` | 각각 `==` / `.equals()` 기준 | Bean 동일성 / 값 검증 |

한 가지 한계가 있다. `.equals()`가 값 비교가 되는 것은 클래스가 이를 재정의했을 때뿐이다. `Object`의 기본 `equals()`는 참조 비교와 같다. 현재 `Reservation`은 `equals()`를 재정의하지 않았으므로, 두 `Reservation`을 `.equals()`로 비교해도 같은 인스턴스인지만 확인한다.

같은 파일 안에 두 비교가 나란히 있다는 점도 다시 확인했다. `save()`의 `reservation.getId() == null`은 "참조가 비어 있는가"를 묻는 자리라 `==`가 맞다. `findById()`의 `r.getId().equals(id)`는 "같은 번호인가"를 묻는 자리라 `.equals()`가 맞다.

> **정리.** `==`는 같은 객체인지, `.equals()`는 클래스가 정의한 같은 값인지 묻는다. 연산자보다 질문을 먼저 정한다.

### 2) Long boxing 캐시와 우연한 통과

`Long` 값 비교에서 `==`가 특히 위험한 이유는 작은 숫자에서 우연히 맞아 보이기 때문이다. 현재 저장소는 `long nextId`를 `assignId(Long id)`에 넘기므로 원시 타입 `long`이 `Long` 객체로 boxing된다.

```text
assignId(nextId++)
→ javac가 autoboxing을 Long.valueOf(long) 호출로 컴파일
→ Long.valueOf는 -128~127 범위의 값을 항상 캐시
→ 범위 안: 같은 값이면 캐시된 같은 인스턴스를 재사용할 수 있음
→ 범위 밖: 같은 값이어도 같은 인스턴스라는 보장 없음
```

그래서 id가 1, 2, 3인 개발 초기 데이터로만 확인하면 `==`도 통과하는 것처럼 보일 수 있다. 연산자의 의미가 바뀐 것이 아니라, 두 참조가 캐시 때문에 같은 인스턴스를 가리켰을 뿐이다.

`Long.valueOf` 문서는 범위 밖의 값도 **캐시할 수 있다**고 적는다. 즉 1000을 `==`로 비교하면 반드시 `false`라는 뜻도 아니다. 결과가 구현과 생성 경로에 달려 있으므로 값 비교 수단으로 믿을 수 없다는 것이 정확한 결론이다.

![객체 다이어그램 세 구획. 첫째 구획에서 test 객체의 first와 second 두 링크가 같은 reservationService 인스턴스 하나를 가리키고, applicationContext도 그 하나를 관리하므로 first == second가 true이고 assertSame이 통과한다. 둘째 구획에서 r의 id 링크는 value 1000인 storedId, findById의 인수 링크는 value 1000인 requestedId를 가리켜 인스턴스가 둘이므로 == 결과는 보장되지 않고 equals는 true다. 셋째 구획에서 value 1인 Long은 캐시된 인스턴스 하나를 두 링크가 공유해 ==가 우연히 true일 수 있다. Long 구획은 캐시 규칙에 따른 도식이며 실행 측정은 하지 않았다.](https://raw.githubusercontent.com/enderpawar/8week_Spring_Study/master/app/study_docs/assets/day06-reference-identity.png)

이 함정은 Day04에서 처음 만났다. 당시 `findById()`에서 `.equals()`를 쓴 이유를 "null값 탐지"라고 답했다가 교정했고, D6 누적시험의 `Long` 값 비교 문항은 통과했다. 작은 값과 큰 값의 `==` 결과를 직접 실행해 비교한 기록은 없으므로 위 대조군은 **미검증**이다.

CS 관점에서는 객체의 identity와 equality 구분이다. identity는 "같은 객체인가", equality는 "같다고 정의한 관계에 있는가"다. vocab에 적은 **동등 관계**라는 말처럼, `equals()`는 클래스가 정하는 관계이고 `==`는 언어가 정한 참조 비교다.

### 3) Singleton Scope와 Bean Reference Equality

Service는 요청마다 새로 만들 필요가 없다. 상태 없이 규칙만 실행하는 객체라면 하나를 만들어 여러 요청이 함께 쓰는 편이 생성 비용과 관리 측면에서 단순하다. Spring의 기본 scope인 Singleton이 이 선택을 제공한다.

```text
ApplicationContext 초기화
→ @Service가 붙은 ReservationService의 Bean 정의 등록
→ 생성자에 ReservationRepository Bean을 넣어 인스턴스 1개 생성
→ 컨테이너가 singleton Bean 캐시에 보관
→ getBean(ReservationService.class) 첫 호출: 캐시의 참조 반환
→ 두 번째 호출: 같은 참조 반환
→ assertSame(first, second): 두 참조값이 같으므로 통과
```

`getBean()`은 호출할 때마다 객체를 만드는 메서드가 아니라, 컨테이너가 이미 관리 중인 Bean을 조회하는 메서드다. 그래서 두 조회 결과를 비교하는 질문은 "값이 같은가"가 아니라 "같은 Bean인가"이며, 여기서는 `==`와 `assertSame`이 정확한 도구다.

Spring 공식 문서는 Bean 정의 하나에서 인스턴스가 한 번만 만들어지고, 그 같은 인스턴스가 협력 객체마다 주입되는 구조를 다음처럼 그린다.

![Spring Singleton scope 도식. 오른쪽의 accountDao Bean 정의 하나에서 인스턴스가 한 번만 생성되고(원 안의 1), 그 같은 공유 인스턴스가 화살표를 따라 왼쪽의 세 협력 Bean 정의에 ref="accountDao"로 각각 주입된다. 위 문구는 Only one instance is ever created, 아래 문구는 and this same shared instance is injected into each collaborating object다.](https://raw.githubusercontent.com/enderpawar/8week_Spring_Study/master/app/study_docs/assets/day06-web-singleton-scope.png)

*출처: [Spring Framework Reference — Bean Scopes, The Singleton Scope](https://docs.spring.io/spring-framework/reference/core/beans/factory-scopes.html) — Copyright © 2005 - Broadcom. All Rights Reserved. (문서 사본은 무료 배포와 저작권 고지 유지 조건으로 허용)*

Spring의 Singleton은 GoF Singleton 패턴과 범위가 다르다.

| 구분 | 인스턴스가 하나인 범위 | 이 글의 관찰 |
|---|---|---|
| Spring Singleton scope | ApplicationContext 하나 안의 Bean 정의 하나 | 같은 context에서 두 번 조회 |
| GoF Singleton 패턴 | ClassLoader 안의 클래스 하나 | 사용하지 않음 |

보장하는 것은 **같은 컨테이너, 같은 Bean 정의**에서 같은 인스턴스를 받는다는 점이다. 스레드 안전성은 보장하지 않는다. 여러 요청이 같은 `ReservationService`를 함께 쓰므로 `currentRequesterName` 같은 요청별 값을 필드에 두면 다른 요청이 덮어쓸 수 있다.

현재 `reserve(String roomName, String requesterName)`는 요청 값을 매개변수와 지역변수로만 다룬다. 필드는 생성 시 한 번 주입되는 `final` 저장소 참조 하나뿐이다. 이 경쟁 상태를 여러 스레드로 실제 재현하지는 않았다.

> **정리.** Singleton Bean은 같은 ApplicationContext 안에서 같은 참조를 돌려준다. 그래서 `==`가 맞고, 같은 이유로 요청별 상태를 필드에 두면 안 된다.

이후 Day16에서 `@Transactional`을 붙인 뒤에는 이 참조가 원본 클래스가 아니라 CGLIB 프록시라는 사실을 확인했다. 그때도 두 조회 결과는 같은 Singleton 프록시였다.

### 4) PK와 단건 갱신 대상의 식별

기존 예약 하나를 취소하려면 "어느 예약인가"를 가리킬 수단이 필요하다. Day04에서 `cancel()`을 `new Reservation(roomName, requesterName)`으로 만들었을 때, 예약이 취소되는 대신 같은 값의 새 인스턴스가 하나 더 생겼다. 값이 같아도 다른 객체였기 때문이다.

방 이름과 예약자 이름은 중복될 수 있다. 같은 사람이 같은 방을 두 번 예약하면 두 데이터는 값으로 구분되지 않는다. 조건 검색 자체는 가능하지만, 결과가 여러 개일 때 어느 하나를 갱신할지 정할 규칙이 없다.

```text
reserve 요청
→ Service가 new Reservation(...) 생성 후 confirm()
→ Repository.save()가 id가 null임을 확인하고 assignId(nextId++)
→ 응답 문자열에 "예약 번호" 포함
→ 클라이언트가 POST /reservations/cancel/{id}
→ findById(id)가 r.getId().equals(id)로 같은 번호의 인스턴스를 찾음
→ 찾은 그 인스턴스에 cancel()
```

| 기준 | 할 수 있는 일 | 단건 갱신 계약 |
|---|---|---|
| 조건 검색(이름·방) | 조건에 맞는 데이터 조회 | 결과가 여럿이면 성립하지 않음 |
| PK(`id`) | 데이터 하나를 고유하게 지목 | 성립 |

PK의 역할은 조회 기능을 가능하게 만드는 데 있지 않다. **갱신 대상을 고유하게 지목해 단건 갱신 계약을 성립시키는 것**이 역할이다. 이 기준은 D7에서 `save()`가 신규와 기존을 id로 나누는 분기로 이어졌다.

현재 id는 메모리의 `nextId` 카운터라서 프로세스를 재시작하면 1부터 다시 시작한다. DB 기본키로 옮기는 작업은 Week B에서 진행한다. CS 관점에서는 관계형 DB의 엔티티 무결성, 즉 기본키가 행 하나를 유일하게 식별해야 한다는 규칙과 같은 질문이다.

### 5) Constructor Injection의 타입 연결

`ReservationService`는 저장소 없이 동작할 수 없다. 이 필수 의존성을 생성자 매개변수로 드러내면, 컨테이너는 생성자를 보고 무엇을 넣어야 하는지 알 수 있고 객체는 생성 직후부터 완전한 상태가 된다.

```text
컨테이너가 ReservationService 생성자를 확인
→ 매개변수 타입 ReservationRepository 확인
→ 그 타입에 대입 가능한 Bean(InMemoryReservationRepository) 선택
→ new ReservationService(그 Bean) 호출
→ this.reservationRepository = reservationRepository
```

여기서 연결 기준은 "저장소 역할처럼 보이는 객체"가 아니라 Java 타입이다. 독립 작성 때 확인할 항목은 세 가지로 정리했다.

1. 생성자 이름이 클래스 이름과 같은가
2. `this.x`의 `x`가 현재 클래스에 선언된 필드인가
3. 매개변수 타입을 그 필드에 대입할 수 있는가

필드 타입이 구현체가 아니라 `ReservationRepository` 인터페이스라는 점은 DIP와 연결된다. Service가 계약에만 의존하므로 메모리 저장소를 JPA 저장소로 바꿀 때 상위 계층까지 수정이 연쇄 전파되는 것을 줄일 수 있다. 실제 교체는 Week B의 범위다.

IoC와 DI의 구분도 같은 흐름에 있다. IoC는 객체 그래프의 생성·연결을 누가 제어하는지에 관한 원칙이고, DI는 그 과정에서 의존 객체를 생성자 같은 통로로 전달하는 구체적 방식이다.

### 6) Request Mapping과 컴파일 검사의 경계

`@PathVariable` 문항은 통과했지만, Day04에서 겪은 경계를 다시 설명한 문항이라 짧게 남긴다. URL 템플릿 `"/reservations/cancel/{id}"`는 문자열이므로 Java 컴파일러가 그 안의 `{id}`와 매개변수 이름을 대조하지 않는다.

```text
./gradlew compileJava → 성공 (문자열 내용은 검사 대상 아님)
./gradlew test        → 성공 (당시 HTTP 요청 테스트 없음)
실제 요청             → Spring MVC가 요청 매핑 시점에 템플릿 변수를 찾음
                     → 실패
```

Day04 당시 실제 응답은 `{"error":"Required URI template variable 'id' for method parameter type Long is not present"}`였다. 이 경계는 D7에서 MockMvc 테스트를 추가하는 이유와도 이어진다.

> **정리.** 같은 대상이라는 판정에는 항상 기준이 필요하다. 객체는 참조, 값은 `equals()`, 데이터는 PK, 의존성은 타입이 그 기준이다.

> **더 볼 것**
> - [JLS 5.1.7 Boxing Conversion](https://docs.oracle.com/javase/specs/jls/se17/html/jls-5.html#jls-5.1.7): 작은 정수 boxing 결과의 참조 재사용 규칙
> - [Java `Long` API](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Long.html): `valueOf(long)`의 -128~127 캐시와 `equals()` 정의
> - [Spring Bean Scopes](https://docs.spring.io/spring-framework/reference/core/beans/factory-scopes.html): Singleton scope의 범위와 GoF Singleton과의 차이
> - 아직 안 본 것 — 여러 스레드가 Singleton Service를 공유할 때의 경쟁 상태 재현, DB 기본키 생성 전략

## 2. 코드 구현

### 1) 누적시험 범위와 결과

Day01~03 문항은 앞선 +2 인출에서 통과했고 다음 복습일이 아직 오지 않아 간격을 유지했다. 이번 시험은 Day04~05에서 도래한 문항과 오답 재시험을 중심으로 진행했다.

| 확인한 항목 | 결과 | 다시 잡은 기준 |
|---|---|---|
| IoC와 DI 구분 | 통과 | 조립 제어권과 의존 객체 전달을 구분 |
| `Long` 값 비교 | 통과 | 참조 동일성과 값 동일성을 구분 |
| Singleton Bean의 `==` | 교정 후 통과 | 값이 아니라 같은 Bean 참조 |
| Repository 인터페이스 의존 | 통과 | 구현 교체 시 변경 전파 감소 |
| PK가 필요한 이유 | 교정 후 통과 | 단건 갱신 대상을 고유하게 식별 |
| `@PathVariable` 불일치 | 통과 | 컴파일이 아닌 요청 처리 시점의 문제 |
| 생성자 주입 문법 | 교정 후 통과 | 생성자명·필드명·타입 확인 |

### 2) 같은 Bean과 같은 값의 비교 코드

```java
// StudyRoomApiApplicationTests — 같은 Bean인지: 참조 비교
ReservationService first = applicationContext.getBean(ReservationService.class);
ReservationService second = applicationContext.getBean(ReservationService.class);
assertSame(first, second);

// InMemoryReservationRepository.findById() — 같은 번호인지: 값 비교
for (Reservation r : store) {
    if (r.getId().equals(id)) {
        return Optional.of(r);
    }
}
```

`assertSame`은 `.equals()`를 호출하지 않고 참조만 비교한다. 테스트 통과는 두 Service의 필드 값이 우연히 같다는 뜻이 아니라 컨테이너가 같은 인스턴스를 반환했다는 뜻이다.

`findById()`에서는 두 `Long`이 같은 객체인지가 중요하지 않다. 저장된 예약 번호와 URL로 받은 번호가 같은 숫자인지가 중요하므로 `.equals()`가 저장소 계약에 맞다. 반환형 `Optional`은 같은 커밋의 D7 작업에서 바뀐 부분이다.

### 3) Constructor Injection의 필드 대입

```java
private final ReservationRepository reservationRepository;

public ReservationService(ReservationRepository reservationRepository) {
    this.reservationRepository = reservationRepository;
}
```

`this.reservationRepository`는 현재 객체의 필드이고, 오른쪽의 `reservationRepository`는 생성자 매개변수다. 매개변수 타입이 필드 타입과 같으므로 대입이 성립한다.

### 4) 자동 검증 결과

| 검증 항목 | 근거 | 결과 |
|---|---|---|
| Singleton Service 동일성 | `reservationServiceBeanIsSingleton()` | 통과 |
| ID 기반 조회와 취소 | `ReservationServiceTest` | 통과 |
| 누적 인출 | `quiz.md` 7문항 | 전부 통과(3문항 교정 후) |

전체 Gradle 테스트 10개가 통과했다. 테스트 통과는 코드 상태의 증거이며 인출 답안 자체를 대신하지 않는다. 성능 측정이나 동시성 경쟁 상태 재현은 하지 않았다.

검증 코드와 Day06·07 산출물: [commit `6c88dcb`](https://github.com/enderpawar/8week_Spring_Study/commit/6c88dcb)

## 3. 스스로 답한 질문

### 1) Singleton Bean 비교 결과의 판단 근거

**질문.** 같은 ApplicationContext에서 `ReservationService` Bean을 두 번 조회해 `==`로 비교하면 왜 `true`인가?

**A1.** 처음 기록은 "객체 상으로는 동일해 보이지만 값이 다르기 때문"이었다. 여기에는 값 비교와 참조 비교가 섞여 있었다. `==`는 값이 다른지를 판단하지 않고, 두 변수가 같은 객체를 가리키는지를 본다.

교정된 답은 "기본 Singleton scope 때문에 같은 컨테이너가 동일한 Bean 인스턴스를 반환하므로 `true`"다. 근거는 `reservationServiceBeanIsSingleton()`의 `assertSame(first, second)` 통과다.

재발 방지로, 연산자를 고르기 전에 비교 목적을 "참조인가, 값인가"로 먼저 말한다. 그다음 "두 변수가 같은 객체를 가리킬 경로가 있는가"를 확인한다.

### 2) PK 없이 수정하는 방식의 한계

**질문.** 기존 예약을 안전하게 수정하는 데 PK가 필요한 이유는 무엇인가?

**A2.** 처음에는 `id`가 없어도 수정할 수 있다고 설명했다. 이름이나 방 번호로 조건 검색을 하는 것 자체는 가능하기 때문이다.

하지만 값이 중복되면 어느 하나를 수정할지 결정할 수 없으므로 안전한 단건 갱신 계약이 되지 못한다. 교정 뒤에는 PK를 "조회 기능을 가능하게 하는 값"이 아니라 "대상 하나를 고유하게 식별하는 값"으로 설명했다.

이 기준은 Day07에서 저장소의 추가와 교체 분기에 그대로 사용했다.

### 3) Constructor Injection에서 `this.x`의 대상

**질문.** 생성자 주입 코드의 `this.x`에서 `x`는 무엇을 가리키는가?

**A3.** 처음에는 `this.x`의 대상을 메서드라고 답했다. `this`는 현재 객체이고 `this.x`는 그 객체의 필드를 가리킨다. 생성자 매개변수의 값을 현재 객체 필드에 대입하는 코드다.

Controller의 생성자 모양을 그대로 복사하는 것이 아니라, 현재 클래스의 이름·필드·필요한 의존 타입에 맞게 바꿔야 한다. 재발 방지로 생성자명과 클래스명, `this` 뒤의 실제 필드명, 필드와 매개변수의 대입 가능한 타입을 순서대로 확인한다.

## 4. 학습 정리와 다음 범위

누적시험 전에는 `==`를 "객체에서 쓰면 안 되는 연산자"처럼 외우기 쉬웠다. 지금은 같은 연산자가 Singleton Bean 확인에는 정확하고 `Long` 값 비교에는 부정확한 이유를 비교 목적과 인스턴스 생성 경로로 나눠 설명할 수 있다.

이 구분은 PK와 생성자 주입에도 이어졌다. 객체나 데이터가 비슷해 보인다는 사실이 같은 대상을 보장하지 않는다. 참조, `equals()` 정의, 고유 식별자, 타입 계약처럼 시스템이 판정할 수 있는 기준이 필요하다.

**아직 남은 것**은 Singleton Service의 공유 필드 경쟁 상태를 여러 스레드로 재현하지 않았다는 점이다. 이번 트랙에서는 **고치지 않을 것**으로 분류했고, 요청별 값은 필드가 아닌 지역변수에 둔다는 규칙만 현재 코드에 적용한다. 다음 범위는 Week A D7 버퍼로, Day03·04의 즉시 수정 기술부채를 처리한다.

면접에서 다시 답해볼 항목을 남긴다.

- Spring Singleton scope와 GoF Singleton 패턴의 범위 차이
- `Long` 식별자를 `==`로 비교한 코드가 테스트를 통과하는 조건

<!-- 선택 복습 메모: 게시 화면에는 노출하지 않는다.
### 1) 선택 추가 설명

[직접 작성] Singleton Bean 비교에서는 `==`가 의미 있고, `Long` 식별자 값 비교에서는 `.equals()`가 필요한 이유를 한 문단으로 설명한다.
-->
