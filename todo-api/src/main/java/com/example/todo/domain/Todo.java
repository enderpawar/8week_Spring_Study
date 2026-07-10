package com.example.todo.domain;

import java.time.LocalDateTime;
import lombok.Getter;

// Domain 객체는 Todo의 상태와 상태 변경 규칙을 표현합니다.
@Getter
public class Todo {

	private Long id;
	private String title;
	private String description;
	private boolean completed;
	private final LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public Todo(String title, String description) {
		this.title = title;
		this.description = description;
		this.completed = false;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = this.createdAt;
	}

	public void assignId(Long id) {
		if (this.id != null) {
			throw new IllegalStateException("이미 id가 할당된 Todo입니다.");
		}
		this.id = id;
	}

	// Todo 수정 규칙은 Controller가 아니라 Domain 객체 안에 둡니다.
	public void update(String title, String description) {
		this.title = title;
		this.description = description;
		this.updatedAt = LocalDateTime.now();
	}

	public void complete() {
		this.completed = true;
		this.updatedAt = LocalDateTime.now();
	}
}
