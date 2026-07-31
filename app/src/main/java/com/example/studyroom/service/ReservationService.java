package com.example.studyroom.service;

import com.example.studyroom.domain.Reservation;
import com.example.studyroom.repository.ReservationRepository;
import org.springframework.stereotype.Service;

@Service
public class ReservationService{

    private final ReservationRepository reservationRepository;
    //생성자 주입 : Spring이 자동으로 InMemoryReservationRepository를 찾아 넣어준다.
    //원리는 5일 차에서 배우기..
    public ReservationService(ReservationRepository reservationRepository){
        this.reservationRepository = reservationRepository;
    }
    public Reservation reserve(String roomName, String requesterName){
        Reservation reservation = new Reservation(roomName,requesterName);
        reservation.confirm(); // 예약 상태를 변경하고
        return reservationRepository.save(reservation); // reservationRepository의 save 메서드 인자값으로 service가 상태를 변경한 reservation 값을 넘겨준다.
    }
    public Reservation cancel(Long id){
        Reservation reservation = reservationRepository.findById(id);
        reservation.cancel();
        reservationRepository.save(reservation);
        return reservation;
    }
}