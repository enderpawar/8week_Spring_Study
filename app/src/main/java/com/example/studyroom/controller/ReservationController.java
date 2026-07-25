package com.example.studyroom.controller;

import com.example.studyroom.domain.Reservation;
import com.example.studyroom.dto.ReservationRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReservationController {

    //@RequestBody = 클라이언트가 보낸 JSON body를 자바 객체 (record)로 자동 변환
    @PostMapping("/reservations")
    public String reserve(@RequestBody ReservationRequest request) {
        Reservation reservation = new Reservation(request.roomName(), request.requesterName());
        reservation.confirm();
        return reservation.getRequesterName() + "님이 " + reservation.getRoomName() + " 예약 완료 (확정: " + reservation.isConfirmed() + ")";
    }


    @PostMapping("/reservations/cancel")
    public String cancel(@RequestBody ReservationRequest request) {
        Reservation reservation = new Reservation(request.roomName(), request.requesterName());
        reservation.canceled();
        return reservation.getRequesterName() + "님이" + reservation.getRoomName() + " 예약을 취소하셨습니다 (확정 : " + reservation.isConfirmed() + ")";
    }

}