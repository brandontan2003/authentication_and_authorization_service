package com.example.auth.service.controller;

import com.example.auth.service.common.dto.ResponsePayload;
import com.example.auth.service.dto.RetrieveUserProfileResponse;
import com.example.auth.service.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.example.auth.service.TestUtils.TestUser.EMAIL;
import static com.example.auth.service.TestUtils.TestUser.USERNAME;
import static com.example.auth.service.common.constant.ApiConstant.STATUS_SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    private RetrieveUserProfileResponse buildRetrieveUserProfileResponse() {
        return RetrieveUserProfileResponse.builder()
                .email(EMAIL)
                .username(USERNAME)
                .accountNonExpired(Boolean.TRUE)
                .accountNonLocked(Boolean.TRUE)
                .enabled(Boolean.TRUE)
                .credentialsNonExpired(Boolean.TRUE).build();
    }

    @Test
    void retrieveEducations_ReturnEducations_Success() {
        when(userService.retrieveUserProfile()).thenReturn(buildRetrieveUserProfileResponse());

        ResponsePayload<RetrieveUserProfileResponse> actualResponse = userController.retrieveProfile();

        assertNotNull(actualResponse);
        assertEquals(STATUS_SUCCESS, actualResponse.getStatus());
        RetrieveUserProfileResponse result = actualResponse.getResult();
        assertEquals(EMAIL, result.getEmail());
        assertEquals(USERNAME, result.getUsername());
        assertEquals(Boolean.TRUE, result.getEnabled());
        assertEquals(Boolean.TRUE, result.getAccountNonLocked());
        assertEquals(Boolean.TRUE, result.getCredentialsNonExpired());
        assertEquals(Boolean.TRUE, result.getAccountNonExpired());
    }

}
