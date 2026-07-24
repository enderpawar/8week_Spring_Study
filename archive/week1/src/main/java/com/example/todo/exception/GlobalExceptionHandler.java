package com.example.todo.exception;

import com.example.todo.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 여러 Controller에서 발생한 예외를 한곳에서 JSON 응답으로 바꾸는 클래스입니다.
// Controller마다 try-catch를 반복하지 않게 해줍니다.
@RestControllerAdvice
public class GlobalExceptionHandler {

	// Service에서 TodoNotFoundException이 발생하면 404 Not Found 응답으로 변환합니다.
	@ExceptionHandler(TodoNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handleTodoNotFound(TodoNotFoundException exception, HttpServletRequest request) {
		return ErrorResponse.of(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
	}

	// @Valid 검증에 실패하면 어떤 필드가 왜 실패했는지 errors에 담아 400 응답으로 변환합니다.
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
		Map<String, String> errors = new LinkedHashMap<>();
		for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
			errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
		}
		return ErrorResponse.validation("요청 값이 올바르지 않습니다.", request.getRequestURI(), errors);
	}

	// JSON 문법이 깨졌거나 요청 body를 읽을 수 없을 때 400 응답으로 변환합니다.
	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleUnreadableJson(HttpMessageNotReadableException exception, HttpServletRequest request) {
		return ErrorResponse.of(HttpStatus.BAD_REQUEST, "요청 JSON 형식이 올바르지 않습니다.", request.getRequestURI());
	}
}
