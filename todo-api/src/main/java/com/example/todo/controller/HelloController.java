package com.example.todo.controller;

import com.example.todo.dto.HelloResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 가장 단순한 Controller 예시입니다.
// GET /api/hello 요청을 받으면 HelloResponse DTO를 JSON으로 반환합니다.
@RestController
@RequestMapping("/api")
public class HelloController {

	// 서버가 정상적으로 실행 중인지 확인하기 위한 기본 API입니다.
	@GetMapping("/hello")
	public HelloResponse hello() {
		return new HelloResponse("Hello Todo API");
	}
}
