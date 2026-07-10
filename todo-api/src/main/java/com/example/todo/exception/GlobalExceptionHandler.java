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

// GlobalExceptionHandler는 여러 Controller에서 발생한 예외를 한 곳에서 JSON 응답으로 바꿉니다.
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(TodoNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handleTodoNotFound(TodoNotFoundException exception, HttpServletRequest request) {
		return ErrorResponse.of(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
		Map<String, String> errors = new LinkedHashMap<>();
		for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
			errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
		}
		return ErrorResponse.validation("요청 값이 올바르지 않습니다.", request.getRequestURI(), errors);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleUnreadableJson(HttpMessageNotReadableException exception, HttpServletRequest request) {
		return ErrorResponse.of(HttpStatus.BAD_REQUEST, "요청 JSON 형식이 올바르지 않습니다.", request.getRequestURI());
	}
}
