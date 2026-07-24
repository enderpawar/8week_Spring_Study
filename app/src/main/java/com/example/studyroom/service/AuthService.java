package com.example.studyroom.service;

import com.example.studyroom.dto.LoginRequest;
import com.example.studyroom.dto.MemberResponse;
import com.example.studyroom.dto.SignupRequest;
import com.example.studyroom.dto.TokenResponse;
import com.example.studyroom.entity.User;
import com.example.studyroom.exception.DuplicateEmailException;
import com.example.studyroom.exception.InvalidCredentialsException;
import com.example.studyroom.repository.UserRepository;
import com.example.studyroom.security.JwtTokenProvider;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;

	@Transactional
	public MemberResponse signup(SignupRequest request) {
		String email = normalizeEmail(request.email());
		if (userRepository.existsByEmail(email)) {
			throw new DuplicateEmailException();
		}
		User user = new User(email, request.name().trim(), passwordEncoder.encode(request.password()));
		return MemberResponse.from(userRepository.save(user));
	}

	public TokenResponse login(LoginRequest request) {
		String email = normalizeEmail(request.email());
		User user = userRepository.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new InvalidCredentialsException();
		}
		return TokenResponse.bearer(jwtTokenProvider.createToken(email), jwtTokenProvider.getExpirationSeconds());
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
