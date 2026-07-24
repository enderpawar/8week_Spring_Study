package com.example.studyroom.experiments;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 실험 2 — Singleton Scope
 *
 * 확인할 동작: 같은 타입의 Bean을 컨테이너에서 두 번 꺼내면 동일한 인스턴스다(== 참).
 * 즉 Spring은 기본적으로 Bean을 요청마다 새로 만들지 않고, 하나만 만들어 공유한다.
 *
 * ── 이 파일은 "모델 예제"입니다 ──────────────────────────────
 * 나머지 실험(1, 3, 4, 5, 6)은 experiments/README.md 안내를 보고
 * 이 형식(동작 하나 = 그것을 증명하는 테스트 하나)을 따라 직접 작성하세요.
 * 테스트 메서드 이름이 곧 학습 내용이 되도록 씁니다.
 * 결과 해석은 study_docs/spring-core-notes.md 에 본인 문장으로 남깁니다.
 * ────────────────────────────────────────────────────────────
 */
@SpringBootTest
@Import(SingletonScopeExperimentTest.DemoConfig.class)
class SingletonScopeExperimentTest {

    @Autowired
    ApplicationContext ctx;

    @Test
    void 같은_타입_Bean을_두_번_꺼내면_동일_인스턴스다() {
        Counter first = ctx.getBean(Counter.class);
        Counter second = ctx.getBean(Counter.class);

        // isSameAs = 참조 비교(==). 값이 같은지가 아니라 "같은 객체"인지를 본다.
        assertThat(first).isSameAs(second);
    }

    @Test
    void 한_참조로_바꾼_상태가_다른_참조에도_보인다() {
        // Singleton은 공유되므로, 한 곳에서 바꾼 값이 다른 곳에서도 보인다.
        // 실험 3(상태 공유 위험)의 출발점: 이 "공유"가 멀티스레드에서 왜 깨지는지 이어서 확인한다.
        Counter a = ctx.getBean(Counter.class);
        Counter b = ctx.getBean(Counter.class);

        a.set(42);

        assertThat(b.get()).isEqualTo(42);
    }

    @TestConfiguration
    static class DemoConfig {
        @Bean
        Counter counter() {
            return new Counter();
        }
    }

    /** 상태를 가진 단순 Bean. 실험 3에서 이 상태가 멀티스레드에 노출될 때의 위험을 다룬다. */
    static class Counter {
        private int value;

        void set(int v) {
            this.value = v;
        }

        int get() {
            return value;
        }
    }
}
