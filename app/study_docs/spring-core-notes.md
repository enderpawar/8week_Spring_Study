# Spring Core 원리 노트 (Week 0)

> Week A~C 학습 중 현재 저장소에서 직접 재현한 뒤 이 워크시트를 채운다.
> 실험으로 재현한 뒤, **결과를 본인 문장으로** 여기에 남긴다.
> AI가 대신 채우지 않는다. 각 답에는 근거가 되는 테스트/코드 위치를 함께 적는다.
> 면접에서 쓸 만한 문장은 `interview-notes.md`로 옮긴다.

---

## Q1. 생성자 주입을 쓰는 이유 세 가지는?

- 내 답변: [직접 작성]
- 코드 근거: (예 — 실험 1 `DiAndOcpExperimentTest`, `app`의 생성자 주입 클래스)
- 확인한 것: [직접 작성]

## Q2. Singleton Bean에 변경 가능한 상태를 두면 왜 위험한가?

- 내 답변: [직접 작성]
- 코드 근거: 실험 3 `SingletonStateRaceExperimentTest` (최종값이 기대보다 작아진 결과)
- 재현 결과 수치: [직접 작성]

## Q3. `@Transactional`이 Proxy로 동작한다는 것은 무슨 뜻인가?

- 내 답변: [직접 작성]
- 코드 근거: 실험 4 `ProxyExistenceExperimentTest` (`getClass().getName()` 출력값)
- 관찰한 클래스 이름: [직접 작성]

## Q4. 같은 클래스 내부 호출에서 `@Transactional`이 적용되지 않을 수 있는 이유는?

- 내 답변: [직접 작성]  ← Bridge 실험 8(Self-invocation)에서 재현 후 채운다
- 코드 근거: (Bridge `experiments`의 Self-invocation 테스트)

## Q5. 현재 `ReservationRepository`와 `InMemoryReservationRepository`는 OCP/DIP와 어떤 관계인가?

- 내 답변: [직접 작성]
- 코드 근거: `src/main/java/com/example/studyroom/repository/ReservationRepository.java` + `InMemoryReservationRepository.java`
- 확인한 것: [직접 작성]

## Q6. Security Filter Chain과 Spring AOP Proxy는 어떻게 다른가?

- 내 답변: [직접 작성]
- 코드 근거: 실험 6 `FilterVsAopOrderExperimentTest` (실행 순서 로그)
- 관찰한 실행 순서: [직접 작성]

---

## 실험 로그 (선택)

| 실험 | 통과 | 한 줄 결론(본인 문장) |
|---|---|---|
| 1 DI/OCP | ☐ | |
| 2 Singleton == | ☐ | |
| 3 상태 공유 위험 | ☐ | |
| 4 Proxy 존재 | ☐ | |
| 5 Component Scan 범위 | ☐ | |
| 6 Filter vs AOP | ☐ | |
