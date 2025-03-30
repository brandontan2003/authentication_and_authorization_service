package com.example.auth.service.configuration;

import com.example.auth.service.common.dto.ResponsePayload;
import com.example.auth.service.common.dto.error.ErrorsPayload;
import com.example.auth.service.common.exception.CommonErrorMessage;
import com.example.auth.service.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

import static com.example.auth.service.common.constant.ApiConstant.*;
import static com.example.auth.service.common.exception.CommonErrorMessage.*;
import static com.example.auth.service.common.exception.CommonExceptionHandler.getError;
import static com.example.auth.service.constant.UriConstant.API_AUTHENTICATION;
import static com.example.auth.service.constant.UriConstant.OPEN;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    private static final List<String> PUBLIC_ENDPOINTS = List.of(API_AUTHENTICATION + OPEN);

    public JwtAuthenticationFilter(HandlerExceptionResolver handlerExceptionResolver, JwtService jwtService,
                                   UserDetailsService userDetailsService) {
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    private boolean isExemptPath(String requestUri) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(requestUri::startsWith);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (isExemptPath(request.getRequestURI())) {
            filterChain.doFilter(request, response); // Proceed with filter chain
            return;
        }

        final String authHeader = request.getHeader(AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER)) {
            sendErrorResponse(response, FORBIDDEN_ERROR);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String username = jwtService.extractUsername(jwt);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (StringUtils.isNotBlank(username) && authentication == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (JwtException ex) {
            sendErrorResponse(response, UNAUTHORIZED_ERROR);
        } catch (Exception ex) {
            sendErrorResponse(response, INVALID_TOKEN_ERROR);
        }
    }

    private void sendErrorResponse(HttpServletResponse response, CommonErrorMessage error) throws IOException {
        response.setContentType(APPLICATION_JSON);
        response.setStatus(error.getHttpStatus().value());

        ErrorsPayload errorPayload = ErrorsPayload.builder()
                .errors(List.of(getError(error)))
                .build();

        ResponsePayload<ErrorsPayload> responsePayload = ResponsePayload.<ErrorsPayload>builder()
                .status(STATUS_ERROR)
                .result(errorPayload)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(responsePayload));
        response.getWriter().flush();
    }
}
