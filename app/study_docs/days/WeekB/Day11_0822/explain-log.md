# Day11 (8/22, Week B D4) 예측→실행→차이 기록

주제: 영속성 컨텍스트 · 1차 캐시 · 동일성

## 실험 1 — `save()` 직후 같은 id를 2회 조회하면 SELECT가 몇 번인가

**조건:** `repository.save()`로 새 예약을 저장하고 `flush()`한 뒤, `clear()` 없이 같은 id로 `findById()`를 연달아 2회 호출한다.

**예측(학습자):** SELECT 1번, `first == second`는 `true`.

**실행 결과:** SELECT **0번**. Hibernate 로그에 `insert`만 찍히고 `select`는 한 줄도 없었다. `assertSame(first, second)`는 통과(`true`).

**판정:** SELECT 횟수 예측은 빗나갔다(1번 예측 → 실제 0번). `first == second`(동일성) 예측은 맞았다.

**설명:** `save()`가 반환한 `saved` 객체는 그 시점부터 이미 영속성 컨텍스트의 1차 캐시에 올라가 있다. 그래서 **첫 번째** `findById()`부터 이미 캐시에 있는 상태였고, 두 번의 조회 모두 SQL 없이 캐시에서 바로 반환됐다.

근거: `src/test/java/com/example/studyroom/repository/JpaReservationRepositoryTest.java:64-80`

## 실험 2 — `clear()`로 캐시를 비운 뒤 같은 id를 2회 조회하면

**조건:** 실험 1과 동일하되, `flush()` 뒤 두 번의 `findById()` 이전에 `entityManager.clear()`를 추가한다.

**예측(학습자):** SELECT는 첫 번째 조회에서 1번, 두 번째 조회는 추가 SQL 없음. `first == second`는 여전히 `true`.

**실행 결과:** 예측과 일치. Hibernate 로그:

```sql
insert into reservation (confirmed, requester_name, room_name, id) values (?, ?, ?, default)
select r1_0.id, r1_0.confirmed, r1_0.requester_name, r1_0.room_name from reservation r1_0 where r1_0.id=?
```

`select`가 정확히 1번만 찍혔다(두 번의 `findById()`를 합쳐서). `assertSame` 통과.

**판정:** ✅ 정확히 일치.

**설명:** `clear()`는 영속성 컨텍스트(1차 캐시)를 통째로 비운다. 그래서 그 다음 첫 번째 `findById()`는 캐시에 아무것도 없으니 DB에 SELECT를 날려야 했고, 그 SELECT로 다시 캐시에 올라온 Entity를 두 번째 `findById()`가 추가 쿼리 없이 그대로 돌려줬다.

근거: `src/test/java/com/example/studyroom/repository/JpaReservationRepositoryTest.java:64-80`(같은 테스트 메서드에 `entityManager.clear();` 한 줄 추가 후 재실행)

## 오늘 아침 인출(복습큐 오답재시험 5건) 결과

| # | 개념 | 이전 상태 | 오늘 판정 |
|---|---|---|---|
| 1 | `final`은 DB 행과 무관, JVM 변수 재대입만 막음 | 8/6 2회 오답 | ✅ 통과 |
| 2 | 싱글톤 Service의 인스턴스 필드는 동시 요청에서 race condition | 8/6 오답("반대 이해") | ✅ 통과(직접 파고들어 "덮어써서 섞인다"까지 설명) |
| 3 | 같은 객체 2회 `save()` — `assignId`가 객체 자체를 바꾸므로 결과는 1개 | 8/6 오답(2로 예측) | ✅ 통과(처음엔 다시 2로 답했으나 유도 질문 후 스스로 교정) |
| 4 | 예상 밖 예외 상세 메시지는 로그에, 클라이언트엔 일반 메시지만 | 8/6 오답(미답) | ✅ 통과(힌트 후) |
| 5 | Entity `final` 필드 + 빈 기본 생성자는 **컴파일 자체가 안 됨**(Hibernate 런타임 문제 아님) | 8/10 오답("된다") | ⚠️ 재확인 필요 — "불변이라 못 다룬다"는 방향은 맞았으나 정확한 메커니즘(컴파일러가 초기화 안 된 생성자 경로를 거부)까지는 스스로 도달하지 못함. 복습큐 +2로 재등록 |

## 다음 학습 시작점

Day12: 변경 감지(dirty checking) · flush 시점. `save()` 없이 관리 중인 Entity의 필드만 바꿔도 DB에 반영되는지 확인한다.

## [직접 작성] 오늘 배운 것을 내 문장으로

- 1차 캐시가 "값이 같아서"가 아니라 "같은 트랜잭션·같은 id라서" 같은 참조를 돌려준다는 게 왜 중요한지:
- `clear()`를 실무에서 언제 써야 할지 (또는 왜 함부로 쓰면 안 되는지):
