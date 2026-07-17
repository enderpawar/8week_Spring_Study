package com.example.todo.domain;

import java.time.LocalDateTime;
import lombok.Getter;

// Domain 객체는 Todo 자체의 데이터와 상태 변경 규칙을 표현합니다. 그러니까, todo 같은게 완료되었는지, 수정중인지. 이런걸 표현해준다고.
// Controller나 Service가 필드를 직접 바꾸기보다 Todo.update(), Todo.complete() 같은 메서드로 변경합니다.
@Getter
public class Todo {

	private Long id;
	private String title;
	private String description;
	private boolean completed;
	private final LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	// 새 Todo를 만들 때 필요한 값은 title, description입니다.
	// id는 저장소가 저장하는 순간 assignId로 부여하고, 완료 여부는 기본값 false입니다.
	public Todo(String title, String description) {
		this.title = title;
		this.description = description;
		this.completed = false;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = this.createdAt;
	}

	// InMemory 저장소가 새 Todo에 id를 부여할 때 사용합니다.
	// 이미 id가 있는 Todo에 다시 id를 부여하면 잘못된 상태라서 예외를 던집니다.
	public void assignId(Long id) {
		if (this.id != null) {
			throw new IllegalStateException("이미 id가 할당된 Todo입니다.");
		}
		this.id = id;
	}

	// Todo 수정 규칙은 Domain 객체 안에 둡니다.
	// 수정될 때마다 updatedAt도 함께 갱신된다는 규칙을 한곳에서 관리할 수 있습니다.
	public void update(String title, String description) {
		this.title = title;
		this.description = description;
		this.updatedAt = LocalDateTime.now();
	}

	// 완료 처리도 단순히 completed만 바꾸는 것이 아니라 updatedAt까지 함께 갱신합니다.
	public void complete() {
		this.completed = true;
		this.updatedAt = LocalDateTime.now();
	}
}
