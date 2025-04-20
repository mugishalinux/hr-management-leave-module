package com.leave.management.system.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDto {
    private String id;
    private String token;
    private String permissions;
}
