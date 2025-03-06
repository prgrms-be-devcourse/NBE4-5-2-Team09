package com.coing.domain.user.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import com.coing.domain.user.CustomUserPrincipal;
import com.coing.domain.user.service.AuthTokenService;
import com.coing.domain.user.service.UserService;
import com.coing.util.MessageUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
class UserControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@MockBean
	UserService userService;

	@MockBean
	MessageUtil messageUtil;

	@MockBean
	AuthTokenService authTokenService;

	@Test
	@DisplayName("로그아웃 성공")
	void t1() throws Exception {
		// 1) CustomUserPrincipal 생성
		var principal = new CustomUserPrincipal(
			UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
			"tester@example.com"
		);

		// 2) Authentication 객체 생성 (권한 ROLE_USER 포함)
		var auth = new UsernamePasswordAuthenticationToken(
			principal,
			null,
			List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);

		// 3) MockMvc 요청에 .with(authentication(auth))를 사용하여 인증 정보 주입
		mockMvc.perform(post("/api/auth/logout")
				.with(csrf())
				.with(authentication(auth))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("로그아웃 성공"))
			.andExpect(jsonPath("$.detail").value("userEmail: tester@example.com"));
	}
}