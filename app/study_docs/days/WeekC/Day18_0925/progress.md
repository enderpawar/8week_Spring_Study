# Day18 (2026-09-25 시작, Week C D4) 진행 기록

> 주제: 연관관계 매핑(`@ManyToOne`) + Hibernate LAZY 프록시
> 상태: **Full 루프 미완료 상태에서 Week C D5(Day19)로 진행 — 2026-09-26 사용자 결정.** Full 루프 6단계 중 ①개념+완성예제, ⑤예측→실행→차이설명 완료. ②빈칸예제(제시만 됨, 미제출) · ③독립 변형 · ④인출(노트 덮고 재작성) · ⑥복습큐 등록(일부만)은 남은 채로 다음 Day로 넘어간다.
>
> **2026-09-26 확인**: `Member.java`(`app/src/main/java/com/example/studyroom/domain/Member.java`)에 역방향 `reservations` 컬렉션 필드가 없음을 코드로 재확인했다 — 빈칸 예제는 미완료 상태 그대로다. 사용자가 "Day18 전부 끝났다"고 판단했으나 산출물(`vocab.md`/`quiz.md`)과 소스 코드 모두 ②③④⑥이 없다는 근거를 재확인 후 사용자에게 제시했고, 사용자는 이를 확인한 뒤에도 Day19 진행을 지시했다. 완료로 표시하지 않고 [기술부채.md](../../../기술부채.md)에 미완료 항목을 등록해 이월한다.

## 1. 완료한 것

| 항목 | 내용 |
|---|---|
| `V4__member.sql` | `member` 테이블 생성 + `reservation.member_id` FK 컬럼·제약 추가 |
| `Member.java` (신규) | `@Entity`, `protected` 기본 생성자, `id`/`name` |
| `Reservation.java` | `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="member_id") private Member member;` 필드 + `assignMember()`/`getMember()` 추가. 기존 필드·생성자·기존 테스트는 무변경 |
| `MemberRepository.java` (신규) | `interface extends JpaRepository<Member, Long>` — Week B에서 이미 검증한 Spring Data 계층을 바로 재사용, 어댑터 2단 분리는 하지 않음(오늘 주제 아님) |
| `MemberLazyProxyTest.java` (신규) | `entityManager.clear()` 후 재조회 → `getMember()`/`getName()` 호출 시점별 SQL 발생 여부 관찰 |

## 2. 겪은 오류 (❌ 흔한 실수 후보 — 다음 주차 마무리 패턴 승격 때 재료로 씀)

1. `member` 필드에 `@ManyToOne`/`@JoinColumn` 애노테이션 자체를 빼먹고 필드만 추가 → `org.hibernate.type.descriptor.java.spi.JdbcTypeRecommendationException`(Hibernate가 `Member`를 일반 컬럼 값으로 착각)
2. `@ManyToOne`을 `@ManyToMany`로 오타 → `org.hibernate.AnnotationException`(`CollectionBinder`가 컬렉션 타입을 기대)

## 3. 예측 기록과 판정 (예측→실행→차이설명)

`entityManager.clear()` 후 `reservationRepository.findById()`로 재조회, `getMember().getClass()` → `getMember().getName()` 순서로 관찰.

- **학습자 예측**: ① reservation 재조회 SELECT는 나가지만 member 테이블 SELECT는 별개(수정 후 정답) ② `getClass()`는 `Member`가 아닐 수도 있다(수정 후 정답) ③ `getName()` 호출 시 SELECT 발생(정답)
- **실제 로그**: `select ... from reservation`(FK값 `member_id` 포함) → `클래스 타입: class ...Member$HibernateProxy` → `select ... from member`(이 시점에야 발생) → `이름: 진우`
- **판정**: 3항목 모두 최종적으로 정답. AOP 프록시(메서드 호출 가로채기)와 Hibernate LAZY 프록시(필드 접근 가로채기)가 다른 장치임을 로그로 직접 확인.

## 4. 세션 시작 인출 워밍업 결과 (9/22 도래분, Day17 self-invocation 관련)

- **self-invocation 용어 자체를 기억 못해 위키 검색** → 오답재시험, 다음 도래 9/26
- **`transactionalOuter()`가 왜 `true`인지 "몰라"로 답변** (바깥 메서드에 경계를 두면 내부 호출도 이미 활성 트랜잭션 안에서 실행된다는 항목) → 오답재시험, 다음 도래 9/26
- **`RuntimeException` 롤백 규칙**: 힌트("프록시가 무슨 기준으로 판단?") 후 "프록시가 RuntimeException 기준으로 판단"까지 도달 → 통과, +7(10/2)

## 5. 다음 세션 시작점

**Day18 이어서 — 빈칸 예제부터.** `Member.java`에 역방향(inverse side, `mappedBy`) 컬렉션을 추가하는 빈칸 4개를 제시한 상태에서 세션을 마쳤다. 학습자가 아직 답하지 않았다.

```java
// Member.java에 추가할 것 (아직 미타이핑)
@______(mappedBy = "______", fetch = FetchType.______)
private List<Reservation> reservations = new ______<>();
```

빈칸 4개(애노테이션 이름/`mappedBy` 값/`fetch` 값/컬렉션 구현체) → 이후 독립 변형 → 인출(노트 덮고 재작성) → 복습큐 등록(연관관계 매핑·LAZY 프록시 개념 자체) 순으로 이어간다.

## 6. 상태 확인

| 시점 | 결과 |
|---|---|
| 세션 시작 | `./gradlew test` green |
| Member 필드 애노테이션 누락 | `compileJava`는 성공, `test` 실패 19/23 (`JdbcTypeRecommendationException`) |
| `@ManyToMany` 오타 교정 전 | `test` 실패 19/23 (`AnnotationException`) |
| 최종 | `./gradlew test` green, 23/23. `MemberLazyProxyTest` 별도 실행으로 SQL 로그 확인 |

## 7. [직접 작성] 오늘 배운 것을 내 문장으로

<!-- 아래는 학습자가 직접 채운다. 비워두지 말 것. -->

- `@ManyToOne`의 fetch 기본값이 `EAGER`인 이유(vs `@OneToMany` 기본값 `LAZY`)를 내 말로:
- AOP 프록시와 Hibernate LAZY 프록시가 "다른 장치"라는 게 정확히 뭐가 다르다는 뜻인지:
- `entityManager.clear()`가 왜 SELECT를 줄이는 게 아니라 늘리는 방향으로 작용하는지:
