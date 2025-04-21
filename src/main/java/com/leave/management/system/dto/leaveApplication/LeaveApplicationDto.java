package com.leave.management.system.dto.leaveApplication;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LeaveApplicationDto {

    @NotBlank(message = "Leave type ID is required")
    private String leaveTypeId;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    private boolean isHalfDay;

    private String reason;

    private String attachmentPath;
}

