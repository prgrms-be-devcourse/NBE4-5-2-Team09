package com.coing.domain.user.controller;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coing.domain.user.CustomUserPrincipal;
import com.coing.domain.user.controller.dto.EmailVerificationResponse;
import com.coing.domain.user.controller.dto.UserLoginRequest;
import com.coing.domain.user.controller.dto.UserResponse;
import com.coing.domain.user.controller.dto.UserSignUpRequest;
import com.coing.domain.user.controller.dto.UserSignupResponse;
import com.coing.domain.user.email.service.EmailVerificationService;
import com.coing.domain.user.entity.User;
import com.coing.domain.user.repository.UserRepository;
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
	private final UserRepository userRepository;
	private final AuthTokenService authTokenService;
	private final EmailVerificationService emailVerificationService;
	private final MessageUtil messageUtil;

	@Operation(summary = "일반 유저 회원 가입")
	@PostMapping("/signup")
	public ResponseEntity<UserSignupResponse> signUp(@RequestBody @Validated UserSignUpRequest request,
		HttpServletResponse response) {
		UserResponse user = userService.join(request);
		UserSignupResponse signupResponse = new UserSignupResponse(
			"회원가입 성공. 인증 이메일 전송 완료.",
			user.name(),
			user.email(),
			user.id()
		);
		return ResponseEntity.status(HttpStatus.CREATED).body(signupResponse);
	}

	@Operation(summary = "이메일 인증")
	@GetMapping("/verify-email")
	public ResponseEntity<?> verifyEmail(@RequestParam(name = "token") String token) {
		Map<String, Object> claims = authTokenService.verifyToken(token);
		if (claims == null || claims.get("id") == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(Map.of("status", "error", "message", "유효하지 않은 토큰입니다."));
		}
		UUID userId = UUID.fromString(claims.get("id").toString());
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(messageUtil.resolveMessage("member.not.found"),
				HttpStatus.BAD_REQUEST, ""));

		if (user.isVerified()) {
			return ResponseEntity.status(HttpStatus.OK)
				.body(Map.of("status", "already", "message", "이미 인증되었습니다."));
		}

		emailVerificationService.verifyEmail(userId);
		return ResponseEntity.ok(Map.of("status", "success", "message", "이메일 인증이 완료되었습니다."));
	}

	@Operation(summary = "이메일 인증 상태 확인", description = "회원가입을 요청한 사용자의 UUID(userId)를 기준으로 이메일 인증 여부를 확인합니다.")
	@GetMapping("/is-verified")
	public ResponseEntity<EmailVerificationResponse> isVerified(@RequestParam(name = "userId") UUID userId) {
		Optional<User> userOpt = userRepository.findById(userId);
		if (userOpt.isEmpty()) {
			throw new BusinessException(messageUtil.resolveMessage("member.not.found"), HttpStatus.BAD_REQUEST, "");
		}
		User user = userOpt.get();
		boolean verified = user.isVerified();
		EmailVerificationResponse response = new EmailVerificationResponse(verified);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "이메일 인증 메일 재전송")
	@PostMapping("/resend-email")
	public ResponseEntity<?> resendEmail(@RequestParam(name = "userId") UUID userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(
				messageUtil.resolveMessage("member.not.found"), HttpStatus.BAD_REQUEST, ""
			));

		if (user.isVerified()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(Map.of("status", "already_verified", "message", "이미 인증된 사용자입니다."));
		}

		try {
			emailVerificationService.resendVerificationEmail(userId);
			return ResponseEntity.ok(Map.of("status", "success", "message", "이메일이 재전송되었습니다."));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of("status", "error", "message", "이메일 재전송에 실패했습니다."));
		}
	}

	@Operation(summary = "일반 유저 로그인")
	@PostMapping("/login")
	public ResponseEntity<BasicResponse> login(@RequestBody @Validated UserLoginRequest request,
		HttpServletResponse response) {
		UserResponse user = userService.login(request.email(), request.password());
		if (!user.verified()) {
			throw new BusinessException("이메일 인증이 완료되지 않았습니다.", HttpStatus.UNAUTHORIZED, "");
		}
		String accessToken = authTokenService.genAccessToken(user);
		String refreshToken = authTokenService.genRefreshToken(user);
		Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
		refreshCookie.setHttpOnly(true);
		refreshCookie.setSecure(false);
		refreshCookie.setPath("/");
		refreshCookie.setMaxAge(604800);
		response.addCookie(refreshCookie);
		response.setHeader("Authorization", "Bearer " + accessToken);
		BasicResponse basicResponse = new BasicResponse(HttpStatus.OK, "로그인 성공", "");
		return ResponseEntity.ok(basicResponse);
	}

	@Operation(summary = "토큰 재발급")
	@PostMapping("/refresh")
	public ResponseEntity<BasicResponse> refreshToken(HttpServletRequest request,
		HttpServletResponse response) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null || cookies.length == 0) {
			// 토큰이 없는 경우 403 처리
			throw new BusinessException(messageUtil.resolveMessage("refresh.token.required"), HttpStatus.FORBIDDEN, "");
		}
		String refreshToken = null;
		for (Cookie cookie : cookies) {
			if ("refreshToken".equals(cookie.getName())) {
				refreshToken = cookie.getValue();
				break;
			}
		}
		if (refreshToken == null || refreshToken.trim().isEmpty()) {
			throw new BusinessException(messageUtil.resolveMessage("invalid.refresh.token"), HttpStatus.FORBIDDEN, "");
		}
		Map<String, Object> claims = authTokenService.verifyToken(refreshToken);
		if (claims == null || claims.get("id") == null) {
			throw new BusinessException(messageUtil.resolveMessage("invalid.refresh.token"), HttpStatus.FORBIDDEN, "");
		}
		UUID id = UUID.fromString(claims.get("id").toString());
		UserResponse user = userService.findById(id);
		if (user == null) {
			throw new BusinessException(messageUtil.resolveMessage("member.not.found"), HttpStatus.BAD_REQUEST, "");
		}
		String newAccessToken = authTokenService.genAccessToken(user);
		String newRefreshToken = authTokenService.genRefreshToken(user);
		Cookie newRefreshCookie = new Cookie("refreshToken", newRefreshToken);
		newRefreshCookie.setHttpOnly(true);
		newRefreshCookie.setSecure(false);
		newRefreshCookie.setPath("/");
		newRefreshCookie.setMaxAge(604800);
		response.addCookie(newRefreshCookie);
		response.setHeader("Authorization", "Bearer " + newAccessToken);
		return ResponseEntity.ok(new BasicResponse(HttpStatus.OK, "토큰 재발급 성공", ""));
	}

	@Operation(summary = "회원 로그아웃", security = @SecurityRequirement(name = "bearerAuth"))
	@PostMapping("/logout")
	public ResponseEntity<BasicResponse> logout(HttpServletResponse response,
		@AuthenticationPrincipal CustomUserPrincipal principal) {
		if (principal == null) {
			throw new BusinessException(messageUtil.resolveMessage("empty.token.provided"), HttpStatus.FORBIDDEN, "");
		}
		Cookie cookie = new Cookie("refreshToken", null);
		cookie.setHttpOnly(true);
		cookie.setSecure(false);
		cookie.setPath("/");
		cookie.setMaxAge(0);
		response.addCookie(cookie);
		return ResponseEntity.ok(new BasicResponse(HttpStatus.OK, "로그아웃 성공", "userEmail: " + principal.email()));
	}

	@Operation(summary = "회원 탈퇴", security = @SecurityRequirement(name = "bearerAuth"))
	@DeleteMapping("/signout")
	public ResponseEntity<?> signOut(@Validated UserLoginRequest request,
		@org.springframework.security.core.annotation.AuthenticationPrincipal CustomUserPrincipal principal) {
		if (principal == null) {
			throw new BusinessException(messageUtil.resolveMessage("empty.token.provided"), HttpStatus.FORBIDDEN, "");
		}
		if (!principal.email().equals(request.email())) {
			throw new BusinessException(messageUtil.resolveMessage("token.email.mismatch"), HttpStatus.FORBIDDEN, "");
		}
		userService.quit(request.email(), request.password());
		return ResponseEntity.ok("회원 탈퇴 성공");
	}
}
