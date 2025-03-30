package com.example.auth.service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.example.auth.service.constant.ErrorConstant.ERROR_PASSWORD_REQUIRED;
import static com.example.auth.service.constant.ErrorConstant.ERROR_USERNAME_REQUIRED;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserRequest {

    @NotBlank(message = ERROR_USERNAME_REQUIRED)
    private String username;
    @NotBlank(message = ERROR_PASSWORD_REQUIRED)
    private String password;
}
