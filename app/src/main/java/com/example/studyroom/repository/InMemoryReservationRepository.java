package com.example.studyroom.repository;

import com.example.studyroom.domain.Reservation;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository //Spring이 관리하는 Bean, "이 클래스는 저장소 역할의 Bean이다."
public class InMemoryReservationRepository implements ReservationRepository {
    private final List<Reservation> store = new ArrayList<>();
    private long nextId = 1;

    @Override // 메서드를 여기서 override로 재정의해주는거지. 즉 How를 정의해준다!
    public Reservation save(Reservation reservation) {
        if (reservation.getId() == null){
            reservation.assignId(nextId++);
            store.add(reservation);
            return reservation;
        }

        for (int index = 0; index < store.size(); index++) {
            if (store.get(index).getId().equals(reservation.getId())) {
                store.set(index, reservation);
                return reservation;
            }
        }

        throw new IllegalArgumentException("저장소에 없는 예약 번호입니다: " + reservation.getId());
    }


    @Override
    public List<Reservation> findAll() {
        return store;
    }
    @Override
    public Optional<Reservation> findById(Long id) {
        for(Reservation r : store){
            if(r.getId().equals(id)){
                return Optional.of(r);
            }
        }
        return Optional.empty();
    }
}
