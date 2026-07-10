package com.example.todo.repository;

import com.example.todo.domain.Todo;
import java.util.List;
import java.util.Optional;

// Repository는 저장소가 Map인지 DB인지 감추고 저장/조회 기능만 제공합니다.
public interface TodoRepository {

	Todo save(Todo todo);

	List<Todo> findAll();

	Optional<Todo> findById(Long id);

	void deleteById(Long id);
}
