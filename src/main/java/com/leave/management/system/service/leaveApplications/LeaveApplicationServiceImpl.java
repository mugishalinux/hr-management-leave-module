package com.leave.management.system.service.leaveApplications;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leave.management.system.constants.HolidayConstants;
import com.leave.management.system.constants.HolidayDescriptionConstants;
import com.leave.management.system.dto.leaveApplication.HolidayResponseDto;
import com.leave.management.system.dto.leaveApplication.LeaveApplicationDto;
import com.leave.management.system.dto.response.ResponseDto;
import com.leave.management.system.enums.LeaveApplicationStatus;
import com.leave.management.system.enums.UserPermission;
import com.leave.management.system.exceptions.ApiRequestException;
import com.leave.management.system.kafka.KafkaProducerService;
import com.leave.management.system.model.LeaveApplication;
import com.leave.management.system.model.LeaveBalance;
import com.leave.management.system.model.LeaveType;
import com.leave.management.system.model.User;
import com.leave.management.system.repository.LeaveApplicationRepository;
import com.leave.management.system.repository.LeaveBalanceRepository;
import com.leave.management.system.repository.LeaveTypeRepository;
import com.leave.management.system.repository.UserRepository;
import com.leave.management.system.security.SecurityUtils;
import com.leave.management.system.util.helpers.DateUtils;
import com.leave.management.system.util.helpers.LeaveValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.util.List;

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

            boolean exists = leaveApplicationRepository.existsByUserAndLeaveTypeAndStatus(currentUser, leaveType, LeaveApplicationStatus.PENDING);
            if (exists) {
                throw new ApiRequestException("You already have a pending application for this leave type");
            }

            // Prevent overlapping leave dates regardless of type or status
            List<LeaveApplication> existingApplications = leaveApplicationRepository.findAllByUser(currentUser, Pageable.unpaged()).getContent();
            for (LeaveApplication existing : existingApplications) {
                boolean overlap = !(dto.getEndDate().isBefore(existing.getStartDate()) || dto.getStartDate().isAfter(existing.getEndDate()));
                if (overlap) {
                    throw new ApiRequestException("You already have a leave application in the date range " +
                            existing.getStartDate() + " to " + existing.getEndDate());
                }
            }

            if (leaveType.isAffectsBalance()) {
                LeaveBalance balance = leaveBalanceRepository.findByUser(currentUser)
                        .orElseThrow(() -> new ApiRequestException("Leave balance not found"));

                long daysRequested = DateUtils.calculateWorkingDays(dto.getStartDate(), dto.getEndDate(), HolidayConstants.HOLIDAYS_2025);
                if (dto.isHalfDay()) {
                    daysRequested = 1;
                }

                if (balance.getRemainingDays() < daysRequested) {
                    if(balance.getRemainingDays() == 0.0){
                        throw new ApiRequestException("You don't have enough leave balance.");
                    }else{
                        throw new ApiRequestException("You don't have enough leave balance. You have only " + balance.getRemainingDays() + " days left.");
                    }

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
            // Publish to Kafka topic
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
    public List<LeaveApplication> getCurrentTeamLeaves() {
        try{
            User currentUser = securityUtils.getCurrentUser();
            if (currentUser.getTeam() == null) {
                throw new ApiRequestException("You don't have a team");
            }
            LocalDate today = LocalDate.now();
            return leaveApplicationRepository.findByUser_TeamAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                    currentUser.getTeam(), LeaveApplicationStatus.APPROVED, today, today);
        } catch (Exception e) {
            throw new ApiRequestException(e.getMessage());
        }

    }

    @Override
    public LeaveApplication getLeaveApplicationById(String id) {

        return leaveApplicationRepository.findById(id)
                .orElseThrow(() -> new ApiRequestException("Leave application not found"));
    }

    @Override
    public ResponseDto updateLeaveApplication(String id, LeaveApplicationDto dto) {
        LeaveApplication application = leaveApplicationRepository.findById(id)
                .orElseThrow(() -> new ApiRequestException("Leave application not found"));

        User currentUser = securityUtils.getCurrentUser();

        LeaveType leaveType = leaveTypeRepository.findById(dto.getLeaveTypeId())
                .orElseThrow(() -> new ApiRequestException("Leave type not found"));

        // Validate application (date range, reason, attachment, etc.)
        LeaveValidationUtils.validateLeaveApplication(dto, leaveType);

        // Prevent duplicate pending application (excluding current one)
        boolean exists = leaveApplicationRepository.existsByUserAndLeaveTypeAndStatus(currentUser, leaveType, LeaveApplicationStatus.PENDING)
                && !application.getId().equals(id);

        if (exists) {
            throw new ApiRequestException("You already have another pending application for this leave type");
        }

        // Validate leave balance if applicable
        if (leaveType.isAffectsBalance()) {
            LeaveBalance balance = leaveBalanceRepository.findByUser(currentUser)
                    .orElseThrow(() -> new ApiRequestException("Leave balance not found"));

            long daysRequested = DateUtils.calculateWorkingDays(dto.getStartDate(), dto.getEndDate(), HolidayConstants.HOLIDAYS_2025);
            if (dto.isHalfDay()) {
                daysRequested = 1;
            }

            if (balance.getRemainingDays() < daysRequested) {
                throw new ApiRequestException("Insufficient leave balance. You have only " + balance.getRemainingDays() + " days left.");
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

        return new ResponseDto(HttpStatus.OK, "Leave application " + status.name().toLowerCase(), application.getId());
    }
    catch (Exception e) {
        throw new ApiRequestException(e.getMessage());}
    }

}