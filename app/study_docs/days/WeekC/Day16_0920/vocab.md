# Day16 (9/20, Week C D2) 용어

주제: Spring AOP 프록시 · self-invocation

| 용어 | 한줄뜻 | 오늘 코드와 관찰 |
|---|---|---|
| 프록시 | 실제 객체 앞에서 호출을 대신 받아 부가 기능을 적용한 뒤 target에 전달하는 대리 객체 | `ReservationService$$SpringCGLIB$$0` |
| target | 프록시 뒤에서 실제 비즈니스 메서드를 실행하는 원본 객체 | 실제 `ReservationService`, 테스트의 `SelfInvocationService` |
| Spring AOP | 메서드 호출 경계에 트랜잭션 같은 공통 부가 기능을 적용하는 Spring 장치 | `@Transactional` 메서드 호출 전후의 시작·commit·rollback |
| CGLIB 프록시 | 원본 클래스를 상속한 동적 하위 클래스로 만든 프록시 | `AopUtils.isCglibProxy(service) == true` |
| self-invocation | 같은 객체의 메서드가 내부에서 자기 메서드를 직접 호출하는 형태 | `outer()`의 `return inner()` |
| 트랜잭션 경계 | 프록시가 트랜잭션을 시작하고 종료하는 외부 메서드 호출 범위 | `transactionalOuter()`에 `@Transactional` 적용 |
| ApplicationContext | Spring Bean과 프록시를 관리하는 IoC 컨테이너 | `applicationContext.getBean(ReservationService.class)` |
| 영속성 컨텍스트 | JPA로 조회·저장한 Entity를 관리하는 공간 | `cancel()`에서 조회한 `Reservation`이 관리 대상 |

## 핵심 호출 경로

```text
외부 inner() 호출
→ 프록시가 @Transactional 확인
→ 트랜잭션 시작
→ target.inner()
→ active=true
```

```text
외부 outer() 호출
→ target.outer()
→ this.inner()로 target 내부 이동
→ 프록시 재진입 없음
→ active=false
```

```text
외부 transactionalOuter() 호출
→ 프록시가 바깥 트랜잭션 시작
→ target.transactionalOuter()
→ this.inner()는 프록시를 우회
→ 이미 시작된 트랜잭션 안에서 active=true
```
