package com.leave.management.system.dto.leaveApplication;

import lombok.Data;

import java.util.List;

@Data
public class TeamOnLeaveDto {
    private String teamName;
    private List<String> onLeaveUsers;

    public TeamOnLeaveDto(String teamName, List<String> onLeaveUsers) {
        this.teamName = teamName;
        this.onLeaveUsers = onLeaveUsers;
    }

    // Getters and setters
}
