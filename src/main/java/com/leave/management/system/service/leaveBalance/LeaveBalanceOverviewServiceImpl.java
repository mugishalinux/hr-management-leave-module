package com.leave.management.system.service.leaveBalance;

import com.leave.management.system.dto.leaveBalance.LeaveBalanceOverviewDto;
import com.leave.management.system.exceptions.ApiRequestException;
import com.leave.management.system.model.LeaveBalance;
import com.leave.management.system.model.LeavePolicy;
import com.leave.management.system.model.User;
import com.leave.management.system.repository.LeaveBalanceOverviewRepository;
import com.leave.management.system.repository.LeaveBalanceRepository;
import com.leave.management.system.repository.LeavePolicyRepository;
import com.leave.management.system.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeaveBalanceOverviewServiceImpl implements LeaveBalanceOverviewService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final SecurityUtils securityUtils;
    private final LeavePolicyRepository leavePolicyRepository;

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
}
