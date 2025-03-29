package com.example.auth.service.common.constant;

public class CommonErrorConstant {
    public static final String FIELD_VALIDATION_ERROR_CODE = "FIELD_VALIDATION_ERROR";
    public static final String FIELD_VALIDATION_ERROR_DESC = "Missing mandatory field or Invalid data.";

    public static final String FORBIDDEN_ERROR_CODE = "FORBIDDEN_ERROR";
    public static final String FORBIDDEN_ERROR_DESC = "You do not have permission to access this resource.";

    public static final String METHOD_NOT_ALLOWED_ERROR_CODE = "METHOD_NOT_ALLOWED_ERROR";
    public static final String METHOD_NOT_ALLOWED_ERROR_DESC = "HTTP method not allowed for this endpoint.";

    public static final String UNAUTHORIZED_ERROR_CODE = "UNAUTHORIZED_ERROR";
    public static final String UNAUTHORIZED_ERROR_DESC = "Token has expired or is invalid.";

    public static final String TOKEN_EXPIRED_ERROR_CODE = "TOKEN_EXPIRED_ERROR";
    public static final String TOKEN_EXPIRED_ERROR_DESC = "Token has expired, please login again.";

    public static final String INVALID_TOKEN_ERROR_CODE = "INVALID_TOKEN_ERROR";
    public static final String INVALID_TOKEN_ERROR_DESC = "Token is invalid.";
}
