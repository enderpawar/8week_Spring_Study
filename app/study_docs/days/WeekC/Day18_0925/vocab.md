# Day18 (9/25, Week C D4) 용어

주제: 연관관계 매핑(`@ManyToOne`) + Hibernate LAZY 프록시

| 용어 | 한줄뜻 | 오늘 코드와 관찰 |
|---|---|---|
| 연관관계 매핑 | 두 Entity 사이의 관계(FK)를 자바 객체 참조로 표현하는 것 | `Reservation.member`가 `Member`를 직접 참조 |
| `@ManyToOne` | "여럿(N) 대 하나(1)" 관계 — 이 Entity 여러 개가 상대 Entity 하나를 참조 | `Reservation`(N) → `Member`(1) |
| `@JoinColumn(name=...)` | 그 참조를 저장할 실제 FK 컬럼 이름을 지정 | `@JoinColumn(name = "member_id")` |
| `FetchType.LAZY` | 연관 필드를 즉시 조회하지 않고, 실제 접근하는 시점까지 SELECT를 미룸 | `@ManyToOne(fetch = FetchType.LAZY)` |
| `FetchType.EAGER` | 연관 필드를 소유 Entity 조회와 동시에 즉시 SELECT | `@ManyToOne` 기본값(명시 안 하면 EAGER) |
| Hibernate 프록시(`Entity$HibernateProxy`) | 실제 값 대신 들어가는 빈 껍데기 대리 객체. 최초엔 식별자만 갖고, 실제 데이터 접근 시 초기화됨 | `found.getMember().getClass()` → `Member$HibernateProxy` |
| 초기화(initialization) | 프록시가 실제 데이터를 채우기 위해 SELECT를 실행하는 순간 | `getName()` 호출 시점에 `select ... from member` 발생 |
| `mappedBy` | 연관관계의 반대편(inverse side)에서 "FK는 이미 저쪽 필드가 갖고 있다"고 알리는 속성 — 새 컬럼을 만들지 않음 | 오늘 빈칸 예제로 제시만 됨, 아직 미적용(다음 세션에 `Member.reservations`에 적용 예정) |

## 핵심 호출 경로

```text
findById() 재조회
→ reservation 테이블 SELECT (member_id 컬럼값 포함, 이 시점에 프록시 생성)
→ getMember() 호출 — 여전히 프록시, SELECT 없음
→ getMember().getName() 호출 — 이 시점에 member 테이블 SELECT 발생 (초기화)
→ 이후 접근은 채워진 값 재사용
```

## AOP 프록시(Day16)와의 대조

| 구분 | Spring AOP 프록시 | Hibernate LAZY 프록시 |
|---|---|---|
| 가로채는 대상 | Bean의 메서드 호출 | Entity의 필드 접근 |
| 목적 | 트랜잭션 등 부가기능 삽입 | SELECT 지연(지연 평가) |
| 오늘/Day16 확인 클래스명 | `ReservationService$$SpringCGLIB$$0` | `Member$HibernateProxy` |
