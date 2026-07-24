package com.example.todo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 생성 요청 DTO입니다.
// 클라이언트가 보낸 JSON body를 Java 객체로 받기 위한 모양입니다.
// @Valid가 Controller 파라미터에 붙어 있어야 아래 검증 어노테이션들이 실행됩니다.
public record TodoCreateRequest(
		@NotBlank(message = "제목은 필수입니다.")
		@Size(max = 100, message = "제목은 100자 이하여야 합니다.")
		String title,
		@Size(max = 500, message = "설명은 500자 이하여야 합니다.")
		String description
) {
}
