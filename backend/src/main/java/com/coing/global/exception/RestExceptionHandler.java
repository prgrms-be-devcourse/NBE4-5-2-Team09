package com.coing.global.exception;

import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.coing.util.BasicResponse;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class RestExceptionHandler {

	private final MessageSource messageSource;

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<BasicResponse> handleBusinessException(BusinessException exception) {
		log.warn("[ExceptionHandler] Message: {}, Detail: {}", exception.getMessage(), exception.getDetail());
		if (exception.getMessage() != null && exception.getMessage().contains("인증 토큰")) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(BasicResponse.of(exception));
		}
		return BasicResponse.to(exception);
	}

	@ExceptionHandler(ExpiredJwtException.class)
	public ResponseEntity<BasicResponse> handleExpiredJwtException(ExpiredJwtException ex) {
		log.warn("[ExceptionHandler] ExpiredJwtException: {}", ex.getMessage());
		BusinessException businessException = new BusinessException("토큰 만료", HttpStatus.FORBIDDEN, ex.getMessage());
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(BasicResponse.of(businessException));
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<BasicResponse> handleRuntimeException(RuntimeException exception) {
		log.error("[ExceptionHandler] Runtime exception occurred: ", exception);

		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

		BusinessException businessException = new BusinessException("Internal Server Error",
			HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
		log.error("[ExceptionHandler] Message: {}, Detail: {}", businessException.getMessage(),
			businessException.getDetail());

		return ResponseEntity
			.status(status)
			.body(BasicResponse.of(businessException));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<BasicResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
		String errors = ex.getBindingResult().getFieldErrors().stream()
			.map(error -> messageSource.getMessage(error, LocaleContextHolder.getLocale()))
			.collect(Collectors.joining(", "));
		BusinessException businessException = new BusinessException("Validation Error", HttpStatus.BAD_REQUEST, errors);
		log.warn("[ExceptionHandler] Validation errors: {}", errors);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BasicResponse.of(businessException));
	}
}
