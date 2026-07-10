package com.example.todo.dto;

import com.example.todo.domain.Todo;
import java.time.LocalDateTime;

// TodoResponse는 Domain 객체를 API 응답 모양으로 변환한 DTO입니다.
public record TodoResponse(
		Long id,
		String title,
		String description,
		boolean completed,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {

	public static TodoResponse from(Todo todo) {
		return new TodoResponse(
				todo.getId(),
				todo.getTitle(),
				todo.getDescription(),
				todo.isCompleted(),
				todo.getCreatedAt(),
				todo.getUpdatedAt()
		);
	}
}
