package com.coing.global.exception.doc;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	// Common
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "internal.server.error"),

	// User
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "member.not.found"),
	NAME_REQUIRED(HttpStatus.BAD_REQUEST, "name.required"),
	INVALID_NAME_LENGTH(HttpStatus.BAD_REQUEST, "invalid.name.length"),
	EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "email.required"),
	PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "password.required"),
	PASSWORD_CONFIRM_REQUIRED(HttpStatus.BAD_REQUEST, "password.confirm.required"),
	INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "invalid.password.format"),
	INVALID_PASSWORD_LENGTH(HttpStatus.BAD_REQUEST, "invalid.password.length"),
	PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "password.mismatch"),
	INVALID_PASSWORD_CONFIRM(HttpStatus.BAD_REQUEST, "invalid.password.confirm"),
	INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "invalid.email.format"),

	// Email
	MAIL_SEND_FAIL(HttpStatus.BAD_REQUEST, "mail.send.fail"),
	INVALID_EMAIL_CODE(HttpStatus.BAD_REQUEST, "invalid.email.code"),
	EMAIL_NOT_VERIFIED(HttpStatus.UNAUTHORIZED, "email.not.verified"),
	ALREADY_REGISTERED_EMAIL(HttpStatus.BAD_REQUEST, "already.registered.email"),
	EMAIL_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "email.token.mismatch"),

	// auth
	LOGIN_FAILURE(HttpStatus.UNAUTHORIZED, "login.failure"),
	DIFFERENT_LOGIN_METHODS(HttpStatus.BAD_REQUEST, "different.login.methods"),
	TOKEN_REQUIRED(HttpStatus.UNAUTHORIZED, "token.required"),
	REFRESH_TOKEN_REQUIRED(HttpStatus.FORBIDDEN, "refresh.token.required"),
	INVALID_REFRESH_TOKEN(HttpStatus.FORBIDDEN, "invalid.refresh.token"),
	EMPTY_TOKEN_PROVIDED(HttpStatus.FORBIDDEN, "empty.token.provided"),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "invalid.token"),

	// Bookmark
	BOOKMARK_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "bookmark.already.exists"),
	BOOKMARK_NOT_FOUND(HttpStatus.NOT_FOUND, "bookmark.not.found"),
	BOOKMARK_ACCESS_DENIED(HttpStatus.FORBIDDEN, "bookmark.access.denied"),

	// Market
	MARKET_NOT_FOUND(HttpStatus.NOT_FOUND, "market.not.found"),

	// Trade
	TRADE_FETCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "trade.fetch.failed"),
	TRADE_NOT_FOUND(HttpStatus.NOT_FOUND, "trade.not.found"),

	// Ticker
	TICKER_NOT_FOUND(HttpStatus.NOT_FOUND, "ticker.not.found");

	private final HttpStatus status;
	private final String messageKey;
}
