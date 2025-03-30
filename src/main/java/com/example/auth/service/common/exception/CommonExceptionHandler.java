package com.example.auth.service.common.exception;

import com.example.auth.service.common.dto.ResponsePayload;
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

    public static ErrorPayload getError(CommonErrorMessage err) {
        return ErrorPayload.builder().errorCode(err.getErrorCode()).errorMessage(err.getErrorMessage()).build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponsePayload<ErrorsPayload>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        List<String> errorDescriptions = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        List<ErrorPayload> errorPayloadList = new ArrayList<>();
        errorDescriptions.forEach(errorDescription -> {
            ErrorPayload err = getError(FIELD_VALIDATION_ERROR);
            err.setErrorMessage(errorDescription);
            errorPayloadList.add(err);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponsePayload.<ErrorsPayload>builder()
                .status(STATUS_ERROR).result(ErrorsPayload.builder().errors(errorPayloadList).build()).build());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ResponsePayload<ErrorsPayload>> handleRequestParametersValidation(
            MissingServletRequestParameterException ex) {
        ErrorPayload err = getError(FIELD_VALIDATION_ERROR);
        err.setErrorMessage(ex.getParameterName() + " is required.");


        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponsePayload.<ErrorsPayload>builder()
                .status(STATUS_ERROR).result(ErrorsPayload.builder().errors(List.of(err)).build()).build());
    }

    @ExceptionHandler({AccessDeniedException.class, AuthenticationException.class})
    public ResponseEntity<ResponsePayload<ErrorsPayload>> handleAccessDenied(Exception ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponsePayload.<ErrorsPayload>builder()
                .status(STATUS_ERROR).result(ErrorsPayload.builder().errors(List.of(getError(FORBIDDEN_ERROR))).build()).build());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ResponsePayload<ErrorsPayload>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ResponsePayload.<ErrorsPayload>builder()
                .status(STATUS_ERROR).result(ErrorsPayload.builder().errors(List.of(getError(METHOD_NOT_ALLOWED_ERROR))).build()).build());
    }

}
