package com.example.studyroom.dto;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;

public record ErrorResponse(
		LocalDateTime timestamp, int status, String error, String message,
		String path, Map<String, String> errors
) {
	public static ErrorResponse of(HttpStatus status, String message, String path) {
		return new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, path, Map.of());
	}

	public static ErrorResponse validation(String message, String path, Map<String, String> errors) {
		return new ErrorResponse(LocalDateTime.now(), 400, "Bad Request", message, path, errors);
	}
}
