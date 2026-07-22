package com.example.studyroom.service;

import com.example.studyroom.dto.MemberResponse;
import com.example.studyroom.entity.User;
import com.example.studyroom.exception.MemberNotFoundException;
import com.example.studyroom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
	private final UserRepository userRepository;

	public MemberResponse findMe(String email) {
		User user = userRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
		return MemberResponse.from(user);
	}
}
