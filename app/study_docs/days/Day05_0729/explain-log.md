# Day05 (7/29 계획→8/1 완료, Week A D5) 예측→실행→차이 설명

형식: 개념 | 예측 | 실제 결과 | 차이 설명

| 개념 | 예측 | 실제 결과 | 차이 설명 |
|---|---|---|---|
| `@Repository` 제거 | 애플리케이션 실행 단계에서 실패할 것. 다만 "저장소 인터페이스 역할을 못한다"고 설명 | `compileJava` 성공 후 `contextLoads()` 실패. 원인 체인에 `NoSuchBeanDefinitionException` 확인 | 클래스의 인터페이스 구현 관계는 유지된다. 사라진 것은 Java 역할이 아니라 Spring Bean 등록이며, Service 생성자에 주입할 객체가 없어 컨텍스트 조립이 실패했다 |
| 같은 `ReservationService` Bean을 두 번 조회한 뒤 `==` 비교 | `==`는 참조 비교이므로 `false`일 것 | `reservationServiceBeanIsSingleton()`의 `assertSame(first, second)` 통과 | 참조 비교 원리는 맞지만 두 조회가 실제로 같은 Singleton Bean 인스턴스를 반환했다. 서로 다른 `Long` 객체 비교와 대상 조건이 달랐다 |
| `ReservationService` 생성자 주입 독립 작성 | 별도 실행 전 예측 없이 작성 | ① 생성자 이름 불일치로 `return type required` ② 없는 필드로 `cannot find symbol` ③ `ReservationService`를 Repository 필드에 대입해 `incompatible types` ④ 모두 교정 후 전체 테스트 통과 | Controller의 생성자 모양을 복사하면서 클래스명·필드명·의존 타입을 현재 Service에 맞게 바꾸지 않았다. 생성자는 클래스 이름, 실제 필드, 필요한 의존 타입을 함께 맞춰야 한다 |
| Singleton Service의 요청별 변경 상태 | "요청에 혼선이 생길 수 있다" | 코드를 추가해 race를 재현하지는 않았고, 공유 인스턴스의 동시 요청 실행 순서를 설명 | 요청별 값은 여러 스레드가 공유하는 필드가 아니라 호출별 매개변수·지역변수에 둬야 한다. race 수치 재현은 하지 않았으므로 측정값은 기록하지 않는다 |
