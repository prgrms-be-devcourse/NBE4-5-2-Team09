package com.coing.domain.user.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coing.domain.user.entity.User;
import com.coing.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public User join(String name, String email, String password, String passwordConfirm) {

		log.info("일반 회원 가입 시도 :{}", email);

		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("name.required");
		}

		if (email == null || email.trim().isEmpty()) {
			throw new IllegalArgumentException("email.required");
		}

		if (password == null || password.trim().isEmpty()) {
			throw new IllegalArgumentException("password.required");
		}

		if (passwordConfirm == null || passwordConfirm.trim().isEmpty()) {
			throw new IllegalArgumentException("password.confirm.required");
		}

		if (!password.equals(passwordConfirm)) {
			throw new IllegalArgumentException("password.mismatch");
		}

		Optional<User> existing = userRepository.findByEmail(email);
		if (existing.isPresent()) {
			throw new IllegalArgumentException("already.registered.email");
		}

		String encodedPassword = passwordEncoder.encode(password);
		User user = User.builder()
			.name(name)
			.email(email)
			.password(encodedPassword)
			.build();

		user = userRepository.save(user);

		return user;
	}

	@Transactional(readOnly = true)
	public User login(String email, String password) {

		log.info("회원 로그인 시도 :{}", email);

		Optional<User> optionalUser = userRepository.findByEmail(email);
		if (optionalUser.isEmpty()) {
			throw new IllegalArgumentException("member.not.found");
		}

		User user = optionalUser.get();

		if (!passwordEncoder.matches(password, user.getPassword())) {
			throw new IllegalArgumentException("password.mismatch");
		}
		
		return user;
	}
}
