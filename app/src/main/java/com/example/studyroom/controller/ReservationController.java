package com.example.studyroom.controller;

import com.example.studyroom.domain.Reservation;
import com.example.studyroom.dto.ReservationRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
public class ReservationController {

    //@RequestBody = 클라이언트가 보낸 JSON body를 자바 객체 (record)로 자동 변환
    @PostMapping("/reservations")
    public String reserve(@RequestBody @Valid ReservationRequest request) {
        Reservation reservation = new Reservation(request.roomName(), request.requesterName());
        reservation.confirm();
        return reservation.getRequesterName() + "님이 " + reservation.getRoomName() + " 예약 완료 (확정: " + reservation.isConfirmed() + ")";
    }
//아마 "" 로 해버리면 @Valid 유효성 검사 들어가서 400 BadRequest 뜨지 않을까 싶은데..

    @PostMapping("/reservations/cancel")
    public String cancel(@RequestBody ReservationRequest request) {
        Reservation reservation = new Reservation(request.roomName(), request.requesterName());
        reservation.canceled();
        return reservation.getRequesterName() + "님이" + reservation.getRoomName() + " 예약을 취소하셨습니다 (확정 : " + reservation.isConfirmed() + ")";
    }

}