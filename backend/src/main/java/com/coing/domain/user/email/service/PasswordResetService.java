package com.coing.domain.user.email.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coing.domain.user.entity.User;
import com.coing.global.exception.BusinessException;
import com.coing.util.MessageUtil;
import com.coing.util.Ut;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

	private final EmailSenderService emailSenderService;
	private final MessageUtil messageUtil;

	@Value("${custom.jwt.secret-key}")
	private String jwtSecretKey;

	// 비밀번호 재설정 이메일 전송 (비동기 처리 및 트랜잭션 관리)
	@Async
	@Transactional
	public void sendPasswordResetEmail(User user) {
		// JWT 토큰 생성 (만료: 1시간 = 3600초)
		Map<String, Object> claims = Map.of("id", user.getId());
		String token = Ut.Jwt.createToken(jwtSecretKey, 3600, claims);
		try {
			emailSenderService.sendPasswordResetEmailMessage(user.getEmail(), token);
			log.info("비밀번호 재설정 이메일 전송 성공: {}", user.getEmail());
		} catch (Exception e) {
			log.error("비밀번호 재설정 이메일 전송 에러: {}", user.getEmail(), e);
			throw new BusinessException(messageUtil.resolveMessage("mail.send.fail"),
				HttpStatus.BAD_REQUEST, "");
		}
	}
}
