package com.coing.domain.user.email.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.coing.global.exception.BusinessException;
import com.coing.util.MessageUtil;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSenderService {

	private final JavaMailSender javaMailSender;
	private final MessageUtil messageUtil;

	@Value("${spring.mail.username}")
	private String senderEmail;

	@Value("${custom.jwt.mail-verification-url}")
	private String emailVerificationUrl;

	@Value("${custom.jwt.password-reset-url}")
	private String passwordResetUrl;

	public MimeMessage createEmailVerificationMail(String recipientEmail, String token) throws MessagingException {
		MimeMessage message = javaMailSender.createMimeMessage();
		String fromEmail = senderEmail;
		message.setFrom(fromEmail);
		message.setRecipients(MimeMessage.RecipientType.TO, recipientEmail);
		message.setSubject("이메일 인증");
		// 인증 링크 구성 (추후 실제 url로 환경 변수 설정 필요)
		String verificationLink = emailVerificationUrl + token;
		String body = "<h3>아래 링크를 클릭하여 이메일 인증을 완료하세요.</h3>"
			+ "<p><a href=\"" + verificationLink + "\">이메일 인증하기</a></p>";
		message.setText(body, "UTF-8", "html");
		return message;
	}

	public void sendEmailVerificationMessage(String recipientEmail, String token) throws MessagingException {
		MimeMessage message = createEmailVerificationMail(recipientEmail, token);
		try {
			javaMailSender.send(message);
			log.info("인증 이메일 전송 성공: {}", recipientEmail);
		} catch (MailException e) {
			log.error("인증 이메일 전송 에러: {}", recipientEmail, e);
			throw new BusinessException(messageUtil.resolveMessage("mail.send.fail"),
				org.springframework.http.HttpStatus.BAD_REQUEST, "");
		}
	}

	// 비밀번호 재설정 이메일 관련 메서드

	public MimeMessage createPasswordResetMail(String recipientEmail, String token) throws MessagingException {
		MimeMessage message = javaMailSender.createMimeMessage();
		message.setFrom(senderEmail);
		message.setRecipients(MimeMessage.RecipientType.TO, recipientEmail);
		message.setSubject("비밀번호 재설정");
		String resetLink = passwordResetUrl + token;
		String body = "<h3>아래 링크를 클릭하여 비밀번호 재설정을 진행하세요.</h3>"
			+ "<p><a href=\"" + resetLink + "\">비밀번호 재설정하기</a></p>";
		message.setText(body, "UTF-8", "html");
		return message;
	}

	public void sendPasswordResetEmailMessage(String recipientEmail, String token) throws MessagingException {
		MimeMessage message = createPasswordResetMail(recipientEmail, token);
		try {
			javaMailSender.send(message);
			log.info("비밀번호 재설정 이메일 전송 성공: {}", recipientEmail);
		} catch (MailException e) {
			log.error("비밀번호 재설정 이메일 전송 에러: {}", recipientEmail, e);
			throw new BusinessException(messageUtil.resolveMessage("mail.send.fail"),
				org.springframework.http.HttpStatus.BAD_REQUEST, "");
		}
	}
}
