package com.example.auth.service.controller;

import com.example.auth.service.common.dto.ResponsePayload;
import com.example.auth.service.dto.RetrieveUserProfileResponse;
import com.example.auth.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.auth.service.common.constant.ApiConstant.STATUS_SUCCESS;
import static com.example.auth.service.constant.UriConstant.*;

@RestController
@RequestMapping(API_USER)
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping(API_VERSION_1 + PROFILE)
    public ResponsePayload<RetrieveUserProfileResponse> retrieveProfile() {
        return ResponsePayload.<RetrieveUserProfileResponse>builder().status(STATUS_SUCCESS)
                .result(userService.retrieveUserProfile()).build();
    }

}
