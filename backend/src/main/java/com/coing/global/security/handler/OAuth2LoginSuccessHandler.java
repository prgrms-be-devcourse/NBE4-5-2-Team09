package com.coing.global.security.handler;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.coing.domain.user.dto.CustomOAuth2User;
import com.coing.domain.user.dto.CustomUserPrincipal;
import com.coing.domain.user.service.AuthTokenService;

import jakarta.servlet.http.Cookie;
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
		Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
		refreshCookie.setHttpOnly(true);
		refreshCookie.setSecure(false);
		refreshCookie.setPath("/");
		refreshCookie.setMaxAge(604800);
		response.addCookie(refreshCookie);
		response.setHeader("Authorization", "Bearer " + accessToken);
		response.sendRedirect(frontUrl + "/user/social-login/callback");
	}
}
