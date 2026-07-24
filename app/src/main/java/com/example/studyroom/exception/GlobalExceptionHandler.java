package com.example.studyroom.exception;

import com.example.studyroom.dto.ErrorResponse;
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

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(StudyRoomNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handleStudyRoomNotFound(StudyRoomNotFoundException e, HttpServletRequest request) {
		return ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(DuplicateEmailException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ErrorResponse handleDuplicate(DuplicateEmailException e, HttpServletRequest request) {
		return ErrorResponse.of(HttpStatus.CONFLICT, e.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ErrorResponse handleInvalidCredentials(InvalidCredentialsException e, HttpServletRequest request) {
		return ErrorResponse.of(HttpStatus.UNAUTHORIZED, e.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(MemberNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handleMemberNotFound(MemberNotFoundException e, HttpServletRequest request) {
		return ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
		Map<String, String> errors = new LinkedHashMap<>();
		for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
			errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
		}
		return ErrorResponse.validation("요청 값이 올바르지 않습니다.", request.getRequestURI(), errors);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleUnreadableJson(HttpServletRequest request) {
		return ErrorResponse.of(HttpStatus.BAD_REQUEST, "요청 JSON 형식이 올바르지 않습니다.", request.getRequestURI());
	}
}
