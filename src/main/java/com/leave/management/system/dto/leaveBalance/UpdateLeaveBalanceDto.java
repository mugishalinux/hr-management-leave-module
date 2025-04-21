package com.leave.management.system.dto.leaveBalance;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateLeaveBalanceDto {
    @NotNull(message = "User ID is required")
    private String userId;

    @Min(value = 0, message = "Leave days must be non-negative")
    private double remainingDays;
}
