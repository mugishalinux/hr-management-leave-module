package com.leave.management.system.repository;

import com.leave.management.system.model.LeaveBalance;
import com.leave.management.system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, String> {
    Optional<LeaveBalance> findByUser(User user);
}
