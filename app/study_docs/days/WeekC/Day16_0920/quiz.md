# Day16 (9/20, Week C D2) 인출 기록

## 1. 세션 예측과 교정

| # | 질문 | 학습자 답 | 판정 | 교정 |
|---|---|---|---|---|
| P1 | `getBean(ReservationService.class)`의 런타임 객체 | Spring 프록시라고 답함 | ✅(근거 교정) | `getBean()`이 임시 객체를 만드는 것이 아니라, Bean 생성 과정에서 만들어진 Singleton 프록시를 조회함 |
| P2 | 외부 `inner()`와 `outer()` 내부의 `inner()`에서 트랜잭션 활성 상태 | 각각 `true`, `false`라고 예측 | ✅(근거 교정) | `inner()`가 별도 Bean이 아니어서가 아니라 target 내부 호출이 프록시를 재통과하지 않기 때문 |
| P3 | `transactionalOuter()` 내부의 `inner()` 활성 상태 | 바깥에서 이미 트랜잭션을 시작하므로 `true`라고 예측 | ✅ | 내부 `inner()`의 애노테이션이 적용된 것은 아니며 바깥 경계의 트랜잭션 안에서 실행됨 |
| P4 | 트랜잭션이 시작되면 현재 Service 객체가 영속성 컨텍스트에 들어가는가 | 현재 객체가 올라가는지 질문 | 교정 후 ✅ | Service·프록시는 ApplicationContext의 Bean이고, JPA로 조회·저장한 `Reservation` Entity가 영속성 컨텍스트의 관리 대상이라고 답함 |

## 2. 다음 복습 질문

1. `@Transactional` 애노테이션 자체와 Spring AOP 프록시의 역할 차이
2. `getBean()`과 프록시 생성 시점의 구분
3. self-invocation에서 내부 메서드의 `@Transactional`이 적용되지 않는 호출 경로
4. 바깥 트랜잭션 경계가 이미 활성화된 상태에서 내부 호출이 실행되는 과정
5. ApplicationContext와 영속성 컨텍스트의 관리 대상 차이

## 3. 복습 일정

Day16 완료일 9/20 기준 일반 항목은 +2일 9/22, +7일 9/27, +14일 10/4에 인출한다. `getBean()` 생성 시점, Bean과 영속 Entity의 관리 공간 구분은 +1일인 9/21에 먼저 재시험한다.
