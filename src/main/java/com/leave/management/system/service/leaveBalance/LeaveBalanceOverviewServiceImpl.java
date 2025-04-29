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

import java.time.LocalDate;
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
    public LeaveBalanceOverviewDto getCurrentUserLeaveOverview(String userId, String leaveTypeId) {
        try {

            User user = securityUtils.getCurrentUser();
            LeaveBalance balance = leaveBalanceRepository.findByUser(user)
                    .orElseThrow(() -> new ApiRequestException("Leave balance not found"));

            LeavePolicy policy = leavePolicyRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new ApiRequestException("Leave policy not found"));
            LeaveType leaveType = leaveTypeRepository.findById(leaveTypeId)
                    .orElseThrow(() -> new ApiRequestException("Leave type not found"));


            LocalDate startOfYear = LocalDate.now().withDayOfYear(1);
            LocalDate endOfYear = LocalDate.now().withMonth(12).withDayOfMonth(31);

            List<LeaveApplication> approvedLeaves = leaveApplicationRepository
                    .findByUserAndLeaveTypeAndStatusAndStartDateBetween(
                            user, leaveType, LeaveApplicationStatus.APPROVED,
                            startOfYear, endOfYear);

            long daysUsed = approvedLeaves.stream()
                    .mapToLong(app -> app.isHalfDay() ? 1 :
                            DateUtils.calculateWorkingDays(app.getStartDate(), app.getEndDate(), HolidayConstants.HOLIDAYS_2025))
                    .sum();

            double daysLimit = balance.getTotalEntitledDays();
            double remaining = daysLimit - daysUsed;

            return new LeaveBalanceOverviewDto(
                    remaining,
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

            LocalDate startOfYear = LocalDate.now().withDayOfYear(1);
            LocalDate endOfYear = LocalDate.now().withMonth(12).withDayOfMonth(31);

            List<LeaveApplication> approvedLeaves = leaveApplicationRepository
                    .findByUserAndLeaveTypeAndStatusAndStartDateBetween(
                            user, leaveType, LeaveApplicationStatus.APPROVED,
                            startOfYear, endOfYear);

            long daysUsed = approvedLeaves.stream()
                    .mapToLong(app -> app.isHalfDay() ? 1 :
                            DateUtils.calculateWorkingDays(app.getStartDate(), app.getEndDate(), HolidayConstants.HOLIDAYS_2025))
                    .sum();

            double daysLimit = leaveType.getDaysLimit();
            double remaining = daysLimit - daysUsed;

            return new LeaveBalanceOverviewDto(remaining, daysLimit, 0);
        } catch (Exception e) {
            throw new ApiRequestException("Failed to get custom leave balance: " + e.getMessage());
        }
    }
}
