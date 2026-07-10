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
@Repository
public class InMemoryTodoRepository implements TodoRepository {

	private final Map<Long, Todo> store = new ConcurrentHashMap<>();
	private final AtomicLong sequence = new AtomicLong(1);

	@Override
	public Todo save(Todo todo) {
		if (todo.getId() == null) {
			todo.assignId(sequence.getAndIncrement());
		}
		store.put(todo.getId(), todo);
		return todo;
	}

	@Override
	public List<Todo> findAll() {
		return store.values()
				.stream()
				.sorted(Comparator.comparing(Todo::getId))
				.toList();
	}

	@Override
	public Optional<Todo> findById(Long id) {
		return Optional.ofNullable(store.get(id));
	}

	@Override
	public void deleteById(Long id) {
		store.remove(id);
	}
}
