package com.example.studyroom;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:api-test;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver"
})
@AutoConfigureMockMvc
class StudyRoomApiIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void createAndFindStudyRoom() throws Exception {
		long id = createStudyRoom("Spring A반");

		mockMvc.perform(get("/api/study-rooms/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Spring A반"))
				.andExpect(jsonPath("$.location").value("서울 강남구"))
				.andExpect(jsonPath("$.capacity").value(6));
	}

	@Test
	void findAllStudyRooms() throws Exception {
		createStudyRoom("JPA 학습방");

		mockMvc.perform(get("/api/study-rooms"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].name", hasItem("JPA 학습방")));
	}

	@Test
	void updateStudyRoom() throws Exception {
		long id = createStudyRoom("수정 전 이름");

		mockMvc.perform(patch("/api/study-rooms/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "수정된 스터디룸",
								  "capacity": 10
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("수정된 스터디룸"))
				.andExpect(jsonPath("$.capacity").value(10))
				.andExpect(jsonPath("$.location").value("서울 강남구"));
	}

	@Test
	void deleteStudyRoom() throws Exception {
		long id = createStudyRoom("삭제할 스터디룸");

		mockMvc.perform(delete("/api/study-rooms/{id}", id))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/study-rooms/{id}", id))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("스터디룸을 찾을 수 없습니다. id=" + id));
	}

	@Test
	void rejectInvalidCreateRequest() throws Exception {
		mockMvc.perform(post("/api/study-rooms")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "",
								  "location": "서울",
								  "capacity": 0
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.name").value("스터디룸 이름은 필수입니다."))
				.andExpect(jsonPath("$.errors.capacity").value("수용 인원은 1명 이상이어야 합니다."));
	}

	private long createStudyRoom(String name) throws Exception {
		String responseBody = mockMvc.perform(post("/api/study-rooms")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "%s",
								  "location": "서울 강남구",
								  "capacity": 6,
								  "description": "조용한 학습 공간"
								}
								""".formatted(name)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andReturn()
				.getResponse()
				.getContentAsString();

		JsonNode responseJson = objectMapper.readTree(responseBody);
		return responseJson.get("id").asLong();
	}
}
