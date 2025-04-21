package com.leave.management.system.dto.leaveApplication;

import com.leave.management.system.enums.LeaveApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeaveApprovalDto {

    @NotNull(message = "Leave application ID is required")
    private String leaveApplicationId;

    @NotNull(message = "Status is required (APPROVED or REJECTED)")
    private LeaveApplicationStatus status;

    private String comment;
}
