# Day12 (8/22, Week B D5) 예측→실행→차이 기록

주제: 변경 감지(Dirty Checking) · flush 시점

## 실험 — `save()` 없이 관리 중인 Entity의 필드만 바꾸면 DB에 반영되는가

**조건:** `confirmed=true`인 예약을 저장해 DB에 반영한 뒤, 같은 트랜잭션에서 다시 조회해 관리 상태로 만들고, `managed.cancel()`로 필드만 바꾼다. `repository.save()`는 호출하지 않는다. 그 뒤 `entityManager.flush()`를 호출한다.

**예측(학습자):** "커밋될 것 같아요, dirty checking 때문에" — 트랜잭션이 끝나면 변경이 반영된다.

**실행 결과:** ✅ 일치. Hibernate 로그:

```sql
insert into reservation (confirmed, requester_name, room_name, id) values (?, ?, ?, default)
update reservation set confirmed=?, requester_name=?, room_name=? where id=?
select r1_0.id, r1_0.confirmed, r1_0.requester_name, r1_0.room_name from reservation r1_0 where r1_0.id=?
```

코드 어디에도 `repository.save(managed)`나 명시적 `UPDATE` 호출이 없는데 `update` SQL이 나갔다. `clear()` 후 재조회한 `assertFalse(reloaded.isConfirmed())`도 통과했다.

**판정:** ✅ 정확히 일치. 다만 "dirty checking 때문"이라는 답변은 용어 수준이었고, "언제(`flush()` 시점) · 무엇을 비교해서(로드 시점 스냅샷 vs 현재 값)"까지는 처음엔 답하지 못해(`ㄱㄱ`로 건너뜀) 실행 결과를 먼저 보고 사후에 설명을 재구성했다.

근거: `src/test/java/com/example/studyroom/repository/JpaReservationRepositoryTest.java:82-109`

## 시행착오 — 처음 두 번의 시도는 왜 "아무것도 증명 못 하는 테스트"였는가

1차 시도: `confirm()` 없이 저장(로드 스냅샷 `false`) → `cancel()`(→`false`) → `assertFalse` 항상 통과, 변경 자체가 없어 실험이 무의미했다.
2차 시도: 로드 후 `confirm()`(→`true`) → 곧바로 `cancel()`(→`false`) → 로드 스냅샷(`false`)과 최종 값(`false`)이 같아져 역시 무의미했다.
3차 시도(최종): `confirm()`을 최초 저장 **이전**으로 옮겨 로드 스냅샷을 `true`로 만들고, 트랜잭션 안에서는 `cancel()` 한 번만 남겨 로드 스냅샷과 최종값이 실제로 달라지게 했다. 이때 비로소 `UPDATE`가 관찰됐다.

이 시행착오 자체가 변경 감지의 핵심을 보여준다 — **Hibernate는 "지금 값이 뭔가"가 아니라 "로드 시점 값과 지금 값이 다른가"를 본다.** 중간에 값이 왔다갔다 해도 최종값이 로드 시점과 같으면 dirty로 판정되지 않을 수 있다.

## 오늘 사용한 명령/도구

`./gradlew test --tests "...JpaReservationRepositoryTest" -i` 로 Hibernate SQL 로그를 콘솔에서 직접 확인했다. `build/reports/tests/test/index.html`에도 같은 로그가 테스트별로 남는다(브라우저 확장 도구는 `file://` 경로를 열 수 없어 터미널 로그로 대체 확인했다).

## 다음 학습 시작점

Day13(D6): 누적시험 A+B. Day14(D7): 버퍼 / `ddl-auto: validate` 마무리 + Week B 패턴 승격(`CODE_PATTERNS.md`/`PATTERN_DRILLS.md`).

## [직접 작성] 오늘 배운 것을 내 문장으로

- `save()`가 "저장"의 유일한 방법이 아니라는 게 왜 처음엔 낯설게 느껴졌는지:
- 오늘 세 번 실험을 다시 짜야 했던 이유를 내 말로:
