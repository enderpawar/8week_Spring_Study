package com.example.studyroom.controller;

import com.example.studyroom.dto.*;
import com.example.studyroom.service.StudyRoomService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/study-rooms")
@RequiredArgsConstructor
public class StudyRoomController {
	private final StudyRoomService service;

	@PostMapping
	public ResponseEntity<StudyRoomResponse> create(@Valid @RequestBody StudyRoomCreateRequest request) {
		StudyRoomResponse response = service.create(request);
		return ResponseEntity.created(URI.create("/api/study-rooms/" + response.id())).body(response);
	}

	@GetMapping public List<StudyRoomResponse> findAll() { return service.findAll(); }
	@GetMapping("/{id}") public StudyRoomResponse findById(@PathVariable Long id) { return service.findById(id); }
	@PatchMapping("/{id}") public StudyRoomResponse update(@PathVariable Long id,
			@Valid @RequestBody StudyRoomUpdateRequest request) { return service.update(id, request); }
	@DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}
}
