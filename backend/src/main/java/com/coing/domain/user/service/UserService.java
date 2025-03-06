package com.coing.domain.user.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coing.domain.user.controller.dto.UserResponse;
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
	private final MessageUtil messageUtil; // MessageUtil 주입

	@Transactional
	public UserResponse join(String name, String email, String password, String passwordConfirm) {
		log.info("일반 회원 가입 시도 :{}", email);

		if (name == null || name.trim().isEmpty()) {
			throw new BusinessException(messageUtil.resolveMessage("name.required"), HttpStatus.BAD_REQUEST, "");
		}
		if (email == null || email.trim().isEmpty()) {
			throw new BusinessException(messageUtil.resolveMessage("email.required"), HttpStatus.BAD_REQUEST, "");
		}
		if (password == null || password.trim().isEmpty()) {
			throw new BusinessException(messageUtil.resolveMessage("password.required"), HttpStatus.BAD_REQUEST, "");
		}
		if (passwordConfirm == null || passwordConfirm.trim().isEmpty()) {
			throw new BusinessException(messageUtil.resolveMessage("password.confirm.required"), HttpStatus.BAD_REQUEST,
				"");
		}
		if (!password.equals(passwordConfirm)) {
			throw new BusinessException(messageUtil.resolveMessage("invalid.password.confirm"), HttpStatus.BAD_REQUEST,
				"");
		}

		Optional<User> existing = userRepository.findByEmail(email);
		if (existing.isPresent()) {
			throw new BusinessException(messageUtil.resolveMessage("already.registered.email"), HttpStatus.BAD_REQUEST,
				"");
		}

		String encodedPassword = passwordEncoder.encode(password);
		User user = User.builder()
			.name(name)
			.email(email)
			.password(encodedPassword)
			.build();

		User savedUser = userRepository.save(user);
		return new UserResponse(savedUser.getId(), savedUser.getName(), savedUser.getEmail());
	}

	@Transactional(readOnly = true)
	public UserResponse login(String email, String password) {
		log.info("회원 로그인 시도 :{}", email);

		Optional<User> optionalUser = userRepository.findByEmail(email);
		if (optionalUser.isEmpty()) {
			throw new BusinessException(messageUtil.resolveMessage("member.not.found"), HttpStatus.BAD_REQUEST, "");
		}

		User user = optionalUser.get();

		if (!passwordEncoder.matches(password, user.getPassword())) {
			throw new BusinessException(messageUtil.resolveMessage("password.mismatch"), HttpStatus.BAD_REQUEST, "");
		}

		return new UserResponse(user.getId(), user.getName(), user.getEmail());
	}

	@Transactional
	public void quit(String email, String password) {
		Optional<User> optionalUser = userRepository.findByEmail(email);
		if (optionalUser.isEmpty()) {
			throw new BusinessException(messageUtil.resolveMessage("member.not.found"), HttpStatus.BAD_REQUEST, "");
		}
		User user = optionalUser.get();
		if (!passwordEncoder.matches(password, user.getPassword())) {
			throw new BusinessException(messageUtil.resolveMessage("password.mismatch"), HttpStatus.BAD_REQUEST, "");

		}
		userRepository.delete(user);
		log.info("회원 탈퇴 성공: {}", email);
	}

	public UserResponse findById(UUID id) {
		return userRepository.findById(id)
			.map(user -> new UserResponse(user.getId(), user.getName(), user.getEmail()))
			.orElseThrow(
				() -> new BusinessException(messageUtil.resolveMessage("member.not.found"), HttpStatus.BAD_REQUEST,
					""));
	}

}
