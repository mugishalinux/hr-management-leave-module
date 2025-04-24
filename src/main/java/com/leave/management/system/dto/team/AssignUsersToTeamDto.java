package com.leave.management.system.dto.team;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AssignUsersToTeamDto {

    @NotNull(message = "Team ID is required")
    private String teamId;

    @NotEmpty(message = "At least one user ID must be provided")
    private List<String> userIds;
}


