package com.example.todo.exception;

// 존재하지 않는 Todo를 요청했을 때 Service 계층에서 발생시키는 예외입니다.
// 이 예외는 GlobalExceptionHandler에서 404 Not Found 응답으로 바뀝니다.
public class TodoNotFoundException extends RuntimeException {

	public TodoNotFoundException(Long id) {
		super("Todo를 찾을 수 없습니다. id=" + id);
	}
}
