package com.leave.management.system.dto.leaveApplication;

import java.util.List;


public class TeamLeaveOverviewDto {
    private String teamName;
    private List<String> allTeamMembers;
    private List<String> membersOnLeave;

    public TeamLeaveOverviewDto(String name, List<String> allTeamNames, List<String> onLeaveNames) {
    }
}

