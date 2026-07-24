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

// TodoController는 Todo 관련 HTTP 요청이 처음 들어오는 입구입니다.
// Controller는 저장/수정 같은 실제 작업을 직접 하지 않고 Service에게 위임합니다.
// 즉, 1. 요청값 해석(POST,GET, PATCH, DELETE 등) 2. Service 호출 3. Service 결과를 응답으로 변환 정도만 담당합니다.
@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class TodoController {

	private final TodoService todoService;

	// POST /api/todos
	// 클라이언트가 보낸 JSON을 TodoCreateRequest로 받고, 실제 생성은 TodoService에게 맡깁니다.
	// @Valid는 TodoCreateRequest 안의 @NotBlank, @Size 같은 검증 규칙을 실행합니다.
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TodoResponse create(@Valid @RequestBody TodoCreateRequest request) {
		return todoService.create(request);
	}

	// GET /api/todos
	// 저장된 Todo 전체 목록을 조회합니다. Controller는 Service 결과를 그대로 응답합니다.
	@GetMapping
	public List<TodoResponse> findAll() {
		return todoService.findAll();
	}

	// GET /api/todos/{id}
	// URL의 {id} 값을 @PathVariable로 꺼내 특정 Todo 하나를 조회합니다.
//	@GetMapping("/{id}")
//	public TodoResponse findById(@PathVariable Long id) {
//		return todoService.findById(id);
//	}

	// PATCH /api/todos/{id}
	// URL의 id로 수정할 Todo를 찾고, 요청 body의 title/description으로 내용을 바꿉니다.
	@PatchMapping("/{id}")
	public TodoResponse update(@PathVariable Long id, @Valid @RequestBody TodoUpdateRequest request) {
		return todoService.update(id, request);
	}

	// PATCH /api/todos/{id}/complete
	// 특정 Todo를 완료 상태로 바꿉니다. 별도 요청 body가 필요 없어서 id만 받습니다.
	@PatchMapping("/{id}/complete")
	public TodoResponse complete(@PathVariable Long id) {
		return todoService.complete(id);
	}

	// DELETE /api/todos/{id}
	// 삭제 성공 시 응답 body 없이 204 No Content만 반환합니다.
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		todoService.delete(id);
	}
}
