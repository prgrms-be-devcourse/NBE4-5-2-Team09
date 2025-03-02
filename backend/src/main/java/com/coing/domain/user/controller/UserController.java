package com.coing.domain.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coing.domain.user.controller.dto.UserSignUpRequest;
import com.coing.domain.user.entity.User;
import com.coing.domain.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "User API", description = "회원 관련 API 엔드포인트")
public class UserController {

	private final UserService userService;

	@Operation(summary = "일반 유저 회원 가입")
	@PostMapping("/signup")
	public ResponseEntity<?> signUp(@RequestBody @Validated UserSignUpRequest request) {
	
		User user = userService.join(
			request.name(),
			request.email(),
			request.password(),
			request.passwordConfirm()
		);
		return ResponseEntity.status(HttpStatus.CREATED).body(user);
	}
}
