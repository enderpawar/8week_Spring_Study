package com.example.studyroom.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record StudyRoomUpdateRequest(
		@Size(min = 1, max = 100, message = "스터디룸 이름은 1자 이상 100자 이하여야 합니다.")
		String name,

		@Size(min = 1, max = 200, message = "위치는 1자 이상 200자 이하여야 합니다.")
		String location,

		@Min(value = 1, message = "수용 인원은 1명 이상이어야 합니다.")
		@Max(value = 1000, message = "수용 인원은 1000명 이하여야 합니다.")
		Integer capacity,

		@Size(max = 500, message = "설명은 500자 이하여야 합니다.")
		String description
) {
}
