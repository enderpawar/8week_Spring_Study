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

// Service는 Controller와 Repository 사이에서 실제 기능 흐름을 담당합니다.
// Controller는 HTTP만 알고, Repository는 저장 방식만 알게 두기 위해 Service가 중간 역할을 합니다.
@Service
@RequiredArgsConstructor
public class TodoService {

	private final TodoRepository todoRepository;

	// 생성 흐름:
	// 요청 DTO -> Todo 도메인 객체 생성 -> Repository 저장 -> 응답 DTO 변환
	public TodoResponse create(TodoCreateRequest request) {
		Todo todo = new Todo(request.title(), request.description());
		Todo savedTodo = todoRepository.save(todo);
		return TodoResponse.from(savedTodo);
	}

	// Repository에서 Todo 목록을 꺼낸 뒤, API 응답에 맞는 TodoResponse 목록으로 바꿉니다.
	public List<TodoResponse> findAll() {
		return todoRepository.findAll()
				.stream()
				.map(TodoResponse::from)
				.toList();
	}

	// 단건 조회는 먼저 Todo를 찾고, 찾은 도메인 객체를 응답 DTO로 변환합니다.
	public TodoResponse findById(Long id) {
		Todo todo = getTodo(id);
		return TodoResponse.from(todo);
	}

	// 수정할 Todo를 찾은 뒤, 상태 변경은 Todo 객체 자신의 update 메서드에게 맡깁니다.
	public TodoResponse update(Long id, TodoUpdateRequest request) {
		Todo todo = getTodo(id);
		todo.update(request.title(), request.description());
		todoRepository.save(todo);
		return TodoResponse.from(todo);
	}

	// 완료 처리도 Service가 필드를 직접 바꾸지 않고 Todo.complete()로 표현합니다.
	public TodoResponse complete(Long id) {
		Todo todo = getTodo(id);
		todo.complete();
		todoRepository.save(todo);
		return TodoResponse.from(todo);
	}

	// 삭제 전에 getTodo(id)를 호출해서 존재하지 않는 id라면 404 예외가 나가게 합니다.
	public void delete(Long id) {
		getTodo(id);
		todoRepository.deleteById(id);
	}

	// 여러 메서드에서 반복되는 "id로 찾고, 없으면 예외" 로직을 한곳에 모았습니다.
	private Todo getTodo(Long id) {
		return todoRepository.findById(id)
				.orElseThrow(() -> new TodoNotFoundException(id));
	}
}
