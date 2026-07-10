package com.example.todo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 수정 요청도 별도 DTO로 분리하면 생성 요청과 다른 검증 규칙을 둘 수 있습니다.
public record TodoUpdateRequest(
		@NotBlank(message = "제목은 필수입니다.")
		@Size(max = 100, message = "제목은 100자 이하여야 합니다.")
		String title,

		@Size(max = 500, message = "설명은 500자 이하여야 합니다.")
		String description
) {
}
