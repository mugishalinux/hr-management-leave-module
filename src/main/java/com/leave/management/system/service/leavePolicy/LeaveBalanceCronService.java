package com.leave.management.system.service.leavePolicy;

import com.leave.management.system.exceptions.ApiRequestException;
import com.leave.management.system.model.LeaveBalance;
import com.leave.management.system.model.LeavePolicy;
import com.leave.management.system.model.User;
import com.leave.management.system.repository.LeaveBalanceRepository;
import com.leave.management.system.repository.LeavePolicyRepository;
import com.leave.management.system.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeaveBalanceCronService {

    private final UserRepository userRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeavePolicyRepository leavePolicyRepository;

//    @Scheduled(cron = "0 0 0 * * *") // Every day at midnight
    @Scheduled(cron= "0/5 * * ? * *")
    @Transactional
    public void updateLeaveBalancesDaily() {
        try{
            log.info("Running daily leave balance update task...");

            LeavePolicy policy = leavePolicyRepository.findAll()
                    .stream().findFirst()
                    .orElseThrow(() -> new ApiRequestException("Leave policy not configured"));

            double accrualRate = policy.getAccrualRate();
            double maxCarryForward = policy.getMaxCarryForwardDays();

            LocalDate today = LocalDate.now();
//        LocalDate today = LocalDate.of(2025, 12, 31);
            LocalDate yearStart = LocalDate.of(today.getYear(), 1, 1);
            long monthsElapsed = ChronoUnit.MONTHS.between(yearStart.withDayOfMonth(1), today.withDayOfMonth(1)) + 1;

            // For testing with a custom date range, uncomment and adjust this block:
        /*
        LocalDate customStart = LocalDate.of(2025, 3, 2);
        LocalDate customEnd = LocalDate.of(2025, 4, 29);
        monthsElapsed = ChronoUnit.MONTHS.between(customStart.withDayOfMonth(1), customEnd.withDayOfMonth(1)) + 1;
        */

            double totalAccrued = monthsElapsed * accrualRate;

            List<User> users = userRepository.findAll();
            for (User user : users) {
                LeaveBalance balance = leaveBalanceRepository.findByUser(user).orElse(
                        LeaveBalance.builder()
                                .user(user)
                                .remainingDays(0)
                                .totalEntitledDays(0)
                                .build()
                );

                if (today.getMonthValue() == 1 && today.getDayOfMonth() == 1) {
                    double carryForward = Math.min(balance.getRemainingDays(), maxCarryForward);
                    balance.setRemainingDays(carryForward);
                    balance.setTotalEntitledDays(0); // Reset entitlement for new year
                }

                balance.setTotalEntitledDays(Math.round(totalAccrued));

                // Optionally: update remainingDays only if it's a new balance
                if (balance.getId() == null) {
                    balance.setRemainingDays(totalAccrued);
                }

                leaveBalanceRepository.save(balance);
            }

            log.info("Leave balance update complete for all users.");
        } catch (Exception e) {
            throw new ApiRequestException(e.getMessage());
        }

    }
}
