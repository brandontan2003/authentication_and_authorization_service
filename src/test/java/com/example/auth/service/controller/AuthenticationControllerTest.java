package com.example.auth.service.controller;

import com.example.auth.service.common.dto.ResponsePayload;
import com.example.auth.service.dto.CreateUserRequest;
import com.example.auth.service.dto.CreateUserResponse;
import com.example.auth.service.dto.LoginResponse;
import com.example.auth.service.dto.LoginUserRequest;
import com.example.auth.service.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.example.auth.service.TestUtils.TestUser.*;
import static com.example.auth.service.common.constant.ApiConstant.STATUS_SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthenticationControllerTest {

    public static final String TOKEN = "token";
    public static final String NEW_TOKEN = "newToken";
    public static final int EXPIRES_IN = 6000;
    @InjectMocks
    private AuthenticationController authenticationController;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private HttpServletRequest request;

    private LoginUserRequest buildLoginUserRequest() {
        LoginUserRequest request = new LoginUserRequest();
        request.setUsername(USERNAME);
        request.setPassword(PASSWORD);
        return request;
    }

    @Test
    void createUser_Success() {
        when(authenticationService.signUp(any())).thenReturn(CreateUserResponse.builder().username(USERNAME)
                .email(EMAIL).enabled(Boolean.TRUE).build());

        ResponsePayload<CreateUserResponse> actualResponse = authenticationController.createUser(
                CreateUserRequest.builder().email(EMAIL).password(PASSWORD).username(USERNAME).build());

        assertNotNull(actualResponse);
        assertEquals(STATUS_SUCCESS, actualResponse.getStatus());
        CreateUserResponse result = actualResponse.getResult();
        assertEquals(EMAIL, result.getEmail());
        assertEquals(USERNAME, result.getUsername());
        assertEquals(Boolean.TRUE, result.getEnabled());
    }

    @Test
    void login_Success() {
        when(authenticationService.authenticate(any())).thenReturn(
                LoginResponse.builder().token(TOKEN).expiresIn(EXPIRES_IN).build());

        ResponsePayload<LoginResponse> actualResponse = authenticationController.login(buildLoginUserRequest());

        assertNotNull(actualResponse);
        assertEquals(STATUS_SUCCESS, actualResponse.getStatus());
        LoginResponse result = actualResponse.getResult();
        assertEquals(TOKEN, result.getToken());
        assertEquals(EXPIRES_IN, result.getExpiresIn());
    }

    @Test
    void refreshToken_Success() {
        when(authenticationService.authenticate(any())).thenReturn(
                LoginResponse.builder().token(TOKEN).expiresIn(EXPIRES_IN).build());
        authenticationController.login(buildLoginUserRequest());

        when(authenticationService.refreshToken(any())).thenReturn(
                LoginResponse.builder().token(NEW_TOKEN).expiresIn(EXPIRES_IN).build());
        ResponsePayload<LoginResponse> actualResponse = authenticationController.refreshToken(request);

        assertNotNull(actualResponse);
        assertEquals(STATUS_SUCCESS, actualResponse.getStatus());
        LoginResponse result = actualResponse.getResult();
        assertEquals(NEW_TOKEN, result.getToken());
        assertEquals(EXPIRES_IN, result.getExpiresIn());
    }
}
