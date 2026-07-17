package com.example.todo.dto;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;

// 에러 응답 DTO입니다.
// 예외가 발생했을 때도 클라이언트가 항상 비슷한 JSON 모양을 받도록 만듭니다.
public record ErrorResponse(
		LocalDateTime timestamp,
		int status,
		String error,
		String message,
		String path,
		Map<String, String> errors
) {

	// 단순 에러 응답을 만들 때 사용합니다. 예: 존재하지 않는 Todo, 잘못된 JSON 형식
	public static ErrorResponse of(HttpStatus status, String message, String path) {
		return new ErrorResponse(
				LocalDateTime.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				path,
				Map.of()
		);
	}

	// Validation 실패처럼 필드별 에러가 있는 경우 사용합니다.
	public static ErrorResponse validation(String message, String path, Map<String, String> errors) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		return new ErrorResponse(
				LocalDateTime.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				path,
				errors
		);
	}
}
