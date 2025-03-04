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

		return BasicResponse.to(exception);
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