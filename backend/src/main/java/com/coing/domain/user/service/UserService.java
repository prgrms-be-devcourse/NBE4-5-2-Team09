package com.coing.domain.user.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coing.domain.user.controller.dto.UserResponse;
import com.coing.domain.user.controller.dto.UserSignUpRequest;
import com.coing.domain.user.email.service.EmailVerificationService;
import com.coing.domain.user.entity.Provider;
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

		// 비밀번호와 비밀번호 확인 일치 여부를 먼저 검사합니다.
		if (!request.password().equals(request.passwordConfirm())) {
			throw new BusinessException(
				messageUtil.resolveMessage("invalid.password.confirm"),
				HttpStatus.BAD_REQUEST,
				""
			);
		}

		Optional<User> existing = userRepository.findByEmail(request.email());
		if (existing.isPresent()) {
			throw new BusinessException(
				messageUtil.resolveMessage("already.registered.email"),
				HttpStatus.BAD_REQUEST,
				""
			);
		}

		String encodedPassword = passwordEncoder.encode(request.password());
		User userEntity = User.builder()
			.name(request.name())
			.email(request.email())
			.password(encodedPassword)
			.provider(Provider.EMAIL)
			.build();

		User savedUser = userRepository.save(userEntity);

		// 이메일 인증 메일 전송 실패 시 예외 전파 (회원가입 전체 롤백)
		emailVerificationService.sendVerificationEmail(savedUser);

		return UserResponse.from(savedUser);
	}

	@Transactional(readOnly = true)
	public UserResponse login(String email, String password) {
		log.info("로그인 시도: {}", email);
		Optional<User> optionalUser = userRepository.findByEmail(email);

		User user = validateUser(password, optionalUser);

		// 이메일 인증 여부 확인 추가
		if (!user.isVerified()) {
			// 세 번째 인자로 user.getId().toString()을 전달하여 프론트에서 사용할 수 있도록 함
			throw new BusinessException(messageUtil.resolveMessage("email.not.verified"),
				HttpStatus.UNAUTHORIZED, user.getId().toString());
		}
		return UserResponse.from(user);
	}

	@Transactional
	public void quit(UUID id, String password) {
		Optional<User> optionalUser = userRepository.findById(id);
		User user = validateUser(password, optionalUser);
		userRepository.delete(user);
		log.info("회원 탈퇴 성공: {}", id);
	}

	@Transactional(readOnly = true)
	public UserResponse findById(UUID id) {
		return userRepository.findById(id)
			.map(UserResponse::from)
			.orElseThrow(() -> new BusinessException(messageUtil.resolveMessage("member.not.found"),
				HttpStatus.BAD_REQUEST, ""));
	}

	// 비밀번호 재설정 메서드
	@Transactional
	public void updatePassword(UUID userId, String newPassword) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(messageUtil.resolveMessage("member.not.found"),
				HttpStatus.BAD_REQUEST, ""));
		String encodedPassword = passwordEncoder.encode(newPassword);
		User updatedUser = user.withPassword(encodedPassword);
		userRepository.save(updatedUser);
		log.info("비밀번호 재설정 성공: {}", userId);
	}

	// 하루에 한번 실행하는 미인증 유저 삭제 스케줄러
	// 매일 새벽 5시에 실행 (cron: 초, 분, 시, 일, 월, 요일)
	@Scheduled(cron = "00 00 5 * * *")
	@Transactional
	public void cleanupUnverifiedUsers() {
		// 현재 시각에서 1주일을 뺀 시간보다 가입된 사용자는 삭제 대상
		LocalDateTime threshold = LocalDateTime.now().minusWeeks(1);
		int deletedCount = userRepository.deleteUnverifiedUsers(threshold);
		if (deletedCount > 0) {
			log.info("삭제된 인증 미완료 사용자 수: {}", deletedCount);
		}
	}

	private User validateUser(String password, Optional<User> optionalUser) {

		if (optionalUser.isEmpty()) {
			throw new BusinessException(messageUtil.resolveMessage("member.not.found"),
				HttpStatus.BAD_REQUEST, "");
		}
		User user = optionalUser.get();
		if (!passwordEncoder.matches(password, user.getPassword())) {
			throw new BusinessException(messageUtil.resolveMessage("password.mismatch"),
				HttpStatus.BAD_REQUEST, "");
		}

		return user;
	}
}
