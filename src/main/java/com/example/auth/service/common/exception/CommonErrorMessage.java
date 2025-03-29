package com.example.auth.service.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static com.example.auth.service.common.constant.CommonErrorConstant.*;


@Getter
public enum CommonErrorMessage {

    FIELD_VALIDATION_ERROR(HttpStatus.BAD_REQUEST, FIELD_VALIDATION_ERROR_CODE, FIELD_VALIDATION_ERROR_DESC),
    METHOD_NOT_ALLOWED_ERROR(HttpStatus.METHOD_NOT_ALLOWED, METHOD_NOT_ALLOWED_ERROR_CODE,
            METHOD_NOT_ALLOWED_ERROR_DESC),
    UNAUTHORIZED_ERROR(HttpStatus.UNAUTHORIZED, UNAUTHORIZED_ERROR_CODE, UNAUTHORIZED_ERROR_DESC),
    TOKEN_EXPIRED_ERROR(HttpStatus.UNAUTHORIZED, TOKEN_EXPIRED_ERROR_CODE, TOKEN_EXPIRED_ERROR_DESC),
    INVALID_TOKEN_ERROR(HttpStatus.UNAUTHORIZED, TOKEN_EXPIRED_ERROR_CODE, TOKEN_EXPIRED_ERROR_DESC),
    FORBIDDEN_ERROR(HttpStatus.FORBIDDEN, FORBIDDEN_ERROR_CODE, FORBIDDEN_ERROR_DESC);

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String errorMessage;

    // Constructor to initialize the enum constants
    CommonErrorMessage(HttpStatus httpStatus, String errorCode, String errorMessage) {
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
