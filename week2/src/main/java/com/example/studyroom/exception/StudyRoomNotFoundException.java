package com.example.studyroom.exception;

public class StudyRoomNotFoundException extends RuntimeException {
	public StudyRoomNotFoundException(Long id) {
		super("스터디룸을 찾을 수 없습니다. id=" + id);
	}
}
