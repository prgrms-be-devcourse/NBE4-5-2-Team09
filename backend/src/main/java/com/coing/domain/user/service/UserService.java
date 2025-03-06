package com.coing.domain.user.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coing.domain.user.controller.dto.UserResponse;
import com.coing.domain.user.controller.dto.UserSignUpRequest;
import com.coing.domain.user.email.service.EmailVerificationService;
import com.coing.domain.user.entity.User;
import com.coing.domain.user.repository.UserRepository;
import com.coing.global.exception.BusinessException;
import com.coing.util.MessageUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailVerificationService emailVerificationService;
	private final MessageUtil messageUtil;

	@Transactional
	public UserResponse join(UserSignUpRequest request) {
		log.info("회원가입 시도: {}", request.email());
		Optional<User> existing = userRepository.findByEmail(request.email());
		if (existing.isPresent()) {
			throw new BusinessException(messageUtil.resolveMessage("already.registered.email"),
				HttpStatus.BAD_REQUEST, "");
		}
		String encodedPassword = passwordEncoder.encode(request.password());
		User userEntity = User.builder()
			.name(request.name())
			.email(request.email())
			.password(encodedPassword)
			.build();
		User savedUser = userRepository.save(userEntity);

		// 회원가입 후 자동으로 이메일 인증 메일 전송
		try {
			emailVerificationService.sendVerificationEmail(savedUser);
		} catch (Exception e) {
			log.error("이메일 인증 메일 전송 실패: {}", request.email(), e);
			// 메일 전송 실패 시 가입은 유지하고, 재전송 API 제공 가능
		}
		return new UserResponse(savedUser.getId(), savedUser.getName(), savedUser.getEmail());
	}

	@Transactional(readOnly = true)
	public UserResponse login(String email, String password) {
		log.info("로그인 시도: {}", email);
		Optional<User> optionalUser = userRepository.findByEmail(email);
		if (optionalUser.isEmpty()) {
			throw new BusinessException(messageUtil.resolveMessage("member.not.found"),
				HttpStatus.BAD_REQUEST, "");
		}
		User user = optionalUser.get();
		if (!passwordEncoder.matches(password, user.getPassword())) {
			throw new BusinessException(messageUtil.resolveMessage("password.mismatch"),
				HttpStatus.BAD_REQUEST, "");
		}
		return new UserResponse(user.getId(), user.getName(), user.getEmail());
	}

	@Transactional
	public void quit(String email, String password) {
		Optional<User> optionalUser = userRepository.findByEmail(email);
		if (optionalUser.isEmpty()) {
			throw new BusinessException(messageUtil.resolveMessage("member.not.found"),
				HttpStatus.BAD_REQUEST, "");
		}
		User user = optionalUser.get();
		if (!passwordEncoder.matches(password, user.getPassword())) {
			throw new BusinessException(messageUtil.resolveMessage("password.mismatch"),
				HttpStatus.BAD_REQUEST, "");
		}
		userRepository.delete(user);
		log.info("회원 탈퇴 성공: {}", email);
	}

	@Transactional(readOnly = true)
	public UserResponse findById(UUID id) {
		return userRepository.findById(id)
			.map(u -> new UserResponse(u.getId(), u.getName(), u.getEmail()))
			.orElseThrow(() -> new BusinessException(messageUtil.resolveMessage("member.not.found"),
				HttpStatus.BAD_REQUEST, ""));
	}
}
