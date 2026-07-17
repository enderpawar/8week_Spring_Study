package com.example.todo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 수정 요청 DTO입니다.
// 지금은 생성 요청과 검증 규칙이 같지만, 나중에 수정 규칙이 달라질 수 있어서 별도 DTO로 둡니다.
public record TodoUpdateRequest(
		@NotBlank(message = "제목은 필수입니다.")
		@Size(max = 100, message = "제목은 100자 이하여야 합니다.")
		String title,

		@Size(max = 500, message = "설명은 500자 이하여야 합니다.")
		String description
) {
}


