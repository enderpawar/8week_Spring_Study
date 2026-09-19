# 학습 세션 재개 상태

최종 갱신: 2026-09-19

## 세션 목적

- 한 달 휴지기 후 Week C에 들어가기 전에 Week A·B를 처음부터 다시 복습한다.
- 기억하고 있다고 가정하지 않고 `개념 설명 → 현재 코드의 완성 예제 → 작은 확인 문제 → 즉시 교정` 순서로 진행한다.
- 실제 오답과 교정 결과는 `app/study_docs/reviews/2026-09-19-week-ab-refresh.md`에 누적한다.

## 현재 위치

- Week A D1~D5 재복습: 완료
- Week A 마무리 연결 문제: 통과
- Week B D1 Flyway·DB 제약 재복습: 완료
- Week B D2 순수 JDBC: 진행 중
- Week C: 아직 시작하지 않음

이 기록은 재복습 세션의 상태다. `app/study_docs/FUNDAMENTALS_ROADMAP.md`의 공식 완료 체크 상태는 변경하지 않았다.

## 오늘 다시 연결한 내용

### Week A — 웹 계층

- 정상 반환한 Controller 응답은 기본적으로 `200 OK`이며, `ResponseEntity`로 상태코드와 본문을 명시할 수 있다.
- DTO는 경계를 넘는 데이터 형식이고, Domain은 상태와 상태 변경 규칙을 가진다.
- Java `record` 접근자는 `getRoomName()`이 아니라 `roomName()`이다.
- `@NotBlank`가 제약을 선언하고 `@Valid`가 요청 DTO 검증을 실행한다.
- 검증 실패 시 Service를 호출하지 않고 전역 예외 처리기가 400 응답으로 변환한다.
- Controller는 HTTP, Service는 업무 흐름, Domain은 상태 변경, Repository는 저장·조회를 담당한다.
- IoC는 객체 조립 제어권을 Spring이 갖는 것이고, DI는 의존 객체를 외부에서 전달받는 것이다.
- 생성자 DI를 사용하면 Repository 구현을 JPA·메모리·mock으로 교체하기 쉽다.
- 기본 Singleton Bean은 여러 요청이 공유하므로 Service의 요청별 값은 필드가 아니라 매개변수·지역변수에 둔다.

### Week B D1 — Flyway·DB 제약

- Flyway는 버전 SQL을 순서대로 적용하고 이력과 체크섬을 DB 장부에 저장한다.
- 이미 적용된 마이그레이션은 수정하지 않고 다음 버전 파일을 추가한다.
- `NOT NULL`은 `NULL`만 막고 빈 문자열 `''`은 막지 못한다.
- 앱의 `@NotBlank`는 빠르고 친절한 요청 검증, DB 제약은 모든 저장 경로에 대한 무결성 방어다.
- PK는 연속성이 아니라 유일성을 보장하므로 실패·롤백으로 ID가 건너뛸 수 있다.

### Week B D2 — 순수 JDBC 도입부

- `DataSource`에서 Connection을 빌리고, `PreparedStatement`의 `?`에 값을 바인딩하고, `ResultSet`을 읽은 뒤 자원을 반납한다.
- JPA를 사용해도 커넥션 풀은 사라지지 않는다.
- `ResultSet.next()`는 다음 행으로 커서를 이동하고, `getXxx()`는 현재 행의 값을 읽는다.

## 실제 오답과 교정

1. 실제 ID 조회 책임을 Service라고 답했다.
   - 교정: 실제 저장·조회는 Repository, 업무 순서 조율은 Service다.
2. 조회 결과가 없을 때 `ReservationNotFoundException`을 발생시키는 책임을 Repository라고 답했다.
   - 교정: Repository는 `Optional.empty()`를 반환하고, Service가 이를 비즈니스 예외로 변환한다.
3. 위 두 항목은 교정 문제에서 올바르게 구분해 통과했다.

세부 최초 답변과 교정 기록은 `app/study_docs/reviews/2026-09-19-week-ab-refresh.md`에 있다.

## 다음 기기에서 시작할 지점

로드맵과 이 파일을 읽은 뒤 아래 문제부터 재개한다.

> 여러 행을 읽는 `while (rs.next())` 안에서 `mapRow(rs)`를 호출한다. 만약 `mapRow(rs)` 내부에서도 다시 `rs.next()`를 호출하면 어떤 문제가 생기는가?

이 문제를 교정한 뒤 Week B D2의 아래 내용을 이어간다.

1. `try-with-resources`와 커넥션 누수
2. JDBC가 직접 수행하는 SQL·파라미터 바인딩·행→객체 매핑
3. JPA / Hibernate / Spring Data JPA의 차이
4. JDBC 대비 JPA가 추상화하는 것

그다음 Week B D3 Entity 매핑 → D4 영속성 컨텍스트·1차 캐시 → D5 변경 감지 순서로 복습한다.

## 검증 상태

- 이번 세션에서는 코드를 수정하지 않았다.
- 빌드와 테스트는 실행하지 않았다.
- 공식 로드맵 체크박스와 복습큐는 변경하지 않았다.

