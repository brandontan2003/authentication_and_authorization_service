package com.example.auth.service.common.exception;

import com.example.auth.service.common.dto.ResponsePayload;
import com.example.auth.service.common.dto.error.Error;
import com.example.auth.service.common.dto.error.ErrorPayload;
import com.example.auth.service.common.dto.error.ErrorsPayload;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;

import static com.example.auth.service.common.constant.ApiConstant.STATUS_ERROR;
import static com.example.auth.service.common.exception.CommonErrorMessage.*;


@ControllerAdvice
public class CommonExceptionHandler {

    private static Error getError(CommonErrorMessage err) {
        return Error.builder().errorCode(err.getErrorCode()).errorMessage(err.getErrorMessage()).build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponsePayload<ErrorsPayload>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        List<String> errorDescriptions = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        List<Error> errorList = new ArrayList<>();
        errorDescriptions.forEach(errorDescription -> {
            Error err = getError(FIELD_VALIDATION_ERROR);
            err.setErrorMessage(errorDescription);
            errorList.add(err);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponsePayload.<ErrorsPayload>builder()
                .status(STATUS_ERROR).result(ErrorsPayload.builder().errors(errorList).build()).build());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ResponsePayload<ErrorPayload>> handleRequestParametersValidation(
            MissingServletRequestParameterException ex) {
        Error err = getError(FIELD_VALIDATION_ERROR);
        err.setErrorMessage(ex.getParameterName() + " is required.");


        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponsePayload.<ErrorPayload>builder()
                .status(STATUS_ERROR).result(ErrorPayload.builder().error(err).build()).build());
    }

    @ExceptionHandler({AccessDeniedException.class, AuthenticationException.class})
    public ResponseEntity<ResponsePayload<ErrorPayload>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponsePayload.<ErrorPayload>builder()
                .status(STATUS_ERROR).result(ErrorPayload.builder().error(getError(FORBIDDEN_ERROR)).build()).build());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ResponsePayload<ErrorPayload>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ResponsePayload.<ErrorPayload>builder()
                .status(STATUS_ERROR).result(ErrorPayload.builder().error(getError(METHOD_NOT_ALLOWED_ERROR)).build()).build());
    }

}
