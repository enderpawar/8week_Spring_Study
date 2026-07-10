package com.example.todo.service;

import com.example.todo.domain.Todo;
import com.example.todo.dto.TodoCreateRequest;
import com.example.todo.dto.TodoResponse;
import com.example.todo.dto.TodoUpdateRequest;
import com.example.todo.exception.TodoNotFoundException;
import com.example.todo.repository.TodoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// Service는 Controller와 Repository 사이에서 비즈니스 흐름을 담당합니다.
@Service
@RequiredArgsConstructor
public class TodoService {

	private final TodoRepository todoRepository;

	public TodoResponse create(TodoCreateRequest request) {
		Todo todo = new Todo(request.title(), request.description());
		Todo savedTodo = todoRepository.save(todo);
		return TodoResponse.from(savedTodo);
	}

	public List<TodoResponse> findAll() {
		return todoRepository.findAll()
				.stream()
				.map(TodoResponse::from)
				.toList();
	}

	public TodoResponse findById(Long id) {
		Todo todo = getTodo(id);
		return TodoResponse.from(todo);
	}

	public TodoResponse update(Long id, TodoUpdateRequest request) {
		Todo todo = getTodo(id);
		todo.update(request.title(), request.description());
		todoRepository.save(todo);
		return TodoResponse.from(todo);
	}

	public TodoResponse complete(Long id) {
		Todo todo = getTodo(id);
		todo.complete();
		todoRepository.save(todo);
		return TodoResponse.from(todo);
	}

	public void delete(Long id) {
		getTodo(id);
		todoRepository.deleteById(id);
	}

	private Todo getTodo(Long id) {
		return todoRepository.findById(id)
				.orElseThrow(() -> new TodoNotFoundException(id));
	}
}
