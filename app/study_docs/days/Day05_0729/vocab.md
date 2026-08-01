# Day05 (7/29 계획→8/1 완료, Week A D5) 단어장 — IoC·DI·생성자 주입

형식: **용어 | 한줄뜻 | CS 연결 | 코드 모습 | 연관 단어**

| 용어 | 한줄뜻 | CS 연결 | 코드 모습 | 연관 단어 |
|---|---|---|---|---|
| IoC (Inversion of Control, 제어의 역전) | 객체를 생성하고 연결하는 제어권을 애플리케이션 코드가 아니라 Spring 컨테이너가 갖는 것 | 의존성 그래프의 조립 책임을 외부 런타임으로 이동 | Spring이 Repository→Service→Controller 순서로 Bean을 조립 | DI, ApplicationContext |
| DI (Dependency Injection, 의존성 주입) | 객체가 필요한 의존성을 직접 만들지 않고 외부에서 전달받는 것 | 결합도 감소, 대체 가능성 | `ReservationService(ReservationRepository repository)` | 생성자 주입, DIP |
| Bean | Spring 컨테이너가 생성·보관·연결하는 객체 | 런타임 객체 그래프의 노드 | `@Service`, `@Repository`, `@RestController` | Component Scan |
| 생성자 주입 | 필요한 객체를 생성자 매개변수로 받는 DI 방식 | 필수 의존성을 타입으로 명시하고 초기화 이후 변경을 제한 | `this.reservationRepository = reservationRepository;` | `final`, DI |
| DIP (의존성 역전 원칙) | 상위 정책이 구체 구현이 아니라 추상화에 의존하는 원칙 | 구현 세부사항 변경의 전파를 줄임 | Service가 `InMemoryReservationRepository`가 아니라 `ReservationRepository`에 의존 | OCP, 인터페이스 |
| Singleton Bean | 기본적으로 ApplicationContext 하나에서 하나만 생성되는 Bean | 여러 스레드가 같은 객체를 공유 | 같은 타입을 두 번 `getBean()`하면 같은 참조 | Scope, 동일성 |
| 무상태(stateless) Service | 요청마다 달라지는 값을 공유 필드에 저장하지 않는 Service | 스레드 공유 메모리와 경쟁 상태 방지 | `requesterName`을 메서드 매개변수·지역변수로 사용 | race condition, 동시성 |
