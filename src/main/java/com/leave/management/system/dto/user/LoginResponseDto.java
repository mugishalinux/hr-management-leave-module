package com.leave.management.system.dto.user;

import com.leave.management.system.model.Department;
import com.leave.management.system.model.Team;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {
    private String id;
    private String token;
    private String permissions;
    private String departmentId;
}
