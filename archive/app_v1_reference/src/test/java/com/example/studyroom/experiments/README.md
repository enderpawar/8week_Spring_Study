# Week 0 — Spring Core 실험

강의를 보지 않고, **동작을 재현하는 테스트를 직접 써서** Spring Core 원리를 익힌다.
규칙: **동작 하나 = 그것을 증명하는 테스트 하나.** 테스트 이름이 곧 학습 내용이 되게 쓴다.
막히면 `spring-basic.zip`(IoC/DI·Singleton·Scan) / `spring-start-v20260130/7. AOP.pdf`(Proxy)를 **그때만** 편다.

결과 해석은 매 실험마다 `study_docs/spring-core-notes.md`에 **본인 문장으로** 남긴다. (테스트 통과 ≠ 학습 완료)

| # | 파일(직접 생성) | 증명할 것 | 힌트 | 노트 질문 |
|---|---|---|---|---|
| 1 | `DiAndOcpExperimentTest` | `Repository` 구현체를 교체해도 `Service` 코드는 그대로다 → OCP/DIP | `archive/week1`의 `TodoRepository`(인터페이스)+`InMemory` 구현을 **읽고**, 여기 작은 인터페이스 1개 + 구현 2개를 만들어 Service가 인터페이스에만 의존함을 보인다. archive는 수정하지 않는다 | Q1, Q5 |
| 2 | `SingletonScopeExperimentTest` ✅ | 같은 타입 Bean은 하나만 만들어져 공유된다(==) | **모델 예제 — 이미 작성됨.** 형식 참고 | — |
| 3 | `SingletonStateRaceExperimentTest` | 공유 Singleton에 변경 가능한 필드를 두고 여러 스레드가 접근하면 값이 깨진다 | `ExecutorService`로 N개 스레드가 카운터를 동시에 증가 → 최종값이 기대보다 작아지는 것 재현. 실험 2의 `Counter`를 재사용 가능 | Q2 |
| 4 | `ProxyExistenceExperimentTest` | `@Transactional` Bean의 실제 클래스 이름에 CGLIB 표식이 붙는다 | 아무 `@Service`/`@Transactional` Bean을 주입받아 `getClass().getName()`에 `$$SpringCGLIB$$` 포함을 단언. **`@SpringBootTest` 필요** | Q3 |
| 5 | `ComponentScanRangeExperimentTest` | 스캔 대상 밖 패키지의 `@Component`는 Bean으로 등록되지 않는다 | 스캔 루트(`com.example.studyroom`) 밖 패키지에 `@Component`를 두고, `getBean` 시 `NoSuchBeanDefinitionException` 발생을 단언 | — |
| 6 | `FilterVsAopOrderExperimentTest` | Filter와 AOP Advice는 서로 다른 기술이고 실행 순서가 다르다 | Filter와 `@Around` Advice에 순번 로그를 심고, 한 요청에서 찍히는 순서를 단언. "Filter는 Servlet 레벨, AOP는 Bean 메서드 레벨"을 노트에 정리 | Q6 |

> Self-invocation(내부 호출 시 `@Transactional` 미적용)은 여기서 다루지 않고 **Bridge 실험 8번**에서 재현한다(노트 Q4). Week 0에서는 실험 4로 "Proxy가 존재한다"까지만 확인한다.

## 실행

```bash
cd app
./gradlew test --tests "com.example.studyroom.experiments.*"
```

## 완료 판단

6개 실험이 모두 통과하고, 각 실험이 무엇을 증명하는지 `spring-core-notes.md`에 본인 문장으로 적혀 있으면 Week 0 학습 트랙 완료.
