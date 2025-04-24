package com.leave.management.system.dto.leaveApplication;

import lombok.Data;

import java.util.List;

// TeamLeaveSummaryDto.java
@Data
public class TeamLeaveSummaryDto {
    private String teamName;
    private List<String> teamMembers;
    private List<LeaveSummaryDto> onLeave;

    public TeamLeaveSummaryDto(String name, List<String> memberNames, List<LeaveSummaryDto> leaves) {
    }

    // constructor, getters, setters
}

