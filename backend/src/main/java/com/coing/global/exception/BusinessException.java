package com.coing.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException{
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
}
