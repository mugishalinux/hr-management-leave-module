package com.leave.management.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterUserDto {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    // Optionally, allow setting roles if needed
    private String permissions = "USER_MANAGEMENT";
}
