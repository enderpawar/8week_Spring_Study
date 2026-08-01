# [백엔드 기본기 Day 4] Service와 Repository — 책임 분리와 식별자로 기존 예약 찾기

Day 3까지 `ReservationController`가 HTTP 요청 처리뿐 아니라 예약 객체 생성과 상태 변경까지 맡고 있었다. Day 4에는 예약 규칙을 `ReservationService`로, 저장소 접근을 `ReservationRepository`로 분리하고, `cancel()`이 새 객체를 만들지 않고 기존 예약을 찾도록 식별자를 도입했다. 생성자 주입의 내부 원리와 실제 데이터베이스 영속성은 아직 다루지 않는다.

## 한눈에 보기

- **문제:** 취소 요청에서 같은 방 이름과 예약자 이름으로 새 `Reservation`을 만들자 기존 예약의 상태는 그대로이고 새 예약이 추가됐다. 값이 같아 보이는 것만으로는 저장된 객체를 특정할 수 없었다.
- **적용:** Controller는 HTTP 입출력, Service는 예약 상태 변경, Repository는 저장과 조회를 맡도록 분리했다. `Reservation`에 `id`를 추가하고 취소 경로를 `POST /reservations/cancel/{id}`로 바꿨다.
- **검증:** `compileJava`와 `test` 성공을 확인했고, 애플리케이션을 실행해 예약 생성 후 ID 1 취소 요청이 `confirmed: false`를 반환하는 정상 경로를 수동 검증했다. 잘못 연결한 `@PathVariable`은 실제 요청에서 500이 발생한 뒤 경로를 고쳤다.
- **한계:** 자동화 테스트는 컨텍스트 기동만 확인한다. `save()`는 기존 객체도 무조건 리스트에 추가하며, `findById()`는 없는 ID에 `null`을 반환하므로 중복 저장과 미존재 예약 처리에는 검증되지 않은 결함이 남아 있다.

## 1. 문제를 이해하기 위한 이론

### 왜 계층과 식별자가 필요한가

Controller가 HTTP 요청 해석, 예약 규칙, 저장 방식까지 모두 알면 변경 이유가 한 클래스에 모인다. URL이나 요청 형식이 바뀌어도 Controller를 고쳐야 하고, 예약 상태 규칙이나 저장 방식이 바뀌어도 같은 클래스를 고쳐야 한다. 또한 상태 변경 규칙을 확인하려면 매번 HTTP 요청을 거쳐야 한다.

책임을 분리해도 취소할 대상을 특정하지 못하면 기존 예약을 변경할 수 없다. 처음 작성한 취소 로직은 전달받은 방 이름과 예약자 이름으로 `new Reservation(...)`을 호출했다. 두 문자열이 같더라도 새로 생성한 인스턴스는 저장소에 있던 인스턴스와 다른 객체다. 더구나 이름 조합은 중복될 수 있으므로 “어느 예약인가”를 안정적으로 가리키는 식별자가 필요했다.

### 핵심 용어

- **Controller 계층:** HTTP 경로와 요청 값을 애플리케이션 호출로 연결하고 응답을 만드는 경계다.
- **Service 계층:** 예약 확정·취소처럼 애플리케이션의 사용 사례와 비즈니스 흐름을 조율한다. 현재 상태 변경 규칙 자체는 `Reservation.confirm()`과 `cancel()` 안에 있다.
- **Repository 계층:** 도메인 객체의 저장과 조회 방법을 감싼다. 인터페이스인 것은 구현 방식이고, 책임은 저장소 접근이다.
- **식별자(identifier):** 각 객체를 다른 객체와 구분하는 값이다. 현재의 `id`는 메모리 저장소에서 순차 부여되며, 데이터베이스의 기본키와 같은 식별 목적을 연습한다.
- **참조 동일성·값 동일성:** 객체에 `==`를 사용하면 같은 객체를 가리키는지 비교한다. `Long.equals()`는 감싼 숫자 값이 같은지 비교한다.
- **`@PathVariable`:** URI 템플릿의 `{id}` 값을 Controller 메서드 파라미터에 연결한다.

단일 책임 원칙(SRP)은 모든 메서드를 별도 클래스로 나누라는 뜻이 아니라, 클래스가 함께 묶을 책임과 변경 이유를 분명히 하라는 원칙이다. 의존성 역전 원칙(DIP)은 상위 정책이 구체 저장 기술에 직접 묶이지 않고 추상화에 의존하도록 한다. 현재 `ReservationService`가 `InMemoryReservationRepository`가 아닌 `ReservationRepository`를 생성자 파라미터로 받는 부분이 이 방향을 보여준다. 다만 인터페이스를 썼다고 향후 구현 교체가 무조건 수정 없이 끝나는 것은 아니며, 새 구현이 같은 계약을 지키고 Spring 설정도 맞아야 한다.

