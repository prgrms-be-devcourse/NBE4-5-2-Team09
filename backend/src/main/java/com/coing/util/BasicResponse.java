package com.coing.util;

import com.coing.global.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public record BasicResponse(
        HttpStatus status,
        String message,
        String detail
) {
    public static BasicResponse of(BusinessException exception) {
        return new BasicResponse(
                exception.getStatus(),
                exception.getMessageCode(),
                exception.getDetail()
        );
    }

    public static BasicResponse of(String message) {
        return new BasicResponse(
                HttpStatus.OK,
                message,
                ""
        );
    }

    public static ResponseEntity<BasicResponse> to(BusinessException exception) {
        return ResponseEntity
                .status(exception.getStatus())
                .body(BasicResponse.of(exception));
    }
}
