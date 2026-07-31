package com.example.studyroom.repository;

import com.example.studyroom.domain.Reservation;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository //Spring이 관리하는 Bean, "이 클래스는 저장소 역할의 Bean이다."
public class InMemoryReservationRepository implements ReservationRepository {
    private final List<Reservation> store = new ArrayList<>();


    @Override // 메서드를 여기서 override로 재정의해주는거지. 즉 How를 정의해준다!
    public Reservation save(Reservation reservation) {
        store.add(reservation);
        return reservation;
    }


    @Override
    public List<Reservation> findAll() {
        return store;
    }
}