# Day14 (8/22, Week B D7) 버퍼 기록

주제: `ddl-auto: validate` 전환 + 기술부채 상환 (Week B 마무리)

## ① 미완료 필수 유닛

없음. D1~D6 모두 오늘 완료됨(D1~D3은 8/9, D4~D6은 8/22).

## ② `ddl-auto: none` → `validate`

**예측(학습자):** "Entity 필드랑 실제 DB 컬럼 타입·이름 비교해서 안 맞으면 기동 실패"

**변경:** `application.yml`의 `spring.jpa.hibernate.ddl-auto`를 `none`에서 `validate`로 직접 수정.

**검증:** `./gradlew test` 전체 재실행 → `BUILD SUCCESSFUL`. Entity 매핑이 실제 스키마(V1+V2 적용 후)와 정확히 일치함을 확인. Hibernate가 스키마를 만들거나 고칠 권한이 전혀 없는 상태에서, 불일치만 있으면 기동을 거부하는 감시자 역할로 확정됐다.

근거: `src/main/resources/application.yml:10`

## ③ 기술부채 상환

### 부채 1 — `NOT NULL`이 빈 문자열을 통과시킴 (Day08 발견)

**해결:** `V2__add_name_check_constraints.sql` 신규 작성(V1은 손대지 않음).

```sql
ALTER TABLE reservation
    ADD CONSTRAINT room_name CHECK (room_name <> '');

ALTER TABLE reservation
    ADD CONSTRAINT requester_name CHECK (requester_name <> '');
```

시행착오: 처음엔 `CHECK NOT BLANK`(자바 `@NotBlank` 애노테이션과 SQL 문법을 혼동)를 썼고, 다음엔 `CHECK <> ''`(비교 대상 컬럼 누락)를 썼다. 두 번의 교정 끝에 `CHECK (컬럼 <> '')` 형태로 정리했다.

**검증:** `entityManager.createNativeQuery()`로 Bean Validation을 우회해 `INSERT INTO reservation (room_name, ...) VALUES ('', ...)`를 직접 실행하는 테스트를 추가했다. `assertThrows(PersistenceException.class, ...)` 통과 — Day08에서 뚫려 있던 경로가 이제 DB 수준에서 막힌다.

근거: `src/main/resources/db/migration/V2__add_name_check_constraints.sql`, `src/test/java/com/example/studyroom/repository/JpaReservationRepositoryTest.java:112-121`

### 부채 2 — 메모리 저장소는 재시작 시 데이터 소실 (Day04 발견)

**상태 재확인:** Day10에서 `JpaReservationRepository`가 유일한 `@Repository` Bean으로 전환되며 이미 해결됐던 것을 오늘 확인하고 원장에 반영했다(`InMemoryReservationRepository`의 `@Repository`는 주석 처리 상태).

## ④ 주간 독립과제 — `cancel_reason` 컬럼 추가 (같은 세션 내 이어서 완료)

Week B 주제(Flyway·JPA·영속성)에 맞춘 독립과제로 "예약에 `cancel_reason` 컬럼을 처음부터 끝까지 힌트 없이 추가하기"를 제안했다. 골격 없이 요구사항만 제시했고, 학습자가 스스로 설계·구현했다.

**한 일:**
1. `V3__cancel_reason.sql` — `ALTER TABLE reservation ADD cancel_reason VARCHAR(100);` (NULL 허용 — 미취소 예약은 사유가 없어야 하므로 `NOT NULL`을 쓰지 않기로 직접 판단).
2. `Reservation` Entity에 `cancelReason` 필드 + `getCancelReason()` 추가.
3. `cancel()`의 시그니처를 `cancel(String cancelReason)`으로 변경(오버로드가 아니라 시그니처 자체를 바꾸는 쪽을 선택 — "취소엔 항상 사유가 있어야 한다"는 도메인 판단).
4. 연쇄 수정: `ReservationService.cancel(Long, String)`, `ReservationController.cancel(...)`(+`@RequestParam @NotBlank cancelReason` 쿼리 파라미터), 기존 호출부 4곳(`JpaReservationRepositoryTest` 2곳, `ReservationServiceTest` 2곳), 그리고 API 계약이 바뀐 Controller 테스트 2곳(`ReservationControllerHttpTest`)까지 전부 갱신.
5. 새 통합 테스트 `checkCancelReason()` — 관리 중인 Entity에 사유를 남기고 `save()` 호출 없이 `flush()`만으로 반영되는지, `clear()` 후 재조회로 검증.

