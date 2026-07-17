package com.example.todo.repository;

import com.example.todo.domain.Todo;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

// InMemoryTodoRepository는 DB 없이 Map에 데이터를 저장하는 학습용 Repository 구현체입니다.
// 애플리케이션을 재시작하면 메모리가 초기화되므로 저장된 Todo도 사라집니다.
@Repository
public class InMemoryTodoRepository implements TodoRepository {

	// key는 Todo id, value는 Todo 객체입니다.
	// ConcurrentHashMap은 여러 요청이 동시에 들어와도 일반 HashMap보다 안전하게 동작합니다.
	private final Map<Long, Todo> store = new ConcurrentHashMap<>();

	// 새 Todo에 부여할 id 값을 1부터 순서대로 증가시키는 도구입니다.
	private final AtomicLong sequence = new AtomicLong(1);

	@Override
	public Todo save(Todo todo) {
		// 새 Todo는 id가 없으므로 저장하기 전에 id를 부여합니다.
		// 이미 id가 있는 Todo는 수정된 객체를 같은 id 위치에 다시 저장합니다.
		if (todo.getId() == null) {
			todo.assignId(sequence.getAndIncrement());
		}
		store.put(todo.getId(), todo);
		return todo;
	}

	@Override
	public List<Todo> findAll() {
		// Map의 값들을 꺼내 id 오름차순으로 정렬해서 반환합니다.
		return store.values()
				.stream()
				.sorted(Comparator.comparing(Todo::getId))
				.toList();
	}

	@Override
	public Optional<Todo> findById(Long id) {
		// 값이 없을 수 있으므로 null 대신 Optional로 감싸서 반환합니다.
		return Optional.ofNullable(store.get(id));
	}

	@Override
	public void deleteById(Long id) {
		// 해당 id가 있으면 삭제하고, 없어도 별도 예외 없이 지나갑니다.
		store.remove(id);
	}
}
