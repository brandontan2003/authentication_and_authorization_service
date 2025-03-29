package com.example.auth.service.service;

import com.example.auth.service.dto.RetrieveUserProfileResponse;
import com.example.auth.service.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static com.example.auth.service.TestUtils.TestUser.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks
    private UserService userService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private User mockTestUser() {
        User user = new User();
        user.setUserId(UUID.randomUUID().toString());
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);
        user.setEmail(EMAIL);
        return user;
    }

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        ReflectionTestUtils.setField(userService, "mapper", new ModelMapper());
    }

    @Test
    void retrieveUserProfile_Success() {
        when(authentication.getPrincipal()).thenReturn(mockTestUser());
        RetrieveUserProfileResponse actualResponse = userService.retrieveUserProfile();

        assertNotNull(actualResponse);
        assertEquals(USERNAME, actualResponse.getUsername());
        assertEquals(EMAIL, actualResponse.getEmail());
        assertEquals(Boolean.TRUE, actualResponse.getEnabled());
        assertEquals(Boolean.TRUE, actualResponse.getAccountNonExpired());
        assertEquals(Boolean.TRUE, actualResponse.getAccountNonLocked());
        assertEquals(Boolean.TRUE, actualResponse.getCredentialsNonExpired());
    }

}
