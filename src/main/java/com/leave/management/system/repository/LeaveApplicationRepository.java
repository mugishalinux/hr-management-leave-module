package com.leave.management.system.repository;

import com.leave.management.system.enums.LeaveApplicationStatus;
import com.leave.management.system.model.LeaveApplication;
import com.leave.management.system.model.LeaveType;
import com.leave.management.system.model.Team;
import com.leave.management.system.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, String> {
    boolean existsByUserAndLeaveTypeAndStatus(User user, LeaveType leaveType, LeaveApplicationStatus status);
    Page<LeaveApplication> findAllByUser(User user, Pageable pageable);
    List<LeaveApplication> findByUser_TeamAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Team team, LeaveApplicationStatus status, LocalDate start, LocalDate end);
}
