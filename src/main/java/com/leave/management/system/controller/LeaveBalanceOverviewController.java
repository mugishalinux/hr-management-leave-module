package com.leave.management.system.controller;

import com.leave.management.system.dto.leaveBalance.LeaveBalanceOverviewDto;
import com.leave.management.system.service.leaveBalance.LeaveBalanceOverviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leave-balance-overview")
@RequiredArgsConstructor
public class LeaveBalanceOverviewController {

    private final LeaveBalanceOverviewService leaveBalanceOverviewService;

    @GetMapping("")
    public LeaveBalanceOverviewDto getOverview() {
        return leaveBalanceOverviewService.getCurrentUserLeaveOverview();
    }
}