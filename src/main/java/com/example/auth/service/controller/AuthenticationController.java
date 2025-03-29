package com.example.auth.service.controller;

import com.example.auth.service.common.dto.ResponsePayload;
import com.example.auth.service.dto.CreateUserRequest;
import com.example.auth.service.dto.CreateUserResponse;
import com.example.auth.service.dto.LoginResponse;
import com.example.auth.service.dto.LoginUserRequest;
import com.example.auth.service.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.auth.service.common.constant.ApiConstant.STATUS_SUCCESS;
import static com.example.auth.service.constant.UriConstant.*;

@RestController
@RequestMapping(API_AUTHENTICATION)
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping(OPEN + API_VERSION_1 + SIGN_UP)
    public ResponsePayload<CreateUserResponse> createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        return ResponsePayload.<CreateUserResponse>builder().status(STATUS_SUCCESS)
                .result(authenticationService.signUp(createUserRequest)).build();
    }

    @PostMapping(OPEN + API_VERSION_1 + LOGIN)
    public ResponsePayload<LoginResponse> login(@Valid @RequestBody LoginUserRequest loginUserRequest) {
        return ResponsePayload.<LoginResponse>builder().status(STATUS_SUCCESS)
                .result(authenticationService.authenticate(loginUserRequest)).build();
    }

    @PostMapping(SECURE + API_VERSION_1 + REFRESH)
    public ResponsePayload<LoginResponse> refreshToken(HttpServletRequest request) {
        return ResponsePayload.<LoginResponse>builder().status(STATUS_SUCCESS)
                .result(authenticationService.refreshToken(request)).build();
    }
}
