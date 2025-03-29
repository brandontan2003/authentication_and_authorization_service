package com.example.auth.service.exception;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {

    private final AuthErrorMessage errorMessage;

    public AuthException(AuthErrorMessage errorMessage) {
        super(errorMessage.getErrorMessage());
        this.errorMessage = errorMessage;
    }
}
