package com.leave.management.system.service.leaveApplications;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leave.management.system.constants.HolidayConstants;
import com.leave.management.system.constants.HolidayDescriptionConstants;
import com.leave.management.system.dto.leaveApplication.*;
import com.leave.management.system.dto.response.ResponseDto;
import com.leave.management.system.enums.LeaveApplicationStatus;
import com.leave.management.system.enums.NofiticationStatus;
import com.leave.management.system.exceptions.ApiRequestException;
import com.leave.management.system.kafka.KafkaProducerService;
import com.leave.management.system.model.*;
import com.leave.management.system.repository.LeaveApplicationRepository;
import com.leave.management.system.repository.LeaveBalanceRepository;
import com.leave.management.system.repository.LeaveTypeRepository;
import com.leave.management.system.repository.UserRepository;
import com.leave.management.system.security.SecurityUtils;
import com.leave.management.system.service.notification.NotificationService;
import com.leave.management.system.util.helpers.DateUtils;
import com.leave.management.system.util.helpers.LeaveValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveApplicationServiceImpl implements LeaveApplicationService {

    private final LeaveApplicationRepository leaveApplicationRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final KafkaProducerService kafkaProducerService;
    private final NotificationService notificationService;

    @Override
    public ResponseDto applyForLeave(LeaveApplicationDto dto) {
        try {
            User currentUser = securityUtils.getCurrentUser();
            LeaveType leaveType = leaveTypeRepository.findById(dto.getLeaveTypeId())
                    .orElseThrow(() -> new ApiRequestException("Leave type not found."));

            LeaveValidationUtils.validateLeaveApplication(dto, leaveType);

            boolean existsPending = leaveApplicationRepository.existsByUserAndLeaveTypeAndStatus(currentUser, leaveType, LeaveApplicationStatus.PENDING);
            if (existsPending) {
                throw new ApiRequestException("You already have a pending application for this leave type.");
            }

            List<LeaveApplication> existingApplications = leaveApplicationRepository
                    .findAllByUser(currentUser, Pageable.unpaged()).getContent();

            for (LeaveApplication existing : existingApplications) {
                boolean overlap = !(dto.getEndDate().isBefore(existing.getStartDate()) ||
                        dto.getStartDate().isAfter(existing.getEndDate()));
                if (overlap) {
                    throw new ApiRequestException("You already have a leave application between " + existing.getStartDate() + " and " + existing.getEndDate());
                }
            }

            long currentRequestDays = dto.isHalfDay() ? 1 :
                    DateUtils.calculateWorkingDays(dto.getStartDate(), dto.getEndDate(), HolidayConstants.HOLIDAYS_2025);

            LocalDate startOfYear = LocalDate.now().withDayOfYear(1);
            LocalDate endOfYear = LocalDate.now().withMonth(12).withDayOfMonth(31);

            List<LeaveApplication> approvedThisYear = leaveApplicationRepository
                    .findByUserAndLeaveTypeAndStatusAndStartDateBetween(
                            currentUser, leaveType, LeaveApplicationStatus.APPROVED,
                            startOfYear, endOfYear);

            long usedDays = approvedThisYear.stream()
                    .mapToLong(a -> a.isHalfDay() ? 1 :
                            DateUtils.calculateWorkingDays(a.getStartDate(), a.getEndDate(), HolidayConstants.HOLIDAYS_2025))
                    .sum();

            if (leaveType.isAffectsBalance()) {
                LeaveBalance balance = leaveBalanceRepository.findByUser(currentUser)
                        .orElseThrow(() -> new ApiRequestException("Leave balance not found."));
                if ((usedDays + currentRequestDays) > balance.getTotalEntitledDays()) {
                    throw new ApiRequestException("Exceeded entitled days. Entitlement: " + balance.getTotalEntitledDays()
                            + ", Used: " + usedDays + ", Requested: " + currentRequestDays);
                }
            } else {
                if ((usedDays + currentRequestDays) > leaveType.getDaysLimit()) {
                    throw new ApiRequestException("Exceeded leave type limit. Limit: " + leaveType.getDaysLimit()
                            + ", Used: " + usedDays + ", Requested: " + currentRequestDays);
                }
            }

            LeaveApplication application = new LeaveApplication();
            application.setLeaveType(leaveType);
            application.setUser(currentUser);
            application.setStartDate(dto.getStartDate());
            application.setEndDate(dto.getEndDate());
            application.setHalfDay(dto.isHalfDay());
            application.setReason(dto.getReason());
            application.setAttachmentPath(dto.getAttachmentPath());
            application.setStatus(LeaveApplicationStatus.PENDING);
            application.setCreatedBy(currentUser);
            application.setUpdatedBy(currentUser);

            leaveApplicationRepository.save(application);

            // Notification
            String notificationMessage = STR."\{currentUser.getFullName()} your leave application was successfully submitted.";
            Notification notification = Notification.builder()
                    .leaveApplication(application)
                    .description(notificationMessage)
                    .status(NofiticationStatus.UNREAD)
                    .build();
            notificationService.saveNotification(notification);

            // Kafka
            if (currentUser.getTeam() != null && currentUser.getTeam().getLead() != null) {
                User lead = currentUser.getTeam().getLead();
                if (!lead.getId().equals(currentUser.getId())) {
                    kafkaProducerService.sendMessage("leave-submitted", STR."\{currentUser.getFullName()},\{lead.getEmail()},\{leaveType.getName()},\{lead.getFullName()}");
                }
            }

            return new ResponseDto(HttpStatus.CREATED, "Leave application submitted", application.getId());

        } catch (Exception e) {
            log.error("Error applying for leave", e);
            throw new ApiRequestException("Failed to apply for leave: " + e.getMessage());
        }
    }

    @Override
    public Page<LeaveApplication> getAllLeaveApplications(int page, int size, String sortBy) {
        return leaveApplicationRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy)));
    }
    @Override
    public Page<LeaveApplication> getMyLeaveApplications(int page, int size, String sortBy) {
        User currentUser = securityUtils.getCurrentUser();
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        return leaveApplicationRepository.findAllByUser(currentUser, pageable);
    }

    @Override
    public List<HolidayResponseDto> getUpcomingHolidays() {
        try {
            LocalDate today = LocalDate.now();
            return HolidayDescriptionConstants.HOLIDAYS_2025_WITH_DESCRIPTION.entrySet().stream()
                    .filter(entry -> entry.getKey().isAfter(today))
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> new HolidayResponseDto(entry.getKey(), entry.getValue()))
                    .toList();
        } catch (Exception e) {
            throw new ApiRequestException("Failed to fetch holidays: " + e.getMessage());
        }
    }

    @Override
    public TeamOnLeaveDto getTodayTeamMembersOnLeave() {
        try {
            User currentUser = securityUtils.getCurrentUser();
            Team team = currentUser.getTeam();

            if (team == null) {
                throw new ApiRequestException("You are not assigned to any team.");
            }

            LocalDate today = LocalDate.now();

            List<LeaveApplication> onLeaveList = leaveApplicationRepository
                    .findByUser_TeamAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                            team, LeaveApplicationStatus.APPROVED, today, today
                    );

            List<String> userNamesOnLeave = onLeaveList.stream()
                    .map(app -> app.getUser().getFullName())
                    .distinct()
                    .toList();

            return new TeamOnLeaveDto(team.getName(), userNamesOnLeave);
        } catch (Exception e) {
            throw new ApiRequestException(e.getMessage());
        }
    }

    @Override
    public Page<LeaveApplication> getPendingApplicationsForApprover(Pageable pageable) {
        try {
            User currentUser = securityUtils.getCurrentUser();

            // ADMIN: Return all leave applications regardless of status
            if (currentUser.getPermissions().name().equals("ADMIN")) {
                return leaveApplicationRepository.findAll(pageable);
            }

            // MANAGER: Return all leave applications for users within the manager's team
            if (currentUser.getPermissions().name().equals("MANAGER")) {
                Team team = currentUser.getTeam();
                if (team == null) {
                    return new PageImpl<>(Collections.emptyList(), pageable, 0);

                }
                return leaveApplicationRepository.findByUser_Team(team, pageable);
            }

            // STAFF: Return all leave applications for the current user only
            if (currentUser.getPermissions().name().equals("STAFF")) {
                return leaveApplicationRepository.findByUser(currentUser, pageable);
            }

            throw new ApiRequestException("Unauthorized access.");
        } catch (Exception e) {
            throw new ApiRequestException(e.getMessage());
        }
    }


    @Override
    public LeaveApplication getLeaveApplicationById(String id) {

        return leaveApplicationRepository.findById(id)
                .orElseThrow(() -> new ApiRequestException("Leave application not found"));
    }

    public ResponseDto updateLeaveApplication(String id, LeaveApplicationDto dto) {
        try {
            User currentUser = securityUtils.getCurrentUser();
            LeaveApplication application = leaveApplicationRepository.findById(id)
                    .orElseThrow(() -> new ApiRequestException("Leave application not found."));

            if (!application.getStatus().equals(LeaveApplicationStatus.PENDING)) {
                throw new ApiRequestException("Cannot update a leave application that is already " + application.getStatus().name());
            }

            LeaveType leaveType = leaveTypeRepository.findById(dto.getLeaveTypeId())
                    .orElseThrow(() -> new ApiRequestException("Leave type not found."));

            LeaveValidationUtils.validateLeaveApplication(dto, leaveType);

            boolean existsAnother = leaveApplicationRepository.existsByUserAndLeaveTypeAndStatus(currentUser, leaveType, LeaveApplicationStatus.PENDING)
                    && !application.getId().equals(id);
            if (existsAnother) {
                throw new ApiRequestException("You already have another pending application for this leave type.");
            }

            // Check overlap excluding itself
            List<LeaveApplication> existingApplications = leaveApplicationRepository
                    .findAllByUser(currentUser, Pageable.unpaged()).getContent();
            for (LeaveApplication existing : existingApplications) {
                if (!existing.getId().equals(id)) {
                    boolean overlap = !(dto.getEndDate().isBefore(existing.getStartDate()) ||
                            dto.getStartDate().isAfter(existing.getEndDate()));
                    if (overlap) {
                        throw new ApiRequestException("Another leave application already exists from " + existing.getStartDate() + " to " + existing.getEndDate());
                    }
                }
            }

            long currentRequestDays = dto.isHalfDay() ? 1 :
                    DateUtils.calculateWorkingDays(dto.getStartDate(), dto.getEndDate(), HolidayConstants.HOLIDAYS_2025);

            LocalDate startOfYear = LocalDate.now().withDayOfYear(1);
            LocalDate endOfYear = LocalDate.now().withMonth(12).withDayOfMonth(31);

            List<LeaveApplication> approvedThisYear = leaveApplicationRepository
                    .findByUserAndLeaveTypeAndStatusAndStartDateBetween(
                            currentUser, leaveType, LeaveApplicationStatus.APPROVED,
                            startOfYear, endOfYear);

            long usedDays = approvedThisYear.stream()
                    .filter(existing -> !existing.getId().equals(id)) // exclude this one
                    .mapToLong(existing -> existing.isHalfDay() ? 1 :
                            DateUtils.calculateWorkingDays(existing.getStartDate(), existing.getEndDate(), HolidayConstants.HOLIDAYS_2025))
                    .sum();

            if (leaveType.isAffectsBalance()) {
                LeaveBalance balance = leaveBalanceRepository.findByUser(currentUser)
                        .orElseThrow(() -> new ApiRequestException("Leave balance not found."));
                if ((usedDays + currentRequestDays) > balance.getTotalEntitledDays()) {
                    throw new ApiRequestException("Exceeded entitled days. Entitlement: " + balance.getTotalEntitledDays()
                            + ", Used: " + usedDays + ", Requested: " + currentRequestDays);
                }
            } else {
                if ((usedDays + currentRequestDays) > leaveType.getDaysLimit()) {
                    throw new ApiRequestException("Exceeded leave type limit. Limit: " + leaveType.getDaysLimit()
                            + ", Used: " + usedDays + ", Requested: " + currentRequestDays);
                }
            }

            application.setLeaveType(leaveType);
            application.setStartDate(dto.getStartDate());
            application.setEndDate(dto.getEndDate());
            application.setHalfDay(dto.isHalfDay());
            application.setReason(dto.getReason());
            application.setAttachmentPath(dto.getAttachmentPath());
            application.setUpdatedBy(currentUser);

            leaveApplicationRepository.save(application);

            Notification notification = Notification.builder()
                    .leaveApplication(application)
                    .description(STR."\{currentUser.getFullName()} your leave application was successfully updated.")
                    .status(NofiticationStatus.UNREAD)
                    .build();
            notificationService.saveNotification(notification);

            return new ResponseDto(HttpStatus.OK, "Leave application updated successfully.", application.getId());

        } catch (Exception e) {
            log.error("Error updating leave application", e);
            throw new ApiRequestException("Failed to update leave application: " + e.getMessage());
        }
    }


    @Override
    public ResponseDto deleteLeaveApplication(String id) {
        LeaveApplication application = leaveApplicationRepository.findById(id)
                .orElseThrow(() -> new ApiRequestException("Leave application not found"));

        leaveApplicationRepository.delete(application);
        return new ResponseDto(HttpStatus.OK, "Leave application deleted", id);
    }

    @Override
    public ResponseDto approveOrRejectLeave(String applicationId, LeaveApplicationStatus status, String comment) {
        try {
            User currentUser = securityUtils.getCurrentUser();

            if (!(currentUser.getPermissions().name().equals("ADMIN") || currentUser.getPermissions().name().equals("MANAGER"))) {
                throw new ApiRequestException("You are not authorized to approve or reject leave applications");
            }

            if (status == LeaveApplicationStatus.REJECTED && (comment == null || comment.trim().isEmpty())) {
                throw new ApiRequestException("Comment cannot be empty, since leave application is rejected");
            }

            LeaveApplication application = leaveApplicationRepository.findById(applicationId)
                    .orElseThrow(() -> new ApiRequestException("Leave application not found"));

            if (currentUser.getPermissions().name().equals("MANAGER") &&
                    (currentUser.getTeam() == null || application.getUser().getTeam() == null ||
                            !currentUser.getTeam().getId().equals(application.getUser().getTeam().getId()))) {
                throw new ApiRequestException("Managers can only approve/reject leave requests within their own team");
            }

            if (!application.getStatus().equals(LeaveApplicationStatus.PENDING)) {
                throw new ApiRequestException("Only pending applications can be approved or rejected");
            }

            if (status == LeaveApplicationStatus.APPROVED) {
                LeaveType leaveType = application.getLeaveType();
                long daysRequested = application.isHalfDay() ? 1 :
                        DateUtils.calculateWorkingDays(application.getStartDate(), application.getEndDate(), HolidayConstants.HOLIDAYS_2025);

                if (leaveType.isAffectsBalance()) {
                    LeaveBalance balance = leaveBalanceRepository.findByUser(application.getUser())
                            .orElseThrow(() -> new ApiRequestException("Leave balance not found."));

                    long startOfYearUsedDays = leaveApplicationRepository
                            .findByUserAndLeaveTypeAndStatusAndStartDateBetween(
                                    application.getUser(), leaveType, LeaveApplicationStatus.APPROVED,
                                    LocalDate.now().withDayOfYear(1),
                                    LocalDate.now().withMonth(12).withDayOfMonth(31)
                            )
                            .stream()
                            .mapToLong(existing -> existing.isHalfDay() ? 1 :
                                    DateUtils.calculateWorkingDays(existing.getStartDate(), existing.getEndDate(), HolidayConstants.HOLIDAYS_2025))
                            .sum();

                    if ((startOfYearUsedDays + daysRequested) > balance.getTotalEntitledDays()) {
                        throw new ApiRequestException("Approval failed: User would exceed total entitled days " + balance.getTotalEntitledDays() + " Currently used: " + startOfYearUsedDays + " Requested: " + daysRequested);
                    }

                } else {
                    // Does not affect balance -> check against leave type limit
                    long startOfYearUsedDays = leaveApplicationRepository
                            .findByUserAndLeaveTypeAndStatusAndStartDateBetween(
                                    application.getUser(), leaveType, LeaveApplicationStatus.APPROVED,
                                    LocalDate.now().withDayOfYear(1),
                                    LocalDate.now().withMonth(12).withDayOfMonth(31)
                            )
                            .stream()
                            .mapToLong(existing -> existing.isHalfDay() ? 1 :
                                    DateUtils.calculateWorkingDays(existing.getStartDate(), existing.getEndDate(), HolidayConstants.HOLIDAYS_2025))
                            .sum();

                    if ((startOfYearUsedDays + daysRequested) > leaveType.getDaysLimit()) {
                        throw new ApiRequestException("Approval failed: User would exceed allowed limit for this leave type " + leaveType.getDaysLimit() + " days :" + " Already used: " + startOfYearUsedDays + " Requested " + daysRequested);
                    }
                }
            }

            String notificationMessage = STR."\{application.getUser().getFullName()} Your leave application was \{status}";
            application.setStatus(status);
            application.setReviewComment(comment);
            application.setReviewedBy(currentUser);
            application.setUpdatedBy(currentUser);
            leaveApplicationRepository.save(application);

            kafkaProducerService.sendMessage("leave-submitted", STR."\{application.getUser().getFullName()},\{application.getUser().getEmail()}");

            Notification notification = Notification.builder()
                    .leaveApplication(application)
                    .description(notificationMessage)
                    .status(NofiticationStatus.UNREAD)
                    .build();
            notificationService.saveNotification(notification);

            return new ResponseDto(HttpStatus.OK, "Leave application " + status.name().toLowerCase(), application.getId());

        } catch (Exception e) {
            throw new ApiRequestException(e.getMessage());
        }
    }

    @Override
    public List<TeamLeaveCalendarDto> getTeamCalendar(String teamId, String departmentId) {
        List<TeamLeaveCalendarDto> result = leaveApplicationRepository.findTeamLeaveCalendar(teamId, departmentId);
        System.out.println("Results found: " + result.size());
        return result;
    }
}