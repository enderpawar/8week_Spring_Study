# 같은 객체에서 HTTP 404까지 — 식별자 하나로 연결한 저장과 오류 경계

Week A의 마지막 날에는 누적시험과 기술부채 상환을 한꺼번에 진행했다. 처음에는 `==`와 `.equals()`를 구분하는 자바 문제처럼 보였지만, 코드를 따라가니 Singleton Bean의 동일성, 예약의 PK, 저장소의 추가·갱신 계약, `Optional`, HTTP 404가 하나의 흐름으로 이어졌다.

> 같은 대상을 판별하는 기준이 불분명하면 저장 중복이 생기고, 값이 없을 때의 계약이 불분명하면 `null`이 계층 사이를 흐른다. 이번 작업에서는 **식별 기준 → 저장 계약 → 부재 표현 → HTTP 오류 응답**을 연결하고 전체 10개 자동 테스트로 경계를 고정했다.

## 1. 개념 설명

| 용어 | 한줄뜻 | 코드 모습 |
|---|---|---|
| 참조 동일성 | 두 변수가 같은 객체를 가리키는지 비교 | `assertSame(first, second)` |
| 값 동일성 | 서로 다른 객체라도 논리적 값이 같은지 비교 | `storedId.equals(requestedId)` |
| PK | 데이터 하나를 고유하게 식별하는 값 | `Reservation.id` |
| 저장 계약 | 신규와 기존 데이터를 저장할 때의 동작 약속 | ID 없음은 추가, 기존 ID는 교체 |
| `Optional` | 값의 부재 가능성을 반환형에 드러내는 컨테이너 | `Optional<Reservation>` |
| 도메인 예외 | 업무 의미가 있는 실패 | `ReservationNotFoundException` |

### 비교 연산자는 타입보다 질문이 먼저다

참조 타입의 `==`는 두 변수가 같은 객체를 가리키는지 확인한다. `.equals()`는 타입이 정의한 논리적 값 동일성을 확인한다. 따라서 “객체 타입이면 무조건 `.equals()`”가 아니라 **지금 같은 객체를 찾는지, 같은 값을 찾는지**를 먼저 정해야 한다.

같은 ApplicationContext에서 `ReservationService` Bean을 두 번 조회할 때는 같은 객체인지가 질문이다. 기본 Singleton scope가 동일한 Bean 인스턴스를 반환하므로 `assertSame()`이 맞다.

예약 ID를 비교할 때는 저장된 `Long`과 URL로 받은 `Long`이 같은 숫자를 나타내는지가 중요하다. 작은 정수에서는 boxing 캐시 때문에 `==`가 우연히 통과할 수 있으므로 식별자 값은 `.equals()`로 비교한다.

### PK가 저장 계약을 만든다

방 이름이나 예약자 이름으로도 검색할 수는 있지만 두 값은 중복될 수 있다. 기존 예약 하나를 안전하게 수정하려면 고유한 `id`가 필요하다.

식별 기준이 생기면 `save()`의 동작도 구분할 수 있다.

- `id == null` → 신규 예약 → ID 부여 후 추가
- 기존 `id` → 같은 ID의 위치를 교체
- ID가 있지만 대상 없음 → 저장 계약 위반

기존 구현은 ID와 관계없이 `store.add()`를 호출했다. `ArrayList`는 같은 객체 참조의 중복 삽입을 막지 않으므로 취소한 예약을 다시 저장할 때 목록 크기가 늘어났다.

### 값의 부재는 계층마다 다른 언어로 바뀐다

`findById()`가 `null`을 반환하면 호출자는 선언만 보고 값 부재를 알 수 없다. 확인 없이 `cancel()`을 호출하면 “예약 없음”이 아니라 `NullPointerException`으로 드러난다.

이를 계층별 책임으로 나눴다.

`Repository의 Optional.empty()` → `Service의 ReservationNotFoundException` → `Web의 404 Not Found와 안전한 JSON 본문` 순서로 의미가 변한다.

Repository는 HTTP를 모르고, Service는 JSON을 모르며, Controller는 목록 순회 방식을 모른다. 각 계층은 자신이 이해하는 언어로 실패를 변환한다.

입력 자체가 잘못된 `id=0`은 Service까지 보내지 않는다. Controller 인수의 `@Positive`를 Spring MVC 메서드 검증이 확인하고, 전역 예외 처리기가 400으로 변환한다.

