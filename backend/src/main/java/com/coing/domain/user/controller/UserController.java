package com.coing.domain.user.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coing.domain.user.CustomUserPrincipal;
import com.coing.domain.user.controller.dto.UserLoginRequest;
import com.coing.domain.user.controller.dto.UserResponse;
import com.coing.domain.user.controller.dto.UserSignUpRequest;
import com.coing.domain.user.service.AuthTokenService;
import com.coing.domain.user.service.UserService;
import com.coing.global.exception.BusinessException;
import com.coing.util.BasicResponse;
import com.coing.util.MessageUtil;

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
	private final MessageUtil messageUtil; // 메시지 해석용 유틸리티

	@Operation(summary = "일반 유저 회원 가입")
	@PostMapping("/signup")
	public ResponseEntity<BasicResponse> signUp(@RequestBody @Validated UserSignUpRequest request) {
		UserResponse user = userService.join(
			request.name(),
			request.email(),
			request.password(),
			request.passwordConfirm()
		);
		BasicResponse response = new BasicResponse(
			HttpStatus.CREATED,
			"회원가입 성공",
			"name: " + user.name() + ", email: " + user.email()
		);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Operation(summary = "일반 유저 로그인")
	@PostMapping("/login")
	public ResponseEntity<BasicResponse> login(@RequestBody @Validated UserLoginRequest request,
		HttpServletResponse response) {
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
		BasicResponse basicResponse = new BasicResponse(HttpStatus.OK, "로그인 성공", res.toString());
		return ResponseEntity.ok(basicResponse);
	}

	@Operation(summary = "회원 로그아웃")
	@PostMapping("/logout")
	public ResponseEntity<BasicResponse> logout(HttpServletResponse response,
		@AuthenticationPrincipal CustomUserPrincipal principal) {
		if (principal == null) {
			throw new BusinessException(messageUtil.resolveMessage("empty.token.provided"), HttpStatus.UNAUTHORIZED,
				"");
		}

		Cookie cookie = new Cookie("refreshToken", null);
		cookie.setHttpOnly(true);
		cookie.setSecure(false);
		cookie.setPath("/");
		cookie.setMaxAge(0);
		response.addCookie(cookie);

		return ResponseEntity.ok(new BasicResponse(HttpStatus.OK, "로그아웃 성공", "userEmail: " + principal.email()));
	}

	@Operation(summary = "토큰 재발급")
	@PostMapping("/refresh")
	public ResponseEntity<BasicResponse> refreshToken(HttpServletRequest request, HttpServletResponse response,
		@AuthenticationPrincipal CustomUserPrincipal principal) {
		if (principal == null) {
			throw new BusinessException(messageUtil.resolveMessage("empty.token.provided"), HttpStatus.UNAUTHORIZED,
				"");
		}

		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			throw new BusinessException(messageUtil.resolveMessage("refresh.token.required"), HttpStatus.BAD_REQUEST,
				"");
		}

		String refreshToken = null;
		for (Cookie cookie : cookies) {
			if ("refreshToken".equals(cookie.getName())) {
				refreshToken = cookie.getValue();
				break;
			}
		}

		if (refreshToken == null) {
			throw new BusinessException(messageUtil.resolveMessage("invalid.refresh.token"), HttpStatus.BAD_REQUEST,
				"");
		}

		String newAccessToken = authTokenService.genAccessToken(principal);
		String newRefreshToken = authTokenService.genRefreshToken(principal);

		Cookie newRefreshCookie = new Cookie("refreshToken", newRefreshToken);
		newRefreshCookie.setHttpOnly(true);
		newRefreshCookie.setSecure(false);
		newRefreshCookie.setPath("/");
		newRefreshCookie.setMaxAge(604800); // 7일 (초 단위)
		response.addCookie(newRefreshCookie);

		Map<String, String> res = new HashMap<>();
		res.put("token", newAccessToken);
		res.put("email", principal.email());
		return ResponseEntity.ok(new BasicResponse(HttpStatus.OK, "토큰 재발급 성공", res.toString()));
	}

	@Operation(summary = "회원 탈퇴")
	@DeleteMapping("/signout")
	public ResponseEntity<?> signOut(@RequestBody @Validated UserLoginRequest request,
		@AuthenticationPrincipal CustomUserPrincipal principal) {

		if (principal == null) {
			throw new BusinessException(messageUtil.resolveMessage("empty.token.provided"), HttpStatus.UNAUTHORIZED,
				"");
		}

		if (!principal.email().equals(request.email())) {
			throw new BusinessException(messageUtil.resolveMessage("token.email.mismatch"), HttpStatus.UNAUTHORIZED,
				"");
		}

		userService.quit(request.email(), request.password());
		return ResponseEntity.ok("회원 탈퇴 성공");
	}
}
