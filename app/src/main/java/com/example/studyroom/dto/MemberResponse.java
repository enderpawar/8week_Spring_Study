package com.example.studyroom.dto;

import com.example.studyroom.entity.User;

public record MemberResponse(Long id, String email, String name) {
	public static MemberResponse from(User user) {
		return new MemberResponse(user.getId(), user.getEmail(), user.getName());
	}
}
