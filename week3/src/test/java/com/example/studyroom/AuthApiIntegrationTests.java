package com.example.studyroom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.studyroom.entity.User;
import com.example.studyroom.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthApiIntegrationTests {
	@Autowired MockMvc mockMvc;
	@Autowired ObjectMapper objectMapper;
	@Autowired UserRepository userRepository;
	@Autowired PasswordEncoder passwordEncoder;

	@BeforeEach
	void cleanDatabase() { userRepository.deleteAll(); }

	@Test
	void signupHashesPassword() throws Exception {
		mockMvc.perform(post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(signupJson()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value("student@example.com"))
				.andExpect(jsonPath("$.name").value("김학생"))
				.andExpect(jsonPath("$.password").doesNotExist());

		User saved = userRepository.findByEmail("student@example.com").orElseThrow();
		assertThat(saved.getPassword()).isNotEqualTo("password123");
		assertThat(passwordEncoder.matches("password123", saved.getPassword())).isTrue();
	}

	@Test
	void loginThenFindMeWithJwt() throws Exception {
		signup();
		String loginBody = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"student@example.com","password":"password123"}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andReturn().getResponse().getContentAsString();
		JsonNode json = objectMapper.readTree(loginBody);
		String token = json.get("accessToken").asText();

		mockMvc.perform(get("/api/members/me").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("student@example.com"))
				.andExpect(jsonPath("$.name").value("김학생"));
	}

	@Test
	void protectedApiRejectsMissingToken() throws Exception {
		mockMvc.perform(get("/api/members/me"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void duplicateEmailReturnsConflict() throws Exception {
		signup();
		mockMvc.perform(post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON).content(signupJson()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("이미 가입된 이메일입니다."));
	}

	@Test
	void wrongPasswordReturnsUnauthorized() throws Exception {
		signup();
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"student@example.com","password":"wrong-password"}
						"""))
				.andExpect(status().isUnauthorized());
	}

	private void signup() throws Exception {
		mockMvc.perform(post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON).content(signupJson()))
				.andExpect(status().isCreated());
	}

	private String signupJson() {
		return """
				{"email":"student@example.com","name":"김학생","password":"password123"}
				""";
	}
}
