package com.coing.domain.user.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.coing.domain.user.service.AuthTokenService;
import com.coing.domain.user.service.UserService;
import com.coing.util.MessageUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
public class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private MessageUtil messageUtil;

	@MockitoBean
	private AuthTokenService authTokenService;

	@WithMockUser
	@Test
	@DisplayName("로그아웃 - BasicResponse JSON 반환 테스트")
	void logout_Success() throws Exception {
		mockMvc.perform(post("/api/auth/logout")
				.with(csrf())) // CSRF 토큰 추가
			.andExpect(status().isOk())
			.andExpect(content().contentType("application/json"))
			.andExpect(jsonPath("$.message").value("로그아웃 성공"));
	}
}
