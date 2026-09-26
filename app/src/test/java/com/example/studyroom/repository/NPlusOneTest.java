package com.example.studyroom.repository;

import com.example.studyroom.domain.Member;
import com.example.studyroom.domain.Reservation;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
class NPlusOneTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    void findAllTriggersNPlusOneSelects() {
        String[] names = {"진우", "철수", "영희"};
        for (String name : names) {
            Member member = memberRepository.save(new Member(name));
            Reservation reservation = new Reservation("Room-" + name, name);
            reservation.assignMember(member);
            reservationRepository.save(reservation);
        }

        entityManager.flush();
        entityManager.clear();

        System.out.println("===== findAll() 호출 시작 =====");
        List<Reservation> reservations = reservationRepository.findAll();
        System.out.println("===== findAll() 끝, 지금부터 순회 시작 =====");

        for (Reservation r : reservations) {
            System.out.println(r.getMember().getName());
        }
        System.out.println("===== 순회 끝 =====");
    }

    @Test
    @Transactional
    void findAllWithMemberUsesSingleJoinQuery() {
        String[] names = {"진우", "철수", "영희"};
        for (String name : names) {
            Member member = memberRepository.save(new Member(name));
            Reservation reservation = new Reservation("Room-" + name, name);
            reservation.assignMember(member);
            reservationRepository.save(reservation);
        }

        entityManager.flush();
        entityManager.clear();

        System.out.println("===== findAllWithMember() 호출 시작 =====");
        List<Reservation> reservations = reservationRepository.findAllWithMember();
        System.out.println("===== findAllWithMember() 끝, 지금부터 순회 시작 =====");

        for (Reservation r : reservations) {
            System.out.println(r.getMember().getName());
        }
        System.out.println("===== 순회 끝 =====");
    }
}