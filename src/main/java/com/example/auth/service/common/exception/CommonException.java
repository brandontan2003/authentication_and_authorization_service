package com.example.auth.service.common.exception;

import lombok.Getter;

@Getter
public class CommonException extends RuntimeException {

    private final CommonErrorMessage errorMessage;

    public CommonException(CommonErrorMessage errorMessage) {
        super(errorMessage.getErrorMessage());
        this.errorMessage = errorMessage;
    }
}
