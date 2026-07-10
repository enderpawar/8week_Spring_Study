package com.example.todo.exception;

// TodoNotFoundException은 존재하지 않는 Todo를 요청했을 때 Service 계층에서 발생시킵니다.
public class TodoNotFoundException extends RuntimeException {

	public TodoNotFoundException(Long id) {
		super("Todo를 찾을 수 없습니다. id=" + id);
	}
}