### 요청에서 상태 변경까지의 흐름

예약 생성은 다음 순서로 진행된다.

```text
POST /reservations
→ ReservationController.reserve(request)
→ ReservationService.reserve(roomName, requesterName)
→ new Reservation(...) 후 confirm()
→ InMemoryReservationRepository.save(reservation)
→ id가 없으면 순차 ID를 부여하고 리스트에 추가
→ Controller가 생성 결과를 문자열 응답으로 반환
```

취소는 새 객체를 만들지 않고 식별자로 기존 객체를 찾는다.

```text
POST /reservations/cancel/{id}
→ ReservationController.cancel(id)
→ ReservationService.cancel(id)
→ ReservationRepository.findById(id)
→ 찾은 Reservation.cancel()
→ Repository.save(reservation)
→ Controller가 confirmed: false인 결과를 반환
```

이 흐름에는 중요한 구현 문제가 하나 남아 있다. 메모리 저장소의 리스트에는 가변 `Reservation` 객체의 참조가 들어간다. `findById()`가 반환한 객체를 `cancel()`로 변경하면 리스트가 가리키는 같은 객체도 이미 변경된 상태다. 그런데 Service가 다시 `save()`를 호출하고, `save()`는 ID 유무와 관계없이 `store.add(reservation)`을 실행한다. 따라서 같은 객체 참조가 리스트에 중복으로 들어갈 수 있다.

### CS 지식과 연결

식별자는 데이터베이스의 기본키와 같은 문제를 해결한다. 방 이름과 예약자 이름 같은 일반 속성은 중복될 수 있지만, 기본키는 한 행을 유일하게 가리켜 조회·수정·삭제의 대상을 정한다. 현재 구현은 데이터베이스가 아니라 `ArrayList`와 증가하는 `nextId`로 이 개념을 축소해 재현했다.

`findById()`는 리스트를 앞에서부터 순회하므로 코드상 시간 복잡도는 `O(n)`이다. 이후 데이터베이스에서 기본키 인덱스를 사용하면 저장 구조와 조회 방식이 달라진다. Day 4에서는 성능을 측정하지 않았으며, 여기서의 복잡도는 현재 반복문 구조에 대한 분석이다.

또한 리스트에는 객체의 복사본이 아니라 참조가 저장된다. 같은 참조를 두 번 추가하면 리스트 원소 수는 늘어나지만 두 원소가 가리키는 객체는 같을 수 있다. 이는 취소 후 `save()`가 “갱신”처럼 보이면서도 내부적으로 중복을 만들 수 있는 이유다.

### 현재 코드에서 찾기

