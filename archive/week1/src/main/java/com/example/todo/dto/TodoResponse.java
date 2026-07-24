package com.example.todo.dto;

import com.example.todo.domain.Todo;
import java.time.LocalDateTime;

// 응답 DTO입니다.
// Domain 객체인 Todo를 그대로 외부에 노출하지 않고, API 응답에 필요한 필드만 골라 반환합니다.
public record TodoResponse(
		Long id,
		String title,
		String description,
		boolean completed,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {

	// Service에서 Todo를 응답 모양으로 바꿀 때 사용하는 정적 팩토리 메서드입니다.
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
