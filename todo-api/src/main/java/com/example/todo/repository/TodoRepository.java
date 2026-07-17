package com.example.todo.repository;

import com.example.todo.domain.Todo;
import java.util.List;
import java.util.Optional;

// Repository 인터페이스는 "저장소가 제공해야 하는 기능"만 정의합니다.
// Service는 이 인터페이스만 바라보기 때문에, 나중에 Map 저장소를 JPA/MySQL로 바꿔도 Service 변경을 줄일 수 있습니다.
public interface TodoRepository {

	Todo save(Todo todo);

	List<Todo> findAll();

	Optional<Todo> findById(Long id);

	void deleteById(Long id);
}
