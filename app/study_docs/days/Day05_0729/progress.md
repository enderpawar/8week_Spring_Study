# Day05 (7/29 계획→8/1 완료, Week A D5) — IoC·DI·생성자 주입

## 완료한 범위

- 현재 코드의 `Repository → Service → Controller` 생성자 주입 흐름 관찰
- IoC와 DI 구분, DIP와 구현체 교체 영향 연결
- `@Repository` 제거 전 예측 후 실제 컨텍스트 실패 관찰
- 생성자 대입문 완성 → Controller 생성자 전체 작성 → Service 생성자 독립 작성
- 컴파일 오류 세 단계(`return type required`, `cannot find symbol`, `incompatible types`)를 learner가 순서대로 교정
- 같은 `ReservationService` Bean 두 번 조회 후 `assertSame`으로 Singleton 동일성 확인
- Singleton Service에서 요청별 값은 지역변수로 두어야 하는 이유를 경쟁 상태와 연결

## 검증 근거

| 검증 | 실제 결과 |
|---|---|
| 시작 상태 `./gradlew test` | `BUILD SUCCESSFUL` |
| `@Repository` 제거 실험 | `compileJava` 성공, `contextLoads()` 실패, 원인 `NoSuchBeanDefinitionException` |
| 애노테이션 복구 후 테스트 | `BUILD SUCCESSFUL` |
| `reservationServiceBeanIsSingleton()` | `assertSame(first, second)` 통과 |
| 독립 생성자 작성 오류 교정 후 전체 테스트 | `BUILD SUCCESSFUL` |

## 오답 및 재시험

- IoC와 DI 최초 독립 설명 실패 → 설명 후 빈칸 교정 성공 → 8/2 재시험
- Singleton Bean 참조 비교를 `false`로 예측 → 실제 동일 인스턴스 확인 → 8/2 재시험
- Service 생성자 독립 작성에서 클래스명·필드명·타입 오류 → 컴파일러 메시지로 교정 → 8/2 재시험
- `Long ==`는 복습에서 힌트 후 교정 → 기존 복습 큐대로 8/2 재시험

## 현재 코드의 범위

- `InMemoryReservationRepository`의 `ArrayList`는 변경 가능한 저장 상태를 의도적으로 가진다. 동시성 안전성을 검증하지 않았고, 이번 Day의 "Service 무상태"와 같은 뜻으로 일반화하지 않는다.
- Day04에서 남긴 `save()` 중복 추가 가능성은 아직 해결하지 않았다. Week B의 영속성·변경 감지 학습까지 예약된 부채로 유지한다.

## 다음 시작점

- Week A D6 누적시험 A
- 시작 전 8/2 오답 재시험: `Long ==`, IoC/DI, Singleton 동일성, 생성자 구조
