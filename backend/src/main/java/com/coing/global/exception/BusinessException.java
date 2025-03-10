package com.coing.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
	private final String messageCode;
	private final HttpStatus status;
	private final String detail;

	public BusinessException(String messageCode, HttpStatus status) {
		super(messageCode);
		this.messageCode = messageCode;
		this.status = status;
		this.detail = "";
	}

	public BusinessException(String messageCode, HttpStatus status, String detail) {
		super(messageCode);
		this.messageCode = messageCode;
		this.status = status;
		this.detail = detail;
	}

	// 토큰 관련 에러를 구분하기 위한 생성자: tokenError가 true이면 FORBIDDEN 처리
	public BusinessException(String messageCode, boolean tokenError) {
		super(messageCode);
		this.messageCode = messageCode;
		this.status = tokenError ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST;
		this.detail = "";
	}
}
