package com.leave.management.system.service.leaveBalance;

import com.leave.management.system.dto.leaveBalance.UpdateLeaveBalanceDto;
import com.leave.management.system.exceptions.ApiRequestException;
import com.leave.management.system.model.LeaveBalance;
import com.leave.management.system.model.User;
import com.leave.management.system.repository.LeaveBalanceRepository;
import com.leave.management.system.repository.UserRepository;
import com.leave.management.system.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeaveBalanceServiceImpl implements LeaveBalanceService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    @Override
    public LeaveBalance updateLeaveBalance(UpdateLeaveBalanceDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ApiRequestException("User not found"));

        LeaveBalance balance = leaveBalanceRepository.findByUser(user)
                .orElseThrow(() -> new ApiRequestException("Leave balance not found"));

        balance.setRemainingDays(dto.getRemainingDays());
        return leaveBalanceRepository.save(balance);
    }

    @Override
    public void createBalanceForUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiRequestException("User not found"));

        LeaveBalance balance = LeaveBalance.builder()
                .user(user)
                .totalEntitledDays(0)
                .remainingDays(0)
                .build();
        leaveBalanceRepository.save(balance);
    }
    @Override
    public LeaveBalance getMyLeaveBalance() {
        User currentUser = securityUtils.getCurrentUser();
        return leaveBalanceRepository.findByUser(currentUser)
                .orElseThrow(() -> new ApiRequestException("Leave balance not found for current user"));
    }
}
