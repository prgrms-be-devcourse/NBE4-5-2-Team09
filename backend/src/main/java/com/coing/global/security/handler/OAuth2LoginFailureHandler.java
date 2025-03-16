package com.coing.global.security.handler;

import java.io.IOException;

import com.coing.util.MessageUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

	@Value("${next.server.url}")
	private String frontUrl;

	private final MessageUtil messageUtil;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException exception) throws IOException {

		String redirectUrl = UriComponentsBuilder.fromUriString(frontUrl + "/user/social-login")
			.queryParam("error", messageUtil.resolveMessage("login.failure"))
			.toUriString();

		response.sendRedirect(redirectUrl);
	}
}
