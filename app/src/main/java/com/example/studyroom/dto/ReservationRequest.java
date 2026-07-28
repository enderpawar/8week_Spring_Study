package com.example.studyroom.dto;
import jakarta.validation.constraints.NotBlank;
// record = 데이터 모양만 정의. 생성자, getter, equals, hashcode 자동 생성.
// ㄴ DTO르르 만드는데 자바의 record 문법을 사용했다고 생각하면 됨. record가 DTO 그 자체다!!
// 불변(immutable) 속성을 지님. - 필드를 한 번 정하면 못 바꾼다.
public record ReservationRequest(@NotBlank(message = "방 이름은 비어있을 수 없습니다") String roomName, @NotBlank(message = "예약자 이름은 비어있을 수 없습니다.") String requesterName) {
}


