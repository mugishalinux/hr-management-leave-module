package com.leave.management.system.service.leaveBalance;

import com.leave.management.system.dto.leaveBalance.LeaveBalanceOverviewDto;

public interface LeaveBalanceOverviewService {
    LeaveBalanceOverviewDto getCurrentUserLeaveOverview();
}