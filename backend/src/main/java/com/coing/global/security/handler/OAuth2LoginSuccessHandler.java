package com.coing.global.security.handler;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.coing.domain.user.dto.CustomOAuth2User;
import com.coing.domain.user.dto.CustomUserPrincipal;
import com.coing.domain.user.service.AuthTokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

	@Value("${next.server.url}")
	private String frontUrl;

	private final AuthTokenService authTokenService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		CustomOAuth2User oAuth2User = (CustomOAuth2User)authentication.getPrincipal();
		CustomUserPrincipal user = new CustomUserPrincipal(oAuth2User.getUser().getId());

		String accessToken = authTokenService.genAccessToken(user);
		String refreshToken = authTokenService.genRefreshToken(user);

		// ResponseCookie로 refresh 토큰 쿠키 생성
		ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
			.httpOnly(true)
			.secure(true)
			.path("/")
			.maxAge(604800) // 7일
			.sameSite("None")
			.build();

		// 쿠키 및 Authorization 헤더 설정 후 리다이렉트
		response.setHeader("Set-Cookie", refreshCookie.toString());
		response.setHeader("Authorization", "Bearer " + accessToken);
		response.sendRedirect(frontUrl + "/user/social-login/callback");
	}
}
