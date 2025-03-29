package com.example.auth.service.constant;

public class ErrorConstant {

    public static final String ERROR_USERNAME_REQUIRED = "Username cannot be blank";
    public static final String ERROR_EMAIL_REQUIRED = "Email cannot be blank";
    public static final String ERROR_INVALID_EMAIL_FORMAT = "Invalid email format";
    public static final String ERROR_PASSWORD_REQUIRED = "Email cannot be blank";

    public static final String USER_NOT_FOUND_ERROR_CODE = "USER_NOT_FOUND";
    public static final String USER_NOT_FOUND_ERROR_DESC = "User not found";

    public static final String USERNAME_ALREADY_EXISTS_ERROR_CODE = "USERNAME_ALREADY_EXISTS";
    public static final String USERNAME_ALREADY_EXISTS_ERROR_DESC = "This username is already in use. Please choose " +
            "another one.";

    public static final String EMAIL_ALREADY_EXISTS_ERROR_CODE = "EMAIL_ALREADY_EXISTS";
    public static final String EMAIL_ALREADY_EXISTS_ERROR_DESC = "An account with this email already exists. Please " +
            "log in or use the 'Forgot Password' option.";
}
