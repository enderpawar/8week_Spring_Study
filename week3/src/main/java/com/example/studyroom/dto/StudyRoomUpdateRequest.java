package com.example.studyroom.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record StudyRoomUpdateRequest(
		@Size(min = 1, max = 100) String name,
		@Size(min = 1, max = 200) String location,
		@Min(value = 1) @Max(value = 1000) Integer capacity,
		@Size(max = 500) String description
) {
}
