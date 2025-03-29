package com.example.auth.service.service;

import com.example.auth.service.dto.RetrieveUserProfileResponse;
import com.example.auth.service.model.User;
import com.example.auth.service.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper mapper;

    public RetrieveUserProfileResponse retrieveUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return mapper.map(user, RetrieveUserProfileResponse.class);
    }
}
