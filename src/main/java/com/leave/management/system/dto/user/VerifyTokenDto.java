package com.leave.management.system.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyTokenDto {
    @NotBlank(message = "Token is required")
    private String token;
}
