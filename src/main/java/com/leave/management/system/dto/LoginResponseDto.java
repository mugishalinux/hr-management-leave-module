package com.leave.management.system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDto {
    private String id;
    private String token;
    private String permissions;
}
