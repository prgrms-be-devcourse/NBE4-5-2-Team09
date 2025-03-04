package com.coing.domain.user.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coing.domain.user.controller.dto.UserLoginRequest;
import com.coing.domain.user.controller.dto.UserResponse;
import com.coing.domain.user.controller.dto.UserSignUpRequest;
import com.coing.domain.user.service.AuthTokenService;
import com.coing.domain.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "User API", description = "회원 관련 API 엔드포인트")
public class UserController {

	private final UserService userService;
	private final AuthTokenService authTokenService;

	@Operation(summary = "일반 유저 회원 가입")
	@PostMapping("/signup")
	public ResponseEntity<?> signUp(@RequestBody @Validated UserSignUpRequest request) {

		UserResponse user = userService.join(
			request.name(),
			request.email(),
			request.password(),
			request.passwordConfirm()
		);
		return ResponseEntity.status(HttpStatus.CREATED).body(user);
	}

	@Operation(summary = "일반 유저 로그인")
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody @Validated UserLoginRequest request, HttpServletResponse response) {
		UserResponse user = userService.login(request.email(), request.password());
		String accessToken = authTokenService.genAccessToken(user);
		String refreshToken = authTokenService.genRefreshToken(user);

		Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
		refreshCookie.setHttpOnly(true);
		refreshCookie.setSecure(true);
		refreshCookie.setPath("/");
		refreshCookie.setMaxAge(604800); // 7일
		response.addCookie(refreshCookie);

		Map<String, String> res = new HashMap<>();
		res.put("token", accessToken);
		res.put("email", request.email());
		return ResponseEntity.ok(res);
	}

	@Operation(summary = "회원 로그아웃")
	@PostMapping("/logout")
	public ResponseEntity<?> logout(HttpServletResponse response) {
		Cookie cookie = new Cookie("refreshToken", null);
		cookie.setHttpOnly(true);
		cookie.setSecure(false);
		cookie.setPath("/");
		cookie.setMaxAge(0);
		response.addCookie(cookie);
		return ResponseEntity.ok("로그아웃 성공");
	}

	@Operation(summary = "토큰 재발급")
	@PostMapping("/refresh")
	public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
		// 쿠키에서 refreshToken 값 추출
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			throw new IllegalArgumentException("refresh.token.required");
		}
		String refreshToken = null;
		for (Cookie cookie : cookies) {
			if ("refreshToken".equals(cookie.getName())) {
				refreshToken = cookie.getValue();
				break;
			}
		}
		if (refreshToken == null) {
			throw new IllegalArgumentException("refresh.token.required");
		}

		// 리프레시 토큰 검증
		Map<String, Object> claims = authTokenService.verifyToken(refreshToken);
		if (claims == null) {
			throw new IllegalArgumentException("invalid.refresh.token");
		}

		// 토큰 클레임에서 사용자 정보 추출
		Long userId = ((Number)claims.get("id")).longValue();
		String email = (String)claims.get("email");

		UserResponse userResponse = new UserResponse(userId, "", email);

		// 새 액세스 토큰과 리프레시 토큰 생성
		String newAccessToken = authTokenService.genAccessToken(userResponse);
		String newRefreshToken = authTokenService.genRefreshToken(userResponse);

		// 새 리프레시 토큰을 쿠키에 설정 (7일 유효)
		Cookie newRefreshCookie = new Cookie("refreshToken", newRefreshToken);
		newRefreshCookie.setHttpOnly(true);
		newRefreshCookie.setSecure(false);
		newRefreshCookie.setPath("/");
		newRefreshCookie.setMaxAge(604800); // 7일 (초 단위)
		response.addCookie(newRefreshCookie);

		Map<String, String> res = new HashMap<>();
		res.put("token", newAccessToken);
		res.put("email", email);

		return ResponseEntity.ok(res);
	}

}
