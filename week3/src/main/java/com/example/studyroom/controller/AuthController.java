package com.example.studyroom.controller;

import com.example.studyroom.dto.LoginRequest;
import com.example.studyroom.dto.MemberResponse;
import com.example.studyroom.dto.SignupRequest;
import com.example.studyroom.dto.TokenResponse;
import com.example.studyroom.service.AuthService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;

	@PostMapping("/signup")
	public ResponseEntity<MemberResponse> signup(@Valid @RequestBody SignupRequest request) {
		MemberResponse response = authService.signup(request);
		return ResponseEntity.created(URI.create("/api/members/" + response.id())).body(response);
	}

	@PostMapping("/login")
	public TokenResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}
}
