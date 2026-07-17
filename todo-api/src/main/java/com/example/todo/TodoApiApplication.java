package com.example.todo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Spring Boot 애플리케이션의 시작점입니다.
// 이 클래스를 실행하면 내장 톰캣이 뜨고, Controller들이 HTTP 요청을 받을 준비를 합니다.
@SpringBootApplication
public class TodoApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodoApiApplication.class, args);
	}

}
