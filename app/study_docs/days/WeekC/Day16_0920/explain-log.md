# Day16 (9/20, Week C D2) 예측→실행→차이 기록

주제: Spring AOP 프록시와 self-invocation

## 실험 1 — ReservationService의 런타임 타입

- 코드: `ApplicationContext`에서 `ReservationService` Bean을 조회
- 예측: Spring 프록시 객체
- 실행: `AopUtils.isAopProxy()`와 `isCglibProxy()` 모두 `true`, 원본 클래스와 런타임 클래스 불일치
- 실제 클래스: `com.example.studyroom.service.ReservationService$$SpringCGLIB$$0`
- 교정: `getBean()`이 임시 객체를 생성하는 것이 아니라, 컨테이너가 Bean 생성 과정에서 준비한 Singleton 프록시를 반환한다.

## 실험 2 — 외부 호출과 self-invocation

- `service.inner()` → `active=true`
- `service.outer()` → `outer()` 내부의 `inner()`에서 `active=false`
- 두 테스트 모두 통과
- 교정: `inner()`가 Bean으로 등록되지 않은 문제가 아니다. `SelfInvocationService` 객체 전체가 Bean이며, target 내부의 `this.inner()`가 앞쪽 프록시를 다시 통과하지 않는 것이 원인이다.

## 실험 3 — 바깥 트랜잭션 경계

- 최초 실행: `transactionalOuter()`에 `@Transactional`을 빠뜨려 `expected true but was false`로 실패
- 수정: `transactionalOuter()`에 `@Transactional` 적용
- 재실행: `active=true`, 세 테스트 모두 통과
- 해석: 내부 `inner()`의 애노테이션은 여전히 적용되지 않는다. 외부 호출이 프록시를 통과하면서 바깥 메서드의 트랜잭션이 먼저 시작됐기 때문에 내부 메서드도 그 안에서 실행된다.

## 관리 공간 구분

- ApplicationContext: `SelfInvocationService`, `ReservationService`, Repository, AOP 프록시 같은 Spring Bean 관리
- 영속성 컨텍스트: JPA로 조회하거나 저장한 `Reservation` 같은 `@Entity` 관리
- `@Transactional`이 임의의 Service 객체를 영속성 컨텍스트에 넣는 것은 아니다.

## 검증 근거

- `src/test/java/com/example/studyroom/StudyRoomApiApplicationTests.java`
- `src/test/java/com/example/studyroom/service/TransactionProxySelfInvocationTest.java`
- 프록시 확인 테스트 1개와 호출 경로 테스트 3개 통과

## [직접 작성] 오늘 배운 것을 내 문장으로

- 애노테이션이 붙어 있어도 self-invocation에서 트랜잭션이 시작되지 않는 이유:
- ApplicationContext와 영속성 컨텍스트의 차이:

## 다음 시작점

Week C D3 — 트랜잭션 전파 `REQUIRED`와 `REQUIRES_NEW`.