**시행착오 (막힌 지점들):**
- SQL: `ALTER TABLE`에 `CREATE TABLE`의 괄호 문법을 반복 적용 → 3차 시도 만에 교정. `NOT NULL` vs `CHECK`까지 고려했다가 "그냥 nullable로 두면 된다"는 더 단순한 답으로 스스로 수렴.
- Entity 메서드: 처음엔 파라미터 없이 자기 자신에게 대입하는 메서드(`this.cancelReason = cancelReason`, 컴파일 불가)를 작성 → 게터/세터 역할 구분 질문 후 교정.
- `cancel()` 시그니처 변경의 파급효과(main 1곳 + test 4곳)를 직접 컴파일 에러로 확인하며 하나씩 추적 수정 — DI/인터페이스 없이도 메서드 시그니처 하나가 호출부 전체에 어떻게 전파되는지 실감.
- `checkCancelReason()` 테스트를 두 번 다시 썼다: 1차는 assert가 없었고, 2차는 D5와 같은 "로드 스냅샷과 최종값이 같아 아무것도 증명 못 하는" 함정(취소 사유를 저장 전과 후에 같은 문자열로 두 번 설정)에 다시 빠졌다가 스스로 서로 다른 문자열로 바꿔 교정.
- Controller: `cancelReason`을 `@PathVariable`로 잘못 선언(URL 템플릿에 자리가 없음) + `@Positive`(숫자용)를 `String`에 오용 → `@RequestParam` + `@NotBlank`로 교정. 그 결과 기존 Controller 테스트 2개가 필수 쿼리 파라미터 누락으로 깨졌고, `.param("cancelReason", ...)`으로 갱신해 해결.

**검증:** 전체 테스트 16개 `BUILD SUCCESSFUL`.

근거: `src/main/resources/db/migration/V3__cancel_reason.sql`, `src/main/java/com/example/studyroom/domain/Reservation.java`, `src/main/java/com/example/studyroom/service/ReservationService.java`, `src/main/java/com/example/studyroom/controller/ReservationController.java`, `src/test/java/com/example/studyroom/repository/JpaReservationRepositoryTest.java:123-141`, `src/test/java/com/example/studyroom/controller/ReservationControllerHttpTest.java:51-66`

## 오늘 세션 전체 요약 (D4~D7)

2026-08-21 노트북 데이터 유실로 사라졌던 Week B D4~D7을 하루에 다시 학습했다.

- D4: 1차 캐시가 "값이 같아서"가 아니라 "같은 트랜잭션·같은 id라서" 같은 참조를 돌려준다는 것을, `clear()` 유무에 따른 SELECT 횟수 차이(0번→1번)로 실험 확인.
- D5: `save()` 없이도 관리 중인 Entity의 필드 변경만으로 `flush()` 시점에 자동 `UPDATE`가 나가는 것(dirty checking)을 확인. 실험을 두 번 잘못 설계(로드 스냅샷과 최종값이 우연히 같아지는 구성)했다가 세 번째 시도에서 바로잡았다.
- D6: Week A+B 누적시험 8문항 전부 통과했으나 3·5·6·8번은 힌트가 필요했고, 2번(DTO/Domain 분리 인과관계)과 6번(Flyway 체크섬 vs dirty checking 용어 혼동)은 복습큐에 재등록.
- D7: `ddl-auto: validate` 전환, `CHECK` 제약으로 Day08 부채 해결, 독립과제는 다음 세션으로 이월.

## 다음 학습 시작점

Week B 독립과제까지 같은 세션에서 완료했다. 남은 건 **Week B 패턴 승격**(`CODE_PATTERNS.md`/`PATTERN_DRILLS.md`, `CLAUDE.md` 필수 절차) 뿐이며, 그 다음이 Week C D1 — 트랜잭션 경계 / 커밋·롤백이다.

## [직접 작성] 오늘 배운 것을 내 문장으로

- 오늘 가장 크게 흔들렸던 개념 하나와, 왜 흔들렸는지:
- 다음에 독립과제를 할 때 스스로에게 줄 힌트 하나:
