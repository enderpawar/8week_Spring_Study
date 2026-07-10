package com.example.todo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 요청 DTO는 클라이언트가 보낸 JSON을 검증 가능한 형태로 받습니다.
public record TodoCreateRequest(
		@NotBlank(message = "제목은 필수입니다.")
		@Size(max = 100, message = "제목은 100자 이하여야 합니다.")
		String title,

		@Size(max = 500, message = "설명은 500자 이하여야 합니다.")
		String description
) {
}
