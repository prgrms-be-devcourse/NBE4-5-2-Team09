package com.coing.global.security.handler;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.coing.domain.user.dto.CustomOAuth2User;
import com.coing.domain.user.service.AuthTokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

	@Value("${next.server.url}")
	private String frontUrl;

	private final AuthTokenService authTokenService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		CustomOAuth2User oAuth2User = (CustomOAuth2User)authentication.getPrincipal();

		String tempToken = UUID.randomUUID().toString();

		String cacheKey = "tempToken:" + tempToken;
		authTokenService.setTempToken(cacheKey, oAuth2User.getUser().getId().toString());

		log.info("[Social Login] Login succeed and issued tempToken");

		String redirectUrl = frontUrl + "/user/social-login/redirect?tempToken=" + tempToken;
		response.sendRedirect(redirectUrl);
	}
}
