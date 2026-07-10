package com.example.todo.dto;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;

// ErrorResponse는 예외 상황에서도 일정한 JSON 응답 모양을 유지하기 위한 DTO입니다.
public record ErrorResponse(
		LocalDateTime timestamp,
		int status,
		String error,
		String message,
		String path,
		Map<String, String> errors
) {

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
