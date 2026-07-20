package com.example.studyroom.dto;

import com.example.studyroom.entity.StudyRoom;
import java.time.LocalDateTime;

public record StudyRoomResponse(
		Long id,
		String name,
		String location,
		int capacity,
		String description,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
	public static StudyRoomResponse from(StudyRoom studyRoom) {
		return new StudyRoomResponse(
				studyRoom.getId(),
				studyRoom.getName(),
				studyRoom.getLocation(),
				studyRoom.getCapacity(),
				studyRoom.getDescription(),
				studyRoom.getCreatedAt(),
				studyRoom.getUpdatedAt()
		);
	}
}
