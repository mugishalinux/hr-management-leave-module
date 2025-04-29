package com.leave.management.system.service.leaveApplications;

import com.leave.management.system.kafka.KafkaProducerService;
import com.leave.management.system.model.LeaveApplication;
import com.leave.management.system.model.TeamCalendarDto;
import com.leave.management.system.model.User;
import com.leave.management.system.repository.LeaveApplicationRepository;
import com.leave.management.system.repository.LeaveBalanceRepository;
import com.leave.management.system.repository.LeaveTypeRepository;
import com.leave.management.system.repository.UserRepository;
import com.leave.management.system.security.SecurityUtils;
import com.leave.management.system.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
@Service

public class TeamLeavesImpl {
    private final LeaveApplicationRepository leaveApplicationRepository;

    public TeamLeavesImpl(LeaveApplicationRepository leaveApplicationRepository, LeaveTypeRepository leaveTypeRepository, LeaveBalanceRepository leaveBalanceRepository, UserRepository userRepository, SecurityUtils securityUtils, KafkaProducerService kafkaProducerService, NotificationService notificationService) {
        this.leaveApplicationRepository = leaveApplicationRepository;
    }

    public List<TeamCalendarDto> getTeamCalendar(String departmentId, String teamId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            LocalDate today = LocalDate.now();
            startDate = today;
            endDate = today;
        }

        List<LeaveApplication> approvedLeaves = leaveApplicationRepository.findApprovedByDateRange(startDate, endDate);

        return approvedLeaves.stream()
                .filter(leave -> {
                    boolean departmentMatch = (departmentId == null) ||
                            (leave.getUser().getDepartment() != null && leave.getUser().getDepartment().getId().equals(departmentId));
                    boolean teamMatch = (teamId == null) ||
                            (leave.getUser().getTeam() != null && leave.getUser().getTeam().getId().equals(teamId));
                    return departmentMatch && teamMatch;
                })
                .map(leave -> {
                    User user = leave.getUser();
                    return new TeamCalendarDto(
                            user.getFullName(),
                            user.getEmail(),
                            user.getProfile(),
                            user.getTeam() != null ? user.getTeam().getName() : null,
                            user.getDepartment() != null ? user.getDepartment().getName() : null,
                            leave.getStartDate(),
                            leave.getEndDate(),
                            leave.isHalfDay()
                    );
                })
                .toList();
    }


}