> **더 볼 것**
> - [JLS 5.1.7 Boxing Conversion](https://docs.oracle.com/javase/specs/jls/se17/html/jls-5.html#jls-5.1.7): Wrapper boxing과 작은 정수 참조 재사용 규칙
> - [Spring Bean Scopes](https://docs.spring.io/spring-framework/reference/core/beans/factory-scopes.html): Singleton scope의 정확한 범위
> - [Spring MVC Validation](https://docs.spring.io/spring-framework/reference/6.2/web/webmvc/mvc-controller/ann-validation.html): 본문 검증과 메서드 인수 검증의 차이
> - [Spring Controller Advice](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-advice.html): 전역 예외 처리기의 적용 방식

## 2. 코드 구현

### 저장소의 추가·교체와 Service의 부재 변환

핵심 구현은 신규·기존 분기와 `Optional` 변환이다. ID가 없는 객체만 추가하고, 기존 ID는 같은 위치를 교체한다. ID는 있지만 대상이 없으면 조용히 신규 데이터로 넣지 않는다.

```java
public Reservation save(Reservation reservation) {
    if (reservation.getId() == null) {
        reservation.assignId(nextId++);
        store.add(reservation);
        return reservation;
    }

    for (int index = 0; index < store.size(); index++) {
        if (store.get(index).getId().equals(reservation.getId())) {
            store.set(index, reservation);
            return reservation;
        }
    }
    throw new IllegalArgumentException("저장소에 없는 예약 번호입니다");
}

public Reservation cancel(Long id) {
    Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new ReservationNotFoundException(id));
    reservation.cancel();
    reservationRepository.save(reservation);
    return reservation;
}
```

`Optional`을 반환하는 것만으로 오류 처리가 끝나지는 않는다. Service의 `orElseThrow()`가 값 부재에 “예약 없음”이라는 업무 의미를 붙여야 웹 계층이 이를 404로 변환할 수 있다.

예상하지 못한 예외는 500으로 응답하되 `ex.getMessage()`를 그대로 공개하지 않는다. 상세 원인과 stack trace는 서버 로그에 남기고 클라이언트에는 일반 메시지만 반환한다.

### 단위 테스트와 HTTP 테스트의 경계

Service 단위 테스트는 Spring 없이 저장 중복과 도메인 예외를 빠르게 확인한다. 하지만 URI 매핑, `@Positive`, `@RestControllerAdvice`, JSON 직렬화는 지나지 않는다.

그래서 MockMvc로 실제 MVC 경계의 상태 코드와 본문을 함께 고정했다.

```java
mockMvc.perform(post("/reservations/cancel/{id}", 0L))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0]")
                .value("예약 번호는 1 이상이어야 합니다"));

mockMvc.perform(post("/reservations/cancel/{id}", 999_999L))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error")
                .value("예약을 찾을 수 없습니다. (id: 999999)"));
```

### 오늘 확인한 것

| 검증 층 | 확인한 동작 | 결과 |
|---|---|---|
| Spring context | 기동, Singleton Service 참조 동일성 | 2개 통과 |
| 단위 테스트 | 저장 중복 방지, 없는 ID, 404 변환, 500 정보 비노출 | 4개 통과 |
| MockMvc | 예약 200, DTO 400, 없는 예약 404, 경로 ID 400 | 4개 통과 |

전체 10개 테스트가 깨끗한 빌드에서 통과했다. 수정 전 코드를 대상으로 실패 테스트를 실행한 기록은 없으므로 TDD의 red-green 순서를 수행했다고 주장하지 않는다.

구현과 검증 근거는 [commit `6c88dcb`](https://github.com/enderpawar/8week_Spring_Study/commit/6c88dcb)에 남겼다.

## 3. 스스로 답한 질문

### Q. Singleton Bean의 `==` 결과를 왜 잘못 설명했나?

처음에는 “객체 상으로는 동일해 보이지만 값이 다르기 때문”이라고 설명했다. 참조 비교와 값 비교가 한 문장에 섞여 있었다.

교정된 답은 값과 무관하다. 같은 ApplicationContext가 기본 Singleton scope의 동일한 `ReservationService` 인스턴스를 반환하므로 두 참조가 같다.

앞으로는 연산자를 고르기 전에 비교 목적을 말한다. Singleton Bean 테스트는 참조 동일성, 예약 ID 검색은 값 동일성을 확인한다.

### Q. `id` 없이도 예약을 수정할 수 있지 않나?

이름이나 방 번호로 조건 검색은 가능하다. 하지만 중복되면 기존 대상 하나를 특정할 수 없다. PK는 단순히 검색을 가능하게 하는 값이 아니라 단건 갱신 계약을 성립시키는 식별자다.

이 교정을 저장소에 적용하면서 ID 없음은 신규 추가, 기존 ID는 같은 위치 교체라는 분기를 만들었다.

### Q. 객체 상태는 바뀌었는데 왜 `save()`에서 중복됐나?

`findById()`가 반환한 객체는 목록 안 객체와 같은 참조이므로 `cancel()` 순간 상태는 이미 바뀐다. 문제는 상태가 아니라 목록 구조였다.

기존 `save()`의 `store.add()`가 같은 참조를 한 칸 더 넣었다. 저장 메서드를 검토할 때는 이름이 아니라 신규·기존 분기와 실제 자료구조 연산을 확인해야 한다.

### Q. `Optional`로 바꾸면 404 처리가 끝난 것인가?

`Optional`은 값 부재만 표현한다. Service가 도메인 예외로 의미를 붙이고 웹 계층이 404로 변환해야 클라이언트 계약이 완성된다.

Repository는 “값이 있는가”, Service는 “업무상 어떤 실패인가”, Web은 “어떤 상태와 본문을 반환하는가”를 각각 결정한다.

## 4. 정리하며

Day06의 `==`와 `.equals()` 교정은 자바 문법 문제로 끝나지 않았다. 같은 객체인지와 같은 값인지 구분한 뒤에야 Singleton Bean과 예약 ID 검색이 서로 다른 비교를 쓰는 이유가 선명해졌다.

그 식별 기준은 Day07의 저장 계약으로 이어졌다. ID가 없으면 추가하고 기존 ID면 교체한다. 값이 없으면 `Optional`로 드러내고, Service와 웹 계층을 지나며 도메인 예외와 HTTP 404로 변환한다.

결국 이번 이틀의 중심 질문은 하나였다.

> 시스템이 “같은 대상”과 “없는 대상”을 어떤 기준으로 판정하고, 그 결과를 다음 계층에 어떻게 전달할 것인가?

남은 한계는 공통 오류 코드·타임스탬프·요청 식별자가 없다는 점이다. 이는 나중에 고칠 것(Week D D5 또는 Week E D1)으로 남겼다. 메모리 저장소가 재시작 후 사라지는 문제는 Week B D3에서 실제 영속성으로 교체하며 해결한다.
