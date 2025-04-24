package com.leave.management.system.dto.leaveType;

import com.leave.management.system.enums.LeaveTypeStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LeaveTypeUpdateDto {

    @NotBlank(message = "Leave type name is required")
    @Size(max = 90, message = "Name must be at most 90 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    private boolean leaveTypeRequiresAttachment;

    @NotNull(message = "Leave type status is required")
    private LeaveTypeStatus leaveTypeStatus;

    private boolean isLeaveTypeRequireReason;

    private boolean affectsBalance;

    private double daysLimit;
}