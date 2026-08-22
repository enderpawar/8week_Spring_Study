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

## ④ 주간 독립과제 — 이번 세션에서는 보류

Week B 주제(Flyway·JPA·영속성)에 맞춘 독립과제로 "예약에 `cancel_reason` 컬럼을 처음부터 끝까지 힌트 없이 추가하기"(V3 마이그레이션 + Entity 필드 + 검증)를 제안했으나, 학습자가 오늘은 여기까지 하기로 결정했다. **다음 세션 시작 시 이 독립과제부터 처리한다.**

## 오늘 세션 전체 요약 (D4~D7)

2026-08-21 노트북 데이터 유실로 사라졌던 Week B D4~D7을 하루에 다시 학습했다.

- D4: 1차 캐시가 "값이 같아서"가 아니라 "같은 트랜잭션·같은 id라서" 같은 참조를 돌려준다는 것을, `clear()` 유무에 따른 SELECT 횟수 차이(0번→1번)로 실험 확인.
- D5: `save()` 없이도 관리 중인 Entity의 필드 변경만으로 `flush()` 시점에 자동 `UPDATE`가 나가는 것(dirty checking)을 확인. 실험을 두 번 잘못 설계(로드 스냅샷과 최종값이 우연히 같아지는 구성)했다가 세 번째 시도에서 바로잡았다.
- D6: Week A+B 누적시험 8문항 전부 통과했으나 3·5·6·8번은 힌트가 필요했고, 2번(DTO/Domain 분리 인과관계)과 6번(Flyway 체크섬 vs dirty checking 용어 혼동)은 복습큐에 재등록.
- D7: `ddl-auto: validate` 전환, `CHECK` 제약으로 Day08 부채 해결, 독립과제는 다음 세션으로 이월.

## 다음 학습 시작점

Week C D1 — 트랜잭션 경계 / 커밋·롤백. 단, 이번 세션에서 이월된 Week B 독립과제(`cancel_reason` 컬럼 추가)를 Week C D1 이전에 먼저 처리한다.

## [직접 작성] 오늘 배운 것을 내 문장으로

- 오늘 가장 크게 흔들렸던 개념 하나와, 왜 흔들렸는지:
- 다음에 독립과제를 할 때 스스로에게 줄 힌트 하나:
