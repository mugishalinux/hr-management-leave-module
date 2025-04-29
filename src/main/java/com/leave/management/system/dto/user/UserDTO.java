package com.leave.management.system.dto.user;

import com.leave.management.system.model.Department;
import com.leave.management.system.model.Team;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private String id;
    private String fullName;
    private String email;
    private String profile;
    private String permissions;
    private boolean accountEnabled;

    private String teamId;
    private String teamName;
    private String departmentId;
    private String departmentName;
}
