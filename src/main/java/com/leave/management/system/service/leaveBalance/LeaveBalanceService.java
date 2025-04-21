package com.leave.management.system.service.leaveBalance;

import com.leave.management.system.dto.leaveBalance.UpdateLeaveBalanceDto;
import com.leave.management.system.model.LeaveBalance;

public interface LeaveBalanceService {
    LeaveBalance updateLeaveBalance(UpdateLeaveBalanceDto dto);
    void createBalanceForUser(String userId);
    LeaveBalance getMyLeaveBalance();
}
