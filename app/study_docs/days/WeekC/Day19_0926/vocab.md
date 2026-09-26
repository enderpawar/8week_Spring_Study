# Day19 (9/26, Week C D5) 용어

주제: N+1 문제 + fetch join

| 용어 | 한줄뜻 | 오늘 코드와 관찰 |
|---|---|---|
| N+1 문제 | 연관 필드가 LAZY인 목록을 조회한 뒤 각 항목의 연관 필드에 접근하면, 목록 조회 1번 + 항목마다 연관 조회 N번 = 총 N+1번의 쿼리가 발생하는 문제 | `findAllTriggersNPlusOneSelects()` — reservation 3건 조회(1) + member 개별 조회 3번(3) = 4번 |
| fetch join | JPQL에서 `join fetch`로 연관 엔티티까지 하나의 SQL JOIN으로 함께 가져오는 것 | `@Query("select r from Reservation r join fetch r.member")` |
| `@Query` | Spring Data JPA 메서드에 직접 JPQL을 지정하는 애노테이션 — **메서드에만** 붙일 수 있다 | `SpringDataReservationRepository.findAllWithMember()` 위에 적용 |
| default 메서드 | 인터페이스에 구현 몸체를 가진 메서드를 선언하는 것 — 기존 구현체를 안 건드리고 인터페이스를 확장하는 수단 | `ReservationRepository.findAllWithMember()`를 `default`로 선언해 `InMemoryReservationRepository`·`JdbcReservationRepository` 무변경으로 컴파일 유지 |
| 인터페이스 메서드 추가의 전파 | 인터페이스에 추상 메서드를 추가하면 그 인터페이스를 구현하는 **모든** 클래스가 컴파일 에러 대상이 된다 | `ReservationRepository`에 추상 메서드 추가 → `InMemoryReservationRepository`·`JdbcReservationRepository` 컴파일 실패(대조군 코드인데도) |
| fail fast | 지원하지 않는 동작을 조용히 우회시키지 않고 즉시 예외로 실패시켜, 잘못된 연결/오용을 바로 드러내는 설계 원칙 | default 몸체를 `throw new UnsupportedOperationException()`으로 선택(폴백 `return findAll()` 대신) |

## 핵심 대조 — LAZY(N+1) vs fetch join

| 구분 | LAZY(`findAll()`) | fetch join(`findAllWithMember()`) |
|---|---|---|
| SQL 개수 | 1(목록) + N(연관, 접근 시점마다) | 1(JOIN으로 한 번에) |
| member 초기화 시점 | 순회 중 getter 호출 시 | 조회 시점에 이미 완료 |
| 오늘 실측(3건 기준) | 1 + 3 = 4번 | 1번 |

## CS 연결

N+1은 DB 처리 속도가 아니라 **애플리케이션↔DB 간 왕복(round trip) 횟수** 문제다. 루프 안에서 원격 호출을 매번 하는 안티패턴과 같은 구조 — 인덱스가 있어도 왕복 자체의 고정 비용은 줄지 않는다. JOIN은 DB 엔진이 하나의 실행계획으로 왕복을 1번으로 묶는 장치.
