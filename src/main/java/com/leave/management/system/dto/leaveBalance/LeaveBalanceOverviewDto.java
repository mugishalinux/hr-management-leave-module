package com.leave.management.system.dto.leaveBalance;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeaveBalanceOverviewDto {
    private double leaveBalance;
    private double daysAllowed;
    private double accrualRate;
}