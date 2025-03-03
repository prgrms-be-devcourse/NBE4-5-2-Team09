package com.coing.global.exception;

import com.coing.util.BasicResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BasicResponse> handleBusinessException(BusinessException exception) {
        log.info("[ExceptionHandler] Message: {}, Detail: {}", exception.getMessage(), exception.getDetail());

        return BasicResponse.to(exception);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BasicResponse> handleRuntimeException(RuntimeException exception) {
        log.error("[ExceptionHandler] Runtime exception occurred: ", exception);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        BusinessException businessException = new BusinessException("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");
        log.error("[ExceptionHandler] Message: {}, Detail: {}", businessException.getMessage(), exception.getMessage());

        return ResponseEntity
                .status(status)
                .body(BasicResponse.of(businessException));
    }
}