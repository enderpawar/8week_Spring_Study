package com.example.todo;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class TodoApiIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void hello() throws Exception {
		mockMvc.perform(get("/api/hello"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Hello Todo API"));
	}

	@Test
	void createAndFindTodo() throws Exception {
		String response = mockMvc.perform(post("/api/todos")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Spring Controller 공부",
								  "description": "요청과 응답 흐름 이해하기"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.title").value("Spring Controller 공부"))
				.andExpect(jsonPath("$.completed").value(false))
				.andReturn()
				.getResponse()
				.getContentAsString();

		String id = response.replaceAll(".*\"id\":(\\d+).*", "$1");

		mockMvc.perform(get("/api/todos/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Spring Controller 공부"));
	}

	@Test
	void findAllTodos() throws Exception {
		mockMvc.perform(post("/api/todos")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Service 공부",
								  "description": "비즈니스 흐름 분리하기"
								}
								"""))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/todos"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].title", hasItem("Service 공부")));
	}

	@Test
	void updateAndCompleteTodo() throws Exception {
		String response = mockMvc.perform(post("/api/todos")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Repository 공부",
								  "description": "Map 저장소 이해하기"
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		String id = response.replaceAll(".*\"id\":(\\d+).*", "$1");

		mockMvc.perform(patch("/api/todos/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Repository 다시 공부",
								  "description": "저장소 인터페이스 이해하기"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Repository 다시 공부"));

		mockMvc.perform(patch("/api/todos/{id}/complete", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.completed").value(true));
	}

	@Test
	void deleteTodo() throws Exception {
		String response = mockMvc.perform(post("/api/todos")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "삭제 테스트",
								  "description": "삭제 후 조회 실패 확인"
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		String id = response.replaceAll(".*\"id\":(\\d+).*", "$1");

		mockMvc.perform(delete("/api/todos/{id}", id))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/todos/{id}", id))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Todo를 찾을 수 없습니다. id=" + id));
	}

	@Test
	void validationError() throws Exception {
		mockMvc.perform(post("/api/todos")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "",
								  "description": "제목이 비어 있으면 실패"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.title").value("제목은 필수입니다."));
	}
}
