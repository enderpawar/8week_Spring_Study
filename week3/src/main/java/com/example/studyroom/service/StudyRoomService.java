package com.example.studyroom.service;

import com.example.studyroom.dto.*;
import com.example.studyroom.entity.StudyRoom;
import com.example.studyroom.exception.StudyRoomNotFoundException;
import com.example.studyroom.repository.StudyRoomRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyRoomService {
	private final StudyRoomRepository repository;

	@Transactional
	public StudyRoomResponse create(StudyRoomCreateRequest request) {
		StudyRoom room = new StudyRoom(request.name(), request.location(), request.capacity(), request.description());
		return StudyRoomResponse.from(repository.save(room));
	}

	public List<StudyRoomResponse> findAll() {
		return repository.findAll().stream().map(StudyRoomResponse::from).toList();
	}

	public StudyRoomResponse findById(Long id) { return StudyRoomResponse.from(findEntity(id)); }

	@Transactional
	public StudyRoomResponse update(Long id, StudyRoomUpdateRequest request) {
		StudyRoom room = findEntity(id);
		room.update(request.name(), request.location(), request.capacity(), request.description());
		return StudyRoomResponse.from(room);
	}

	@Transactional
	public void delete(Long id) { repository.delete(findEntity(id)); }

	private StudyRoom findEntity(Long id) {
		return repository.findById(id).orElseThrow(() -> new StudyRoomNotFoundException(id));
	}
}
