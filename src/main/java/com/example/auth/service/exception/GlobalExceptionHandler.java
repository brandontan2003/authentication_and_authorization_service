package com.example.auth.service.exception;

import com.example.auth.service.common.dto.ResponsePayload;
import com.example.auth.service.common.dto.error.Error;
import com.example.auth.service.common.dto.error.ErrorPayload;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.example.auth.service.common.constant.ApiConstant.STATUS_ERROR;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ResponsePayload<ErrorPayload>> handleProductException(AuthException ex) {
        AuthErrorMessage err = ex.getErrorMessage();
        return ResponseEntity.status(err.getHttpStatus()).body(ResponsePayload.<ErrorPayload>builder()
                .status(STATUS_ERROR).result(ErrorPayload.builder().error(getError(err)).build()).build());
    }

    private static Error getError(AuthErrorMessage err) {
        return Error.builder().errorCode(err.getErrorCode()).errorMessage(err.getErrorMessage()).build();
    }

}
