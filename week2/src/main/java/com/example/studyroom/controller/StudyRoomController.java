package com.example.studyroom.controller;

import com.example.studyroom.dto.StudyRoomCreateRequest;
import com.example.studyroom.dto.StudyRoomResponse;
import com.example.studyroom.dto.StudyRoomUpdateRequest;
import com.example.studyroom.service.StudyRoomService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/study-rooms")
@RequiredArgsConstructor
public class StudyRoomController {

	private final StudyRoomService studyRoomService;

	@PostMapping
	public ResponseEntity<StudyRoomResponse> create(@Valid @RequestBody StudyRoomCreateRequest request) {
		StudyRoomResponse response = studyRoomService.create(request);
		return ResponseEntity.created(URI.create("/api/study-rooms/" + response.id())).body(response);
	}

	@GetMapping
	public List<StudyRoomResponse> findAll() {
		return studyRoomService.findAll();
	}

	@GetMapping("/{id}")
	public StudyRoomResponse findById(@PathVariable Long id) {
		return studyRoomService.findById(id);
	}

	@PatchMapping("/{id}")
	public StudyRoomResponse update(
			@PathVariable Long id,
			@Valid @RequestBody StudyRoomUpdateRequest request
	) {
		return studyRoomService.update(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		studyRoomService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
