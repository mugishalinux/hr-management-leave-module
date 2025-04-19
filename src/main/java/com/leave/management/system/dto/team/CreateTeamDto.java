package com.leave.management.system.dto.team;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTeamDto {
    @NotBlank(message = "Team name is required")
    private String name;

    private String description;

    @NotBlank(message = "Department ID is required")
    private String departmentId;

}