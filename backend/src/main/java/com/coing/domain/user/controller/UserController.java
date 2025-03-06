package com.coing.domain.user.controller;

import java.util.Map;
import java.util.UUID;

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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

		// 리프레시 토큰을 HttpOnly 쿠키에 설정
		Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
		refreshCookie.setHttpOnly(true);
		refreshCookie.setSecure(false); // 개발 환경
		refreshCookie.setPath("/");
		refreshCookie.setMaxAge(604800); // 7일
		response.addCookie(refreshCookie);

		// 액세스 토큰을 응답 헤더에 추가 (Bearer 스킴 포함)
		response.setHeader("Authorization", "Bearer " + accessToken);

		BasicResponse basicResponse = new BasicResponse(HttpStatus.OK, "로그인 성공", "");
		return ResponseEntity.ok(basicResponse);
	}

	@Operation(summary = "회원 로그아웃", security = @SecurityRequirement(name = "bearerAuth"))
	@PostMapping("/logout")
	public ResponseEntity<BasicResponse> logout(HttpServletResponse response,
		@AuthenticationPrincipal CustomUserPrincipal principal) {
		if (principal == null) {
			throw new BusinessException(messageUtil.resolveMessage("empty.token.provided"), HttpStatus.UNAUTHORIZED,
				"");
		}

		Cookie cookie = new Cookie("refreshToken", null);
		cookie.setHttpOnly(true);
		cookie.setSecure(false); // 개발 환경
		cookie.setPath("/");
		cookie.setMaxAge(0);
		response.addCookie(cookie);

		return ResponseEntity.ok(new BasicResponse(HttpStatus.OK, "로그아웃 성공", "userEmail: " + principal.email()));
	}

	@Operation(summary = "토큰 재발급")
	@PostMapping("/refresh")
	public ResponseEntity<BasicResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null || cookies.length == 0) {
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

		if (refreshToken == null || refreshToken.trim().isEmpty()) {
			throw new BusinessException(messageUtil.resolveMessage("invalid.refresh.token"), HttpStatus.BAD_REQUEST,
				"");
		}

		// refresh 토큰 검증: verifyToken이 null을 반환하면 토큰이 유효하지 않음을 의미합니다.
		Map<String, Object> claims = authTokenService.verifyToken(refreshToken);
		if (claims == null || claims.get("id") == null) {
			throw new BusinessException(messageUtil.resolveMessage("invalid.refresh.token"), HttpStatus.BAD_REQUEST,
				"");
		}

		UUID id = UUID.fromString(claims.get("id").toString());
		UserResponse user = userService.findById(id);
		if (user == null) {
			throw new BusinessException(messageUtil.resolveMessage("member.not.found"), HttpStatus.BAD_REQUEST, "");
		}

		String newAccessToken = authTokenService.genAccessToken(user);
		String newRefreshToken = authTokenService.genRefreshToken(user);

		// 새로운 리프레시 토큰을 쿠키에 설정
		Cookie newRefreshCookie = new Cookie("refreshToken", newRefreshToken);
		newRefreshCookie.setHttpOnly(true);
		newRefreshCookie.setSecure(false); // 개발 환경, 운영 시 true로 설정
		newRefreshCookie.setPath("/");
		newRefreshCookie.setMaxAge(604800); // 7일
		response.addCookie(newRefreshCookie);

		// 새 액세스 토큰을 응답 헤더에 추가
		response.setHeader("Authorization", "Bearer " + newAccessToken);

		return ResponseEntity.ok(new BasicResponse(HttpStatus.OK, "토큰 재발급 성공", ""));
	}

	@Operation(summary = "회원 탈퇴", security = @SecurityRequirement(name = "bearerAuth"))
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
