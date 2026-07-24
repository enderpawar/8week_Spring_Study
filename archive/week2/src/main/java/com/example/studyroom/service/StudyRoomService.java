package com.example.studyroom.service;

import com.example.studyroom.dto.StudyRoomCreateRequest;
import com.example.studyroom.dto.StudyRoomResponse;
import com.example.studyroom.dto.StudyRoomUpdateRequest;
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

	private final StudyRoomRepository studyRoomRepository;

	@Transactional
	public StudyRoomResponse create(StudyRoomCreateRequest request) {
		StudyRoom studyRoom = new StudyRoom(
				request.name(), request.location(), request.capacity(), request.description());
		return StudyRoomResponse.from(studyRoomRepository.save(studyRoom));
	}

	public List<StudyRoomResponse> findAll() {
		return studyRoomRepository.findAll().stream()
				.map(StudyRoomResponse::from)
				.toList();
	}

	public StudyRoomResponse findById(Long id) {
		return StudyRoomResponse.from(getStudyRoom(id));
	}

	@Transactional
	public StudyRoomResponse update(Long id, StudyRoomUpdateRequest request) {
		StudyRoom studyRoom = getStudyRoom(id);
		studyRoom.update(request.name(), request.location(), request.capacity(), request.description());
		return StudyRoomResponse.from(studyRoom);
	}

	@Transactional
	public void delete(Long id) {
		StudyRoom studyRoom = getStudyRoom(id);
		studyRoomRepository.delete(studyRoom);
	}

	private StudyRoom getStudyRoom(Long id) {
		return studyRoomRepository.findById(id)
				.orElseThrow(() -> new StudyRoomNotFoundException(id));
	}
}
