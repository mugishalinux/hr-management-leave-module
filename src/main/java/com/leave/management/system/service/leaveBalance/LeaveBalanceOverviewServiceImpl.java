package com.leave.management.system.service.leaveBalance;

import com.leave.management.system.constants.HolidayConstants;
import com.leave.management.system.dto.leaveBalance.LeaveBalanceOverviewDto;
import com.leave.management.system.enums.LeaveApplicationStatus;
import com.leave.management.system.exceptions.ApiRequestException;
import com.leave.management.system.model.*;
import com.leave.management.system.repository.*;
import com.leave.management.system.security.SecurityUtils;
import com.leave.management.system.util.helpers.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveBalanceOverviewServiceImpl implements LeaveBalanceOverviewService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final SecurityUtils securityUtils;
    private final LeavePolicyRepository leavePolicyRepository;
    private final UserRepository userRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveApplicationRepository leaveApplicationRepository;

    @Override
    public LeaveBalanceOverviewDto getCurrentUserLeaveOverview() {
        try {
            User user = securityUtils.getCurrentUser();
            LeaveBalance balance = leaveBalanceRepository.findByUser(user)
                    .orElseThrow(() -> new ApiRequestException("Leave balance not found"));

            LeavePolicy policy = leavePolicyRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new ApiRequestException("Leave policy not found"));


            return new LeaveBalanceOverviewDto(
                    balance.getRemainingDays(),
                    balance.getTotalEntitledDays(),
                    policy.getAccrualRate()
            );
        }catch (Exception e){
            throw new ApiRequestException(e.getMessage());
        }
    }

    @Override
    public LeaveBalanceOverviewDto getCustomLeaveBalanceForUser(String userId, String leaveTypeId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ApiRequestException("User not found"));

            LeaveType leaveType = leaveTypeRepository.findById(leaveTypeId)
                    .orElseThrow(() -> new ApiRequestException("Leave type not found"));

            if (leaveType.isAffectsBalance()) {
                throw new ApiRequestException("This method is for leave types that do NOT affect balance");
            }

            List<LeaveApplication> approvedLeaves = leaveApplicationRepository.findAllByUserAndLeaveTypeAndStatus(user, leaveType, LeaveApplicationStatus.APPROVED);

            long daysUsed = approvedLeaves.stream()
                    .mapToLong(app -> DateUtils.calculateWorkingDays(app.getStartDate(), app.getEndDate(), HolidayConstants.HOLIDAYS_2025))
                    .sum();

            double daysLimit = leaveType.getDaysLimit();
            double remaining = daysLimit - daysUsed;

            return new LeaveBalanceOverviewDto(remaining, daysLimit, 0);
        } catch (Exception e) {
            throw new ApiRequestException(e.getMessage());
        }
    }
}
