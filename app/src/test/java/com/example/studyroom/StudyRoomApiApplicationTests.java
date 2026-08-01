package com.example.studyroom;

import com.example.studyroom.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * 스켈레톤 기동 확인용 최소 테스트.
 * Spring ApplicationContext가 정상적으로 뜨는지만 검증한다.
 * Week A D1부터 여기에 실제 학습 테스트를 쌓아 올린다.
 */
@SpringBootTest
class StudyRoomApiApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
    }

    @Test
    void reservationServiceBeanIsSingleton() {
        ReservationService first = applicationContext.getBean(ReservationService.class);
        ReservationService second = applicationContext.getBean(ReservationService.class);

        assertSame(first, second);
    }
}