- [`ReservationController.java`](https://github.com/enderpawar/8week_Spring_Study/blob/e22bb34/app/src/main/java/com/example/studyroom/controller/ReservationController.java): HTTP 요청을 Service 호출로 연결하고 ID를 경로에서 받는다.
- [`ReservationService.java`](https://github.com/enderpawar/8week_Spring_Study/blob/e22bb34/app/src/main/java/com/example/studyroom/service/ReservationService.java): 예약 생성과 기존 예약 취소 흐름을 조율한다.
- [`ReservationRepository.java`](https://github.com/enderpawar/8week_Spring_Study/blob/e22bb34/app/src/main/java/com/example/studyroom/repository/ReservationRepository.java): `save`, `findAll`, `findById`라는 저장소 계약을 선언한다.
- [`InMemoryReservationRepository.java`](https://github.com/enderpawar/8week_Spring_Study/blob/e22bb34/app/src/main/java/com/example/studyroom/repository/InMemoryReservationRepository.java): 리스트 저장, ID 부여, 선형 조회를 구현한다.
- [`Reservation.java`](https://github.com/enderpawar/8week_Spring_Study/blob/e22bb34/app/src/main/java/com/example/studyroom/domain/Reservation.java): 식별자와 예약 상태를 가지며 `confirm()`과 `cancel()`로 상태를 변경한다.

## 2. 설계 선택과 대안

### 역할을 HTTP·사용 사례·저장 접근으로 나눴다

Controller에는 `@RequestBody`, `@PathVariable`, 응답 문자열처럼 HTTP 경계의 코드를 남겼다. Service는 생성·확정·조회·취소의 순서를 결정하고, Repository는 객체를 어떻게 저장하고 찾는지 맡았다. Domain은 외부에서 `confirmed` 필드를 직접 바꾸지 못하게 하고 상태 변경 메서드를 제공한다.

| 선택지 | 장점 | 단점·적합하지 않았던 이유 |
|---|---|---|
| Controller → Service → Repository | HTTP, 사용 사례, 저장 방식의 변경 이유를 분리할 수 있다. Service 로직을 HTTP 없이 테스트할 구조가 된다. | 클래스와 연결 지점이 늘어나며, 현재는 실제 Service 단위 테스트가 아직 없다. |
| Controller에서 객체 생성·저장·변경 | 작은 예제를 빠르게 작성할 수 있다. | HTTP 변경과 비즈니스 규칙 변경이 Controller에 함께 누적되고 저장 구현에도 직접 결합한다. |

Service는 구체 클래스가 아니라 `ReservationRepository` 인터페이스에 의존한다. 현재는 메모리 구현 하나뿐이므로 효과가 작아 보이지만, 저장 계약과 구현 세부를 분리하는 경계를 먼저 만든 것이다. 실제 JPA 구현으로 교체할 때 계약이 충분한지는 Week B에서 다시 확인해야 한다.

취소 대상을 방 이름과 예약자 이름의 조합이 아니라 ID로 찾도록 바꿨다. 이름 조합은 사람이 읽기 쉽지만 중복 가능성이 있고 변경에도 취약하다. ID는 의미 없는 값에 가깝지만 한 예약을 안정적으로 지칭할 수 있다.

## 3. 코드로 적용하기

Service는 Repository 인터페이스만 알고 사용 사례의 순서를 표현한다.

```java
@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Reservation reserve(String roomName, String requesterName) {
        Reservation reservation = new Reservation(roomName, requesterName);
        reservation.confirm();
        return reservationRepository.save(reservation);
    }

    public Reservation cancel(Long id) {
        Reservation reservation = reservationRepository.findById(id);
        reservation.cancel();
        reservationRepository.save(reservation);
        return reservation;
    }
}
```

`reserve()`는 새 객체를 만든 뒤 확정하고 저장한다. `cancel()`은 ID로 저장된 객체를 찾아 상태를 변경한다. 두 메서드의 차이는 “새 객체 생성”과 “기존 객체 조회”이며, 이 차이가 식별자를 도입한 이유다.

메모리 저장소는 다음과 같이 ID를 부여하고 조회한다.

```java
public Reservation save(Reservation reservation) {
    if (reservation.getId() == null) {
        reservation.assignId(nextId++);
    }
    store.add(reservation);
    return reservation;
}

public Reservation findById(Long id) {
    for (Reservation reservation : store) {
        if (reservation.getId().equals(id)) {
            return reservation;
        }
    }
    return null;
}
```

새 예약은 ID가 `null`이므로 `nextId`를 받고 리스트에 들어간다. 조회 시에는 `Long` 객체의 참조가 아니라 값이 같은지 확인하려고 `.equals()`를 사용한다. 그러나 `save()`의 `store.add()`는 새 객체와 기존 객체를 구분하지 않고, 조회 실패도 `null`로 표현한다. 코드는 컴파일되지만 저장과 실패 처리 계약은 아직 완성되지 않았다.

## 4. 예측 → 실행 → 차이 설명

| 구분 | 기록 |
|---|---|
| 실행 전 예측 | `cancel()`을 `reserve()`와 같은 방식으로 옮기면 기존에 저장된 예약을 찾아 `confirmed`만 `false`로 바꿀 것으로 예상했다. `@PathVariable Long id`를 추가한 뒤에는 컴파일이 통과했으므로 요청도 정상 동작할 것으로 예상했다. |
| 실행 또는 테스트 | 애플리케이션을 실행해 예약 생성과 취소 요청을 보냈다. 중간에는 `compileJava`와 `test`를 실행했고, URI 템플릿 수정 후 다시 `reserve → cancel/1` 흐름을 요청했다. |
| 실제 결과 | 처음 취소 구현은 기존 예약을 바꾸지 않고 새 예약을 추가했다. 또한 `{id}` 없는 `/reservations/cancel` 경로에 `@PathVariable Long id`를 선언했을 때 컴파일과 테스트는 성공했지만 실제 요청은 500과 `Required URI template variable 'id' for method parameter type Long is not present` 오류를 반환했다. 경로를 `/reservations/cancel/{id}`로 고친 뒤 ID 1 예약의 응답에서 `confirmed: false`를 확인했다. |
| 차이의 원인 | 같은 속성값으로 생성한 객체도 기존 객체와는 다른 인스턴스이며, 기존 대상을 찾으려면 식별자 조회가 필요했다. URI 템플릿과 `@PathVariable`의 일치 여부는 Java 컴파일 규칙이 아니라 Spring MVC가 실제 요청을 바인딩할 때 검사되므로 현재의 컨텍스트 테스트만으로는 드러나지 않았다. |

예측과 실제의 차이는 계층을 나누는 것만으로 동작이 자동 교정되지는 않는다는 점을 보여줬다. Service가 기존 객체를 변경하려면 Repository가 식별자로 그 객체를 찾는 계약을 제공해야 한다. 또한 빌드 성공은 코드가 컴파일되고 현재 테스트가 통과했다는 뜻이지, 모든 HTTP 경로가 올바르게 연결됐다는 뜻은 아니다.

## 5. 검증 근거

| 검증 대상 | 검증 방법 | 확인한 결과 |
|---|---|---|
| Java 컴파일 | `./gradlew compileJava` | 성공 |
| 현재 테스트 모음 | `./gradlew test` | 성공. 실제 테스트 메서드는 애플리케이션 컨텍스트 기동을 확인하는 `contextLoads()` 하나다. |
| 잘못된 취소 경로 | `{id}` 없는 경로로 애플리케이션 실행 후 `POST /reservations/cancel` 요청 | `500`, `Required URI template variable 'id' for method parameter type Long is not present` |
| 수정한 정상 경로 | 예약 생성 후 `POST /reservations/cancel/1` 요청 | 생성 응답은 `예약 번호1-... (확정: true)`, 취소 응답은 같은 예약자·방에 대해 `(확정 : false)` |

최종 구현과 Day 4 산출물은 [`e22bb34` 커밋](https://github.com/enderpawar/8week_Spring_Study/commit/e22bb34)에서 확인할 수 있다. 수동 정상 경로는 응답의 상태 변경을 확인했지만, 저장소 리스트의 원소 수나 동일 객체 중복 여부까지 검증한 것은 아니다.

## 6. 막힌 지점과 오답 교정

### Repository의 책임을 인터페이스 자체로 설명했다

처음에는 Repository의 책임을 “인터페이스 역할 수행”이라고 답했다. 인터페이스는 `ReservationRepository`의 구현 형태일 뿐 계층의 책임은 아니다. Repository의 책임은 도메인 객체의 저장과 조회를 저장 기술로부터 감싸는 것이다. 이후에는 “무엇을 책임지는가”와 “어떤 Java 장치로 구현했는가”를 구분해 설명해야 한다.

### `Long.equals()`를 null 탐지 방법으로 이해했다

`findById()`의 비교를 `==`에서 `.equals()`로 고친 이유를 처음에는 “null값 탐지”라고 답했다. 실제 이유는 객체의 참조 동일성과 값 동일성을 구분하기 위해서다. 작은 `Long` 값은 boxing 과정에서 같은 객체가 재사용될 수 있어 `==`가 우연히 참이 될 수 있지만, 그 동작에 값 비교를 맡기면 안 된다.

`.equals()`로 바꾼 것만으로 조회 실패 처리가 해결된 것도 아니다. 현재 `findById()`는 일치하는 값이 없으면 `null`을 반환하고, Service는 확인 없이 `reservation.cancel()`을 호출한다. 이는 값 비교 문제와 “없음”을 표현하는 계약 문제가 서로 다르다는 뜻이다.

### 반환문을 추가해 컴파일만 통과시켰다

처음 작성한 `findById()`는 반복문 안에서 일치할 때만 반환하고, 끝까지 찾지 못했을 때의 반환문이 없어 `missing return statement`가 발생했다. `return null`을 추가하면 Java의 모든 실행 경로가 값을 반환한다는 조건은 만족한다. 그러나 예약이 없을 때 무엇을 해야 하는지 결정한 것은 아니다. 컴파일러가 요구한 문법적 완결성과 애플리케이션의 실패 정책을 분리해서 봐야 한다.

### 경로 템플릿과 파라미터의 연결은 실제 요청에서 깨졌다

`@PathVariable Long id`를 추가하면서 `@PostMapping`에는 `{id}`를 넣지 않았다. 컴파일과 `contextLoads()`는 성공했지만 실제 취소 요청에서 URI 값을 채울 수 없어 500이 발생했다. 경로를 `/reservations/cancel/{id}`로 고쳐 정상 요청을 확인했다. 같은 문제를 자동으로 막으려면 해당 URL로 요청을 보내 상태와 응답을 검증하는 웹 계층 테스트가 필요하다.

## 7. 현재 한계와 다음 개선

- **기존 예약의 중복 저장 가능성:** `cancel()`은 리스트 안의 객체를 변경한 뒤 `save()`를 다시 호출하고, `save()`는 무조건 `store.add()`를 수행한다. 같은 객체 참조가 중복될 수 있지만 Day 4에는 이를 세는 테스트나 조회 엔드포인트가 없어 실제 원소 수는 미검증이다. 메모리 저장소에서는 새 객체만 추가하거나, 기존 ID는 교체·무시하는 식으로 `save()` 계약을 명확히 해야 한다.
- **없는 ID의 `null` 처리:** `findById()`가 `null`을 반환하면 `ReservationService.cancel()`이 이를 역참조한다. 현재의 전역 `Exception` 처리기는 이런 예외를 500으로 감싸게 되어 있으며, “예약 없음”을 404 같은 명시적 도메인 오류로 바꾸는 처리는 없다. 이 경로에 실제 요청을 보내 확인한 기록도 없으므로 응답 본문은 추측하지 않는다.
- **테스트 범위:** `./gradlew test`는 성공했지만 `contextLoads()`만 존재한다. Service의 상태 변경, Repository의 ID 부여·조회·중복, Controller의 경로 바인딩을 검증하는 단위·웹 테스트는 없다.
- **메모리 저장소의 경계:** 데이터는 프로세스 재시작 시 사라지고 `ArrayList`와 `nextId`는 동시 요청에 안전하지 않다. `findAll()`도 내부 가변 리스트를 그대로 반환한다. Day 4는 계층과 식별자 학습용 구현이며 영속성과 동시성 보장은 Week B 이후의 범위다.

## 8. 복습을 위한 인출 질문

### Q1. Controller, Service, Repository가 각각 맡는 책임은 무엇인가?

<details>
<summary>답 확인</summary>

Controller는 HTTP 요청을 해석하고 응답을 만드는 경계다. Service는 예약 생성·취소 같은 사용 사례의 순서를 조율한다. Repository는 도메인 객체의 저장과 조회를 저장 기술로부터 감싼다. 인터페이스는 Repository 책임을 구현하는 수단이지 책임 자체가 아니다.

</details>

### Q2. 같은 방 이름과 예약자 이름으로 새 객체를 만들면 왜 기존 예약을 취소할 수 없는가?

<details>
<summary>답 확인</summary>

`new Reservation(...)`은 속성값이 같아도 기존 객체와 다른 인스턴스를 만든다. 이름 조합은 중복될 수도 있으므로 저장된 예약 하나를 안정적으로 특정하지 못한다. 기존 객체를 변경하려면 고유 식별자로 Repository에서 조회한 뒤 그 객체의 상태를 바꿔야 한다.

</details>

### Q3. `findById()`에서 `.equals()`를 쓰는 문제와 조회 실패 시 `null`을 반환하는 문제는 어떻게 다른가?

<details>
<summary>답 확인</summary>

`.equals()`는 두 `Long` 객체가 감싼 값이 같은지 비교하는 문제를 해결한다. 조회 결과가 없을 때 무엇을 반환하고 Service가 어떻게 대응할지는 별도의 실패 계약 문제다. 현재 코드는 `null`을 반환한 뒤 바로 역참조하므로 명시적인 예외나 `Optional` 같은 방식으로 “없음”을 처리해야 한다.

</details>

### Q4. `compileJava`와 현재 `test`가 성공했는데도 `@PathVariable` 오류가 실제 요청에서 발생한 이유는 무엇인가?

<details>
<summary>답 확인</summary>

URI 템플릿과 `@PathVariable`의 일치는 Java 문법이 아니라 Spring MVC의 요청 바인딩 규칙이다. 현재 테스트는 컨텍스트가 뜨는지만 확인하고 해당 취소 URL로 요청하지 않았다. 그래서 실제 요청이 그 경로를 실행했을 때에야 누락된 `{id}`가 드러났다.

</details>

## 정리하며

처음에는 계층을 나누면 기존 예약 취소도 같은 패턴으로 해결될 것으로 예상했다. 실제로는 새 객체와 저장된 객체를 구분할 식별자와 조회 계약이 먼저 필요했다. 또한 Repository의 책임과 인터페이스라는 구현 수단, `Long`의 값 비교와 조회 실패 처리를 각각 분리해 설명하게 됐다.

수동 요청으로 ID 기반 취소의 정상 응답은 확인했지만, 저장소 중복과 없는 ID 경로는 검증하지 않았다. 다음 단계인 IoC·DI·생성자 주입에서는 현재 `ReservationService`가 `ReservationRepository` 구현을 어떻게 전달받는지 확인하고, 데이터 접근 단계에서는 메모리 저장소의 저장 계약을 다시 비교한다.
