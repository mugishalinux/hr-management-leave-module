package com.leave.management.system.dto.leaveApplication;


import com.leave.management.system.enums.LeaveApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDate;


@Data
//@AllArgsConstructor
@NoArgsConstructor
public class TeamLeaveCalendarDto {
    private String userId;
    private String userFullName;
    private String teamName;
    private String departmentName;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private String leaveType;
    private LeaveApplicationStatus status;


    public TeamLeaveCalendarDto(String userId, String userFullName, String teamName,
                                String departmentName, String location,
                                LocalDate startDate, LocalDate endDate,
                                String leaveType, LeaveApplicationStatus status) {
        this.userId = userId;
        this.userFullName = userFullName;
        this.teamName = teamName;
        this.departmentName = departmentName;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.leaveType = leaveType;
        this.status = status;
    }
}





