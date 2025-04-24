package com.leave.management.system.service.leaveApplications;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leave.management.system.constants.HolidayConstants;
import com.leave.management.system.constants.HolidayDescriptionConstants;
import com.leave.management.system.dto.leaveApplication.*;
import com.leave.management.system.dto.response.ResponseDto;
import com.leave.management.system.enums.LeaveApplicationStatus;
import com.leave.management.system.enums.UserPermission;
import com.leave.management.system.exceptions.ApiRequestException;
import com.leave.management.system.kafka.KafkaProducerService;
import com.leave.management.system.model.*;
import com.leave.management.system.repository.LeaveApplicationRepository;
import com.leave.management.system.repository.LeaveBalanceRepository;
import com.leave.management.system.repository.LeaveTypeRepository;
import com.leave.management.system.repository.UserRepository;
import com.leave.management.system.security.SecurityUtils;
import com.leave.management.system.util.helpers.DateUtils;
import com.leave.management.system.util.helpers.LeaveValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveApplicationServiceImpl implements LeaveApplicationService {

    private final LeaveApplicationRepository leaveApplicationRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final KafkaProducerService kafkaProducerService;

    @Override
    public ResponseDto applyForLeave(LeaveApplicationDto dto) {
        try {
            User currentUser = securityUtils.getCurrentUser();
            LeaveType leaveType = leaveTypeRepository.findById(dto.getLeaveTypeId())
                    .orElseThrow(() -> new ApiRequestException("Leave type not found"));

            LeaveValidationUtils.validateLeaveApplication(dto, leaveType);

            boolean exists = leaveApplicationRepository.existsByUserAndLeaveTypeAndStatus(
                    currentUser, leaveType, LeaveApplicationStatus.PENDING);
            if (exists) {
                throw new ApiRequestException("You already have a pending application for this leave type");
            }

            // Prevent overlapping leave dates regardless of type or status
            List<LeaveApplication> existingApplications = leaveApplicationRepository
                    .findAllByUser(currentUser, Pageable.unpaged()).getContent();
            for (LeaveApplication existing : existingApplications) {
                boolean overlap = !(dto.getEndDate().isBefore(existing.getStartDate()) ||
                        dto.getStartDate().isAfter(existing.getEndDate()));
                if (overlap) {
                    throw new ApiRequestException(STR."You already have a leave application in the date range \{existing.getStartDate()} to \{existing.getEndDate()}");
                }
            }

            if (leaveType.isAffectsBalance()) {
                LeaveBalance balance = leaveBalanceRepository.findByUser(currentUser)
                        .orElseThrow(() -> new ApiRequestException("Leave balance not found"));

                long daysRequested = DateUtils.calculateWorkingDays(
                        dto.getStartDate(), dto.getEndDate(), HolidayConstants.HOLIDAYS_2025);
                if (dto.isHalfDay()) {
                    daysRequested = 1;
                }

                if (balance.getRemainingDays() < daysRequested) {
                    if (balance.getRemainingDays() == 0.0) {
                        throw new ApiRequestException("You don't have enough leave balance.");
                    } else {
                        throw new ApiRequestException(STR."You don't have enough leave balance. You have only \{balance.getRemainingDays()} days left.");
                    }
                }

            } else {
                System.out.println("*********************************");
                // Leave does NOT affect balance, check total usage against daysLimit
                LocalDate startOfYear = LocalDate.now().withDayOfYear(1);

                LocalDate endOfYear = LocalDate.now().withMonth(12).withDayOfMonth(31);
                System.out.println(STR."start year : \{startOfYear}");
                System.out.println(STR."end year : \{endOfYear}");
                List<LeaveApplication> approvedSameType = leaveApplicationRepository
                        .findByUserAndLeaveTypeAndStatusAndStartDateBetween(
                                currentUser, leaveType, LeaveApplicationStatus.APPROVED,
                                startOfYear, endOfYear);

                long usedDays = 0;
                for (LeaveApplication approved : approvedSameType) {
                    if (approved.isHalfDay()) {
                        usedDays += 1;
                    } else {
                        usedDays += DateUtils.calculateWorkingDays(
                                approved.getStartDate(), approved.getEndDate(), HolidayConstants.HOLIDAYS_2025);
                    }
                }
                System.out.println(STR."used days : \{usedDays}");

                long currentRequestDays = dto.isHalfDay() ? 1 :
                        DateUtils.calculateWorkingDays(dto.getStartDate(), dto.getEndDate(), HolidayConstants.HOLIDAYS_2025);

                System.out.println(STR."current request days : \{currentRequestDays}");

                if ((usedDays + currentRequestDays) > leaveType.getDaysLimit()) {
                    throw new ApiRequestException(STR."You have exceeded the maximum allowed days for this leave type. Limit: \{leaveType.getDaysLimit()}");
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

            String leadEmail = "";
            String teamLeaderNames = "";
            if (currentUser.getTeam() != null && currentUser.getTeam().getLead() != null) {
                User lead = currentUser.getTeam().getLead();
                if (!lead.getId().equals(currentUser.getId())) {
                    leadEmail = lead.getEmail();
                    teamLeaderNames = lead.getFullName();
                }
            }

            String messageSender = currentUser.getFullName() + "," + leadEmail + "," + leaveType.getName() + "," + teamLeaderNames;
            kafkaProducerService.sendMessage("leave-submitted", messageSender);

            return new ResponseDto(HttpStatus.CREATED, "Leave application submitted", application.getId());

        } catch (Exception e) {
            throw new ApiRequestException(e.getMessage());
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
            throw new ApiRequestException("Error while fetching team members on leave: " + e.getMessage());
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
                    .orElseThrow(() -> new ApiRequestException("Leave application not found"));
            if(!application.getStatus().equals(LeaveApplicationStatus.PENDING)) {
                throw new ApiRequestException(STR."You can't update leave application that already \{application.getStatus().name()}");
            }
            LeaveType leaveType = leaveTypeRepository.findById(dto.getLeaveTypeId())
                    .orElseThrow(() -> new ApiRequestException("Leave type not found"));

            LeaveValidationUtils.validateLeaveApplication(dto, leaveType);

            boolean exists = leaveApplicationRepository.existsByUserAndLeaveTypeAndStatus(currentUser, leaveType, LeaveApplicationStatus.PENDING)
                    && !application.getId().equals(id);

            if (exists) {
                throw new ApiRequestException("You already have another pending application for this leave type");
            }

            long daysRequested = dto.isHalfDay() ? 1 :
                    DateUtils.calculateWorkingDays(dto.getStartDate(), dto.getEndDate(), HolidayConstants.HOLIDAYS_2025);

            if (leaveType.isAffectsBalance()) {
                LeaveBalance balance = leaveBalanceRepository.findByUser(currentUser)
                        .orElseThrow(() -> new ApiRequestException("Leave balance not found"));

                if (balance.getRemainingDays() < daysRequested) {
                    throw new ApiRequestException("Insufficient leave balance. You have only " + balance.getRemainingDays() + " days left.");
                }
            } else {
                LocalDate startOfYear = LocalDate.now().withDayOfYear(1);
                LocalDate endOfYear = LocalDate.now().withMonth(12).withDayOfMonth(31);

                List<LeaveApplication> approvedSameType = leaveApplicationRepository
                        .findByUserAndLeaveTypeAndStatusAndStartDateBetween(
                                currentUser, leaveType, LeaveApplicationStatus.APPROVED,
                                startOfYear, endOfYear);

                long usedDays = 0;
                for (LeaveApplication approved : approvedSameType) {
                    if (!approved.getId().equals(id)) {
                        usedDays += approved.isHalfDay() ? 1 :
                                DateUtils.calculateWorkingDays(
                                        approved.getStartDate(), approved.getEndDate(), HolidayConstants.HOLIDAYS_2025);
                    }
                }

                if ((usedDays + daysRequested) > leaveType.getDaysLimit()) {
                    throw new ApiRequestException("You have exceeded the maximum allowed days for this leave type. Limit: "
                            + leaveType.getDaysLimit() + ", Already used: " + usedDays);
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
            return new ResponseDto(HttpStatus.OK, "Leave application updated", application.getId());
        } catch (Exception e) {
            throw new ApiRequestException(e.getMessage());
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
        try{
        User currentUser = securityUtils.getCurrentUser();

        if (!(currentUser.getPermissions().name().equals("ADMIN") || currentUser.getPermissions().name().equals("MANAGER"))) {
            throw new ApiRequestException("You are not authorized to approve or reject leave applications");
        }

        if(status == LeaveApplicationStatus.REJECTED && comment == null) {
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

        application.setStatus(status);
        application.setReviewComment(comment);
        application.setReviewedBy(currentUser);
        application.setUpdatedBy(currentUser);

        if (status == LeaveApplicationStatus.APPROVED && application.getLeaveType().isAffectsBalance()) {
            LeaveBalance balance = leaveBalanceRepository.findByUser(application.getUser())
                    .orElseThrow(() -> new ApiRequestException("Leave balance not found"));

            long daysRequested = DateUtils.calculateWorkingDays(application.getStartDate(), application.getEndDate(), HolidayConstants.HOLIDAYS_2025);
            if (application.isHalfDay()) {
                daysRequested = 1;
            }

            double remaining = balance.getRemainingDays() - daysRequested;
            if (remaining < 0) {
                throw new ApiRequestException("Cannot approve. User has insufficient balance.");
            }

            balance.setRemainingDays(remaining);
            leaveBalanceRepository.save(balance);
        }
        leaveApplicationRepository.save(application);
            String messageSender = application.getUser().getFullName() + "," + application.getUser().getEmail();
            // Publish to Kafka topic
            kafkaProducerService.sendMessage("leave-submitted", messageSender);

            /**
             save new notification for either approve or reject
             */
        return new ResponseDto(HttpStatus.OK, "Leave application " + status.name().toLowerCase(), application.getId());
    }
    catch (Exception e) {
        throw new ApiRequestException(e.getMessage());}
    }

}