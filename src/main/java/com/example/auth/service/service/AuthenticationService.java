package com.example.auth.service.service;

import com.example.auth.service.common.exception.CommonException;
import com.example.auth.service.dto.CreateUserRequest;
import com.example.auth.service.dto.CreateUserResponse;
import com.example.auth.service.dto.LoginResponse;
import com.example.auth.service.dto.LoginUserRequest;
import com.example.auth.service.exception.AuthException;
import com.example.auth.service.model.User;
import com.example.auth.service.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.example.auth.service.common.constant.ApiConstant.AUTHORIZATION;
import static com.example.auth.service.common.constant.ApiConstant.BEARER;
import static com.example.auth.service.common.exception.CommonErrorMessage.*;
import static com.example.auth.service.exception.AuthErrorMessage.*;

@Service
@Slf4j
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ModelMapper mapper;

    public CreateUserResponse signUp(CreateUserRequest createUserRequest) {
        validateSignUpRequest(createUserRequest);
        User user = saveUser(createUserRequest);
        return mapper.map(user, CreateUserResponse.class);

    }

    private User saveUser(CreateUserRequest createUserRequest) {
        User user = new User();
        user.setUsername(createUserRequest.getUsername());
        user.setEmail(createUserRequest.getEmail());
        user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
        return userRepository.save(user);
    }

    public void validateSignUpRequest(CreateUserRequest createUserRequest) {
        Optional<User> username = userRepository.findByUsername(createUserRequest.getUsername());
        if (username.isPresent()) {
            throw new AuthException(USERNAME_ALREADY_EXISTS);
        }

        Optional<User> email = userRepository.findByEmail(createUserRequest.getEmail());
        if (email.isPresent()) {
            throw new AuthException(EMAIL_ALREADY_EXISTS);
        }
    }

    public LoginResponse authenticate(LoginUserRequest loginUserRequest) {
        User user = userRepository.findByUsername(loginUserRequest.getUsername())
                .orElseThrow(() -> new AuthException(USER_NOT_FOUND));

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginUserRequest.getUsername(),
                loginUserRequest.getPassword()));

        String jwtToken = jwtService.generateToken(user);
        return LoginResponse.builder().token(jwtToken).expiresIn(jwtService.getJwtExpiration()).build();
    }

    public LoginResponse refreshToken(HttpServletRequest request) {
        String accessToken = extractJwtFromHeader(request);
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new CommonException(INVALID_TOKEN_ERROR);
        }

        // Extract username from the current access token
        String username = jwtService.extractUsername(accessToken);

        if (!SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            throw new CommonException(UNAUTHORIZED_ERROR);
        }

        UserDetails userDetails = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException(USER_NOT_FOUND));

        // Validate the access token before refreshing
        if (!jwtService.isTokenValid(accessToken, userDetails)) {
            throw new CommonException(TOKEN_EXPIRED_ERROR);
        }

        String jwtToken = jwtService.generateToken(userDetails);
        return LoginResponse.builder().token(jwtToken).expiresIn(jwtService.getJwtExpiration()).build();
    }

    private String extractJwtFromHeader(HttpServletRequest request) {
        final String authHeader = request.getHeader(AUTHORIZATION);
        return (authHeader != null && authHeader.startsWith(BEARER)) ? authHeader.substring(7) : null;
    }
}
