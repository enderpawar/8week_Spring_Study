package com.example.studyroom.controller;

import com.example.studyroom.dto.MemberResponse;
import com.example.studyroom.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
	private final MemberService memberService;

	@GetMapping("/me")
	public MemberResponse me(@AuthenticationPrincipal UserDetails userDetails) {
		return memberService.findMe(userDetails.getUsername());
	}
}
