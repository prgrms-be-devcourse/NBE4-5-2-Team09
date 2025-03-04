package com.coing.domain.user.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coing.domain.user.controller.dto.UserLoginRequest;
import com.coing.domain.user.controller.dto.UserResponse;
import com.coing.domain.user.controller.dto.UserSignUpRequest;
import com.coing.domain.user.service.AuthTokenService;
import com.coing.domain.user.service.UserService;
import com.coing.global.exception.BusinessException;
import com.coing.util.BasicResponse;
import com.coing.util.MessageUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
		try {
			UserResponse user = userService.join(
				request.name(),
				request.email(),
				request.password(),
				request.passwordConfirm()
			);
			BasicResponse response = new BasicResponse(
				HttpStatus.CREATED,
				"회원가입 성공",
				"userId: " + user.id() + ", name: " + user.name() + ", email: " + user.email()
			);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} catch (Exception e) {
			String errorMessage = messageUtil.resolveMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new BasicResponse(HttpStatus.INTERNAL_SERVER_ERROR, "회원가입 실패", errorMessage));
		}
	}

	@Operation(summary = "일반 유저 로그인")
	@PostMapping("/login")
	public ResponseEntity<BasicResponse> login(@RequestBody @Validated UserLoginRequest request,
		HttpServletResponse response) {
		try {
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
		} catch (Exception e) {
			String errorMessage = messageUtil.resolveMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new BasicResponse(HttpStatus.INTERNAL_SERVER_ERROR, "로그인 실패", errorMessage));
		}
	}

	@Operation(summary = "회원 로그아웃")
	@PostMapping("/logout")
	public ResponseEntity<BasicResponse> logout(HttpServletResponse response) {
		try {
			Cookie cookie = new Cookie("refreshToken", null);
			cookie.setHttpOnly(true);
			cookie.setSecure(false);
			cookie.setPath("/");
			cookie.setMaxAge(0);
			response.addCookie(cookie);
			BasicResponse basicResponse = new BasicResponse(HttpStatus.OK, "로그아웃 성공", "");
			return ResponseEntity.ok(basicResponse);
		} catch (Exception e) {
			String errorMessage = messageUtil.resolveMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new BasicResponse(HttpStatus.INTERNAL_SERVER_ERROR, "로그아웃 실패", errorMessage));
		}
	}

	@Operation(summary = "토큰 재발급")
	@PostMapping("/refresh")
	public ResponseEntity<BasicResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
		try {
			Cookie[] cookies = request.getCookies();
			if (cookies == null) {
				throw new BusinessException(messageUtil.resolveMessage("refresh.token.required"),
					HttpStatus.BAD_REQUEST, "");
			}
			String refreshToken = null;
			for (Cookie cookie : cookies) {
				if ("refreshToken".equals(cookie.getName())) {
					refreshToken = cookie.getValue();
					break;
				}
			}
			if (refreshToken == null) {
				throw new BusinessException(messageUtil.resolveMessage("refresh.token.required"),
					HttpStatus.BAD_REQUEST, "");
			}

			Map<String, Object> claims = authTokenService.verifyToken(refreshToken);
			if (claims == null) {
				throw new BusinessException(messageUtil.resolveMessage("invalid.refresh.token"), HttpStatus.BAD_REQUEST,
					"");
			}

			Long userId = ((Number)claims.get("id")).longValue();
			String email = (String)claims.get("email");

			// 이름은 필요 없으므로 빈 문자열 처리
			UserResponse userResponse = new UserResponse(userId, "", email);

			String newAccessToken = authTokenService.genAccessToken(userResponse);
			String newRefreshToken = authTokenService.genRefreshToken(userResponse);

			Cookie newRefreshCookie = new Cookie("refreshToken", newRefreshToken);
			newRefreshCookie.setHttpOnly(true);
			newRefreshCookie.setSecure(false);
			newRefreshCookie.setPath("/");
			newRefreshCookie.setMaxAge(604800); // 7일 (초 단위)
			response.addCookie(newRefreshCookie);

			Map<String, String> res = new HashMap<>();
			res.put("token", newAccessToken);
			res.put("email", email);
			BasicResponse basicResponse = new BasicResponse(HttpStatus.OK, "토큰 재발급 성공", res.toString());
			return ResponseEntity.ok(basicResponse);
		} catch (Exception e) {
			String errorMessage = messageUtil.resolveMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new BasicResponse(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 재발급 실패", errorMessage));
		}
	}

	@Operation(summary = "회원 탈퇴")
	@DeleteMapping("/signout")
	public ResponseEntity<?> signOut(
		@RequestBody @Validated UserLoginRequest request,
		@Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authHeader) {

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			throw new BusinessException(messageUtil.resolveMessage("empty.token.provided"), HttpStatus.UNAUTHORIZED,
				"");
		}

		String accessToken = authHeader.substring("Bearer ".length());
		Map<String, Object> claims = authTokenService.verifyToken(accessToken);
		if (claims == null || claims.get("email") == null) {
			throw new BusinessException(messageUtil.resolveMessage("invalid.token"), HttpStatus.UNAUTHORIZED, "");
		}

		String tokenEmail = (String)claims.get("email");
		if (!tokenEmail.equals(request.email())) {
			throw new BusinessException(messageUtil.resolveMessage("token.email.mismatch"), HttpStatus.UNAUTHORIZED,
				"");
		}

		// UserService를 사용하여 회원 탈퇴 처리
		userService.quit(request.email(), request.password());
		return ResponseEntity.ok("회원 탈퇴 성공");
	}
}
