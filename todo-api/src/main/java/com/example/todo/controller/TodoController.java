package com.example.todo.controller;

import com.example.todo.dto.TodoCreateRequest;
import com.example.todo.dto.TodoResponse;
import com.example.todo.dto.TodoUpdateRequest;
import com.example.todo.service.TodoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// TodoController는 Todo 관련 HTTP API의 입구입니다.
@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class TodoController {

	private final TodoService todoService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TodoResponse create(@Valid @RequestBody TodoCreateRequest request) {
		return todoService.create(request);
	}

	@GetMapping
	public List<TodoResponse> findAll() {
		return todoService.findAll();
	}

	@GetMapping("/{id}")
	public TodoResponse findById(@PathVariable Long id) {
		return todoService.findById(id);
	}

	@PatchMapping("/{id}")
	public TodoResponse update(@PathVariable Long id, @Valid @RequestBody TodoUpdateRequest request) {
		return todoService.update(id, request);
	}

	@PatchMapping("/{id}/complete")
	public TodoResponse complete(@PathVariable Long id) {
		return todoService.complete(id);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		todoService.delete(id);
	}
}
