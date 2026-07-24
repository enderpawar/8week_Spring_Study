package com.example.studyroom.dto;

import com.example.studyroom.entity.StudyRoom;
import java.time.LocalDateTime;

public record StudyRoomResponse(
		Long id, String name, String location, int capacity, String description,
		LocalDateTime createdAt, LocalDateTime updatedAt
) {
	public static StudyRoomResponse from(StudyRoom room) {
		return new StudyRoomResponse(room.getId(), room.getName(), room.getLocation(), room.getCapacity(),
				room.getDescription(), room.getCreatedAt(), room.getUpdatedAt());
	}
}
