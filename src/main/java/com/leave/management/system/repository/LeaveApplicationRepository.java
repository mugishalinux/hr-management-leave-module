package com.leave.management.system.repository;

import com.leave.management.system.dto.leaveApplication.TeamLeaveCalendarDto;
import com.leave.management.system.enums.LeaveApplicationStatus;
import com.leave.management.system.model.LeaveApplication;
import com.leave.management.system.model.LeaveType;
import com.leave.management.system.model.Team;
import com.leave.management.system.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, String> {
    boolean existsByUserAndLeaveTypeAndStatus(User user, LeaveType leaveType, LeaveApplicationStatus status);
    Page<LeaveApplication> findAllByUser(User user, Pageable pageable);
    List<LeaveApplication> findByUser_TeamAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Team team, LeaveApplicationStatus status, LocalDate start, LocalDate end);
    List<LeaveApplication> findByUserAndLeaveTypeAndStatusAndStartDateBetween(
            User user,
            LeaveType leaveType,
            LeaveApplicationStatus status,
            LocalDate startDate,
            LocalDate endDate
    );
    Page<LeaveApplication> findAllByStatus(LeaveApplicationStatus status, Pageable pageable);
    Page<LeaveApplication> findByUser_TeamAndStatus(Team team, LeaveApplicationStatus status, Pageable pageable);
    List<LeaveApplication> findAllByUserAndLeaveTypeAndStatus(User user, LeaveType leaveType, LeaveApplicationStatus status);
    Page<LeaveApplication> findByUser_Team(Team team, Pageable pageable);
    Page<LeaveApplication> findByUser(User user, Pageable pageable);
    @Query("SELECT new com.leave.management.system.dto.leaveApplication.TeamLeaveCalendarDto(" +
            "u.id, u.fullName, " +
            "t.name, d.name, " +
            "u.profile, la.startDate, la.endDate, lt.name, la.status) " +
            "FROM LeaveApplication la " +
            "JOIN la.user u " +
            "LEFT JOIN u.team t " +
            "LEFT JOIN u.department d " +
            "JOIN la.leaveType lt " +
            "WHERE (:teamId IS NULL OR t.id = :teamId) " +
            "AND (:departmentId IS NULL OR d.id = :departmentId) " +
            "AND la.status = 'APPROVED'")
    List<TeamLeaveCalendarDto> findTeamLeaveCalendar(@Param("teamId") String teamId,
                                                     @Param("departmentId") String departmentId);


}
