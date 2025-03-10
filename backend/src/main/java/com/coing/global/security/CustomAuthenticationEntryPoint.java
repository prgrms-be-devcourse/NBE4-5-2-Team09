package com.coing.global.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// 액세스 토큰 만료 시 403 Forbidden 응답을 반환하는 클래스
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException authException) throws IOException, ServletException {
		// 403 응답을 반환하고 에러 메시지 작성
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.getWriter().write("Forbidden: " + authException.getMessage());
	}
}
