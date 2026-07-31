# Day04 (7/28→7/31 이어서 완료, Week A D4) 단어장 — Service/Repository 책임 분리

형식: **용어 | 한줄뜻 | CS 연결 | 코드 모습 | 연관 단어**

| 용어 | 한줄뜻 | CS 연결 | 코드 모습 | 연관 단어 |
|---|---|---|---|---|
| Service 계층 | 비즈니스 로직(상태 변경 규칙)을 수행하는 계층. Controller와 Repository 사이를 조율 | 관심사 분리(Separation of Concerns) | `reservationService.cancel(id)` | Controller, Repository |
| Repository 인터페이스 | 저장소 접근을 "무엇을 할 수 있는지(What)"만 선언하고 "어떻게(How)"는 구현체에 위임 | 의존성 역전 원칙(DIP) | `interface ReservationRepository { Reservation findById(Long id); }` | InMemoryReservationRepository, DI |
| 의존성 역전 원칙(DIP) | 상위 모듈(Service)이 하위 모듈의 구체 구현이 아니라 추상화(인터페이스)에 의존하는 설계 원칙 | 느슨한 결합(Loose coupling) — 구현체를 바꿔도 상위 코드는 안 바뀜(Week B에서 JPA로 교체 예정) | `ReservationService(ReservationRepository repository)` | 인터페이스, 생성자 주입 |
| 단일 책임 원칙(SRP) | 클래스는 변경 이유를 하나만 가져야 한다는 원칙 — Controller에 HTTP 처리와 비즈니스 로직을 같이 두면 위반 | 응집도(Cohesion) | Controller는 HTTP 입출력만, Service는 로직만 | 관심사 분리, 테스트 용이성 |
| 식별자(PK) | 값이 같아 보여도 서로 다른 레코드를 구별하기 위한 고유 값. 없으면 "찾아서 갱신"이 아니라 "새로 추가"만 가능 | DB의 기본키(Primary Key)와 동일 개념. JPA에선 `@GeneratedValue`가 이 역할 | `private Long id;` | `assignId()`, `findById()` |
| Wrapper 타입 값 비교 | `Long`, `Integer` 같은 객체 타입은 `==`이 아니라 `.equals()`로 비교해야 값이 같은지 알 수 있다 | 참조 동일성(reference equality) vs 값 동일성(value equality) | `r.getId().equals(id)` | `String`을 `==`로 비교하면 안 되는 것과 동일 원리 |
| `@PathVariable` | URL 경로의 일부(`{id}`)를 메서드 파라미터로 매핑 | URI를 통한 리소스 식별(REST) | `@PostMapping("/reservations/cancel/{id}") public String cancel(@PathVariable Long id)` | `@RequestBody`, REST 자원 식별 |
