package com.example.studyroom.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudyRoomCreateRequest(
		@NotBlank(message = "스터디룸 이름은 필수입니다.") @Size(max = 100) String name,
		@NotBlank(message = "위치는 필수입니다.") @Size(max = 200) String location,
		@Min(value = 1, message = "수용 인원은 1명 이상이어야 합니다.")
		@Max(value = 1000, message = "수용 인원은 1000명 이하여야 합니다.") int capacity,
		@Size(max = 500) String description
) {
}
