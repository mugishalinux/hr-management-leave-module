package com.leave.management.system.util.helpers;

import com.leave.management.system.dto.leaveApplication.LeaveApplicationDto;
import com.leave.management.system.exceptions.ApiRequestException;
import com.leave.management.system.model.LeaveType;

import java.time.LocalDate;

public class LeaveValidationUtils {

    public static void validateLeaveApplication(LeaveApplicationDto dto, LeaveType leaveType) {
        if (dto.getStartDate() == null || dto.getEndDate() == null) {
            throw new ApiRequestException("Start date and end date must be provided");
        }

        LocalDate today = LocalDate.now();
        if (dto.getStartDate().isBefore(today) || dto.getEndDate().isBefore(today)) {
            throw new ApiRequestException("Leave dates must not be in the past");
        }

        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new ApiRequestException("End date cannot be before start date");
        }

        if (!leaveType.getStatus().name().equals("ACTIVE")) {
            throw new ApiRequestException("Leave type is not active");
        }

        if (leaveType.isLeaveTypeRequiresAttachment()
                && (dto.getAttachmentPath() == null || dto.getAttachmentPath().isBlank())) {
            throw new ApiRequestException("Attachment is required for this leave type");
        }

        if (leaveType.isLeaveTypeRequireReason()
                && (dto.getReason() == null || dto.getReason().isBlank())) {
            throw new ApiRequestException("Reason is required for this leave type");
        }
    }
}
