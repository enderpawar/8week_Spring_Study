package com.example.todo.controller;

import com.example.todo.dto.HelloResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Controller는 HTTP 요청을 받고, 응답 DTO를 반환하는 웹 계층입니다.
@RestController
@RequestMapping("/api")
public class HelloController {

	@GetMapping("/hello")
	public HelloResponse hello() {
		return new HelloResponse("Hello Todo API");
	}
}
