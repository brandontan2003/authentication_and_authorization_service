package com.example.auth.service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static com.example.auth.service.constant.ErrorConstant.*;

@Getter
public enum AuthErrorMessage {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, USER_NOT_FOUND_ERROR_CODE, USER_NOT_FOUND_ERROR_DESC),
    USERNAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, USERNAME_ALREADY_EXISTS_ERROR_CODE,
            USERNAME_ALREADY_EXISTS_ERROR_DESC),
    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, EMAIL_ALREADY_EXISTS_ERROR_CODE, EMAIL_ALREADY_EXISTS_ERROR_DESC);

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String errorMessage;

    // Constructor to initialize the enum constants
    AuthErrorMessage(HttpStatus httpStatus, String errorCode, String errorMessage) {
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
