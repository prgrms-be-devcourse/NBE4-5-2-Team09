package com.coing.domain.user.email.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coing.domain.user.entity.User;
import com.coing.domain.user.repository.UserRepository;
import com.coing.global.exception.BusinessException;
import com.coing.util.MessageUtil;
import com.coing.util.Ut;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

	private final UserRepository userRepository;
	private final EmailSenderService emailSenderService;
	private final MessageUtil messageUtil;

	@Value("${custom.jwt.secret-key}")
	private String jwtSecretKey;

	/**
	 * 회원가입 후, 이메일 인증 토큰을 생성하여 이메일 전송하고 DB 업데이트
	 */
	@Transactional
	public void sendVerificationEmail(User user) {
		// 이메일 인증 토큰 생성 (JWT 기반, 만료 10분)
		String token = Ut.AuthTokenUtil.createEmailVerificationToken(jwtSecretKey, user.getId());
		// 회원가입 시, 기존 인증 코드 관련 필드를 업데이트할 필요가 있으면 여기서 수행
		// 예를 들어, 토큰 자체를 DB에 저장할 수도 있으나, 여기서는 링크 기반이므로 별도 저장 없이 처리
		try {
			emailSenderService.sendEmailVerificationMessage(user.getEmail(), token);
			log.info("인증 이메일 전송 성공: {}", user.getEmail());
		} catch (Exception e) {
			log.error("인증 이메일 전송 에러: {}", user.getEmail(), e);
			throw new BusinessException(messageUtil.resolveMessage("mail.send.fail"),
				org.springframework.http.HttpStatus.BAD_REQUEST, "");
		}
	}

	/**
	 * 사용자가 인증 링크를 클릭하면 호출되는 엔드포인트에서 토큰을 검증하고, 이메일 인증 상태를 업데이트
	 */
	@Transactional
	public User verifyEmail(UUID userId) {
		Optional<User> optionalUser = userRepository.findById(userId);
		if (optionalUser.isEmpty()) {
			throw new BusinessException(messageUtil.resolveMessage("user.not.found"),
				org.springframework.http.HttpStatus.BAD_REQUEST, "");
		}
		User user = optionalUser.get();
		// 불변 엔티티 업데이트 (setter 대신 with 메서드 또는 커스텀 변경 메서드 사용)
		User verifiedUser = user.verifyEmail();
		return userRepository.save(verifiedUser);
	}
}
