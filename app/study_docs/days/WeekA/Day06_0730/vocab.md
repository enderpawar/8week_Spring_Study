# Day06 (7/30 계획→8/2 완료, Week A D6) 단어장 — 누적 인출 A

| 용어 | 한줄뜻 | CS 연결 | 코드 모습 | 연관 단어 |
|---|---|---|---|---|
| 참조 동일성 | 두 변수가 같은 객체를 가리키는지 비교하는 것 | 객체 식별(identity) | Singleton Bean 두 조회의 `==`·`assertSame` | 값 동일성 |
| 값 동일성 | 서로 다른 객체라도 논리적으로 같은 값을 나타내는지 비교하는 것 | 동등 관계 | `storedId.equals(requestedId)` | Wrapper 타입 |
| Singleton scope | Bean 정의 하나당 컨테이너가 같은 인스턴스를 재사용하는 기본 scope | 공유 객체와 동시성 | `ReservationService` | 무상태 Service |
| PK(기본키) | 데이터 하나를 고유하게 식별하는 값 | DB 행 식별 | `Reservation.id` | 갱신, 중복 |
| 요청 매핑 | URI·본문 같은 HTTP 입력을 Controller 인수와 연결하는 처리 | 컴파일과 런타임 경계 | `@PathVariable Long id` | DispatcherServlet |
| 생성자 주입 | 필수 의존성을 생성자 매개변수로 전달받는 방식 | 의존성 그래프 조립 | `ReservationService(ReservationRepository repository)` | IoC, DI |
| DIP | 상위 정책이 구체 구현보다 추상 계약에 의존하는 원칙 | 결합도와 변경 전파 | `ReservationRepository` 필드 | 인터페이스 |
