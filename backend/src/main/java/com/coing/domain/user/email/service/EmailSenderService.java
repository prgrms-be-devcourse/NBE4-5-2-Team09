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

	public MimeMessage createEmailVerificationMail(String recipientEmail, String token) throws MessagingException {
		MimeMessage message = javaMailSender.createMimeMessage();
		String fromEmail = senderEmail;
		message.setFrom(fromEmail);
		message.setRecipients(MimeMessage.RecipientType.TO, recipientEmail);
		message.setSubject("이메일 인증");
		// 인증 링크 구성 (도메인은 실제 서비스 URL로 변경)
		String verificationLink = "http://localhost:8080/api/auth/verify-email?token=" + token;
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
}
