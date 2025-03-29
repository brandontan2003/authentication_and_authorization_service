package com.example.auth.service.service;

import com.example.auth.service.common.exception.CommonException;
import com.example.auth.service.dto.CreateUserRequest;
import com.example.auth.service.dto.CreateUserResponse;
import com.example.auth.service.dto.LoginResponse;
import com.example.auth.service.dto.LoginUserRequest;
import com.example.auth.service.exception.AuthErrorMessage;
import com.example.auth.service.exception.AuthException;
import com.example.auth.service.model.User;
import com.example.auth.service.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static com.example.auth.service.TestUtils.TestUser.*;
import static com.example.auth.service.common.constant.ApiConstant.AUTHORIZATION;
import static com.example.auth.service.common.constant.ApiConstant.BEARER;
import static com.example.auth.service.common.exception.CommonErrorMessage.INVALID_TOKEN_ERROR;
import static com.example.auth.service.common.exception.CommonErrorMessage.UNAUTHORIZED_ERROR;
import static com.example.auth.service.exception.AuthErrorMessage.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    private static final long JWT_EXPIRATION = 60000L;
    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private HttpServletRequest request;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authenticationService, "mapper", new ModelMapper());

        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(authenticationService, "jwtService", jwtService);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", JWT_EXPIRATION);

        Key secretKey = Jwts.SIG.HS256.key().build();
        String base64SecretKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        ReflectionTestUtils.setField(jwtService, "secretKey", base64SecretKey);
    }

    private CreateUserRequest buildCreateUserRequest() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername(USERNAME);
        request.setPassword(PASSWORD);
        request.setEmail(EMAIL);
        return request;
    }

    private LoginUserRequest buildLoginUserRequest() {
        LoginUserRequest request = new LoginUserRequest();
        request.setUsername(USERNAME);
        request.setPassword(PASSWORD);
        return request;
    }

    private static User mockTestUser() {
        User user = new User();
        user.setUserId(UUID.randomUUID().toString());
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);
        user.setEmail(EMAIL);
        return user;
    }

    @Test
    void createUser_Success() {
        when(userRepository.save(any())).thenReturn(mockTestUser());
        CreateUserResponse actualResponse = authenticationService.signUp(buildCreateUserRequest());

        verify(userRepository, times(1)).save(any());
        assertNotNull(actualResponse);
        assertEquals(USERNAME, actualResponse.getUsername());
        assertEquals(EMAIL, actualResponse.getEmail());
        assertEquals(Boolean.TRUE, actualResponse.getEnabled());
    }

    static Stream<Arguments> test_createUser_Failure() {
        return Stream.of(
                Arguments.of("Test createUser where email is already registered", null, mockTestUser(),
                        EMAIL_ALREADY_EXISTS),
                Arguments.of("Test createUser where username is already registered", mockTestUser(), null,
                        USERNAME_ALREADY_EXISTS)
        );
    }


    @ParameterizedTest
    @MethodSource("test_createUser_Failure")
    void createUser_Failure(String name, User usernameUser, User emailUser, AuthErrorMessage errorMessage) {
        when(userRepository.findByUsername(any())).thenReturn(Optional.ofNullable(usernameUser));
        lenient().when(userRepository.findByEmail(any())).thenReturn(Optional.ofNullable(emailUser));

        AuthException ex = assertThrows(AuthException.class,
                () -> authenticationService.signUp(buildCreateUserRequest()));

        verify(userRepository, times(0)).save(any());
        assertEquals(errorMessage, ex.getErrorMessage());
        assertEquals(errorMessage.getHttpStatus(), ex.getErrorMessage().getHttpStatus());
        assertEquals(errorMessage.getErrorCode(), ex.getErrorMessage().getErrorCode());
        assertEquals(errorMessage.getErrorMessage(), ex.getErrorMessage().getErrorMessage());
    }

    @Test
    void authenticateUser_notFound_Failure() {
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());

        AuthException ex = assertThrows(AuthException.class,
                () -> authenticationService.authenticate(buildLoginUserRequest()));

        verify(userRepository, times(1)).findByUsername(any());
        assertEquals(USER_NOT_FOUND, ex.getErrorMessage());
        assertEquals(USER_NOT_FOUND.getHttpStatus(), ex.getErrorMessage().getHttpStatus());
        assertEquals(USER_NOT_FOUND.getErrorCode(), ex.getErrorMessage().getErrorCode());
        assertEquals(USER_NOT_FOUND.getErrorMessage(), ex.getErrorMessage().getErrorMessage());
    }

    @Test
    void authenticateUser_Success() {
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(mockTestUser()));

        LoginResponse actualResponse = authenticationService.authenticate(buildLoginUserRequest());

        verify(userRepository, times(1)).findByUsername(any());
        assertNotNull(actualResponse);
        assertNotNull(actualResponse.getToken());
        assertEquals(JWT_EXPIRATION, actualResponse.getExpiresIn());
    }

    @Test
    void refreshToken_Success() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(Boolean.TRUE);

        when(userRepository.findByUsername(any())).thenReturn(Optional.of(mockTestUser()));
        LoginResponse loginResponse = authenticationService.authenticate(buildLoginUserRequest());
        when(request.getHeader(AUTHORIZATION)).thenReturn(BEARER + loginResponse.getToken());

        LoginResponse actualResponse = authenticationService.refreshToken(request);

        verify(userRepository, times(2)).findByUsername(any());
        assertNotNull(actualResponse);
        assertNotNull(actualResponse.getToken());
        assertEquals(JWT_EXPIRATION, actualResponse.getExpiresIn());
    }

    static Stream<Arguments> test_refreshToken_emptyBearerToken_Failure() {
        return Stream.of(
                Arguments.of("Test refreshToken where Authorization is null", null),
                Arguments.of("Test refreshToken where Authorization contains Bearer ", BEARER)
        );
    }

    @ParameterizedTest
    @MethodSource("test_refreshToken_emptyBearerToken_Failure")
    void refreshToken_emptyBearerToken_Failure(String name, String authorizationHeader) {
        when(request.getHeader(AUTHORIZATION)).thenReturn(authorizationHeader);

        CommonException ex = assertThrows(CommonException.class, () -> authenticationService.refreshToken(request));

        verify(userRepository, times(0)).findByUsername(any());
        assertEquals(INVALID_TOKEN_ERROR, ex.getErrorMessage());
        assertEquals(INVALID_TOKEN_ERROR.getHttpStatus(), ex.getErrorMessage().getHttpStatus());
        assertEquals(INVALID_TOKEN_ERROR.getErrorCode(), ex.getErrorMessage().getErrorCode());
        assertEquals(INVALID_TOKEN_ERROR.getErrorMessage(), ex.getErrorMessage().getErrorMessage());
    }

    @Test
    void refreshToken_securityContextNotAuthenticated_Failure() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(Boolean.FALSE);

        when(userRepository.findByUsername(any())).thenReturn(Optional.of(mockTestUser()));
        LoginResponse loginResponse = authenticationService.authenticate(buildLoginUserRequest());
        when(request.getHeader(AUTHORIZATION)).thenReturn(BEARER + loginResponse.getToken());

        CommonException ex = assertThrows(CommonException.class, () -> authenticationService.refreshToken(request));

        verify(userRepository, times(1)).findByUsername(any());
        assertEquals(UNAUTHORIZED_ERROR, ex.getErrorMessage());
        assertEquals(UNAUTHORIZED_ERROR.getHttpStatus(), ex.getErrorMessage().getHttpStatus());
        assertEquals(UNAUTHORIZED_ERROR.getErrorCode(), ex.getErrorMessage().getErrorCode());
        assertEquals(UNAUTHORIZED_ERROR.getErrorMessage(), ex.getErrorMessage().getErrorMessage());
    }

}
