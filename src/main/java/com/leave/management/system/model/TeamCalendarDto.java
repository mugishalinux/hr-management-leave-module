package com.leave.management.system.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamCalendarDto {
    private String fullName;
    private String email;
    private String profile;
    private String teamName;
    private String departmentName;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isHalfDay;
}
