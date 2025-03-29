package com.example.auth.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.example.auth.service.constant.ErrorConstant.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = ERROR_USERNAME_REQUIRED)
    private String username;
    @Email(message = ERROR_INVALID_EMAIL_FORMAT)
    @NotBlank(message = ERROR_EMAIL_REQUIRED)
    private String email;
    @NotBlank(message = ERROR_PASSWORD_REQUIRED)
    private String password;
}
