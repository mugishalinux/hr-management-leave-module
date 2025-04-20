package com.leave.management.system.service.leaveType;

import com.leave.management.system.dto.leaveType.LeaveTypeDto;
import com.leave.management.system.dto.leaveType.LeaveTypeUpdateDto;
import com.leave.management.system.dto.response.ResponseDto;
import com.leave.management.system.model.LeaveType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface LeaveTypeService {
    ResponseDto createLeaveType(LeaveTypeDto leaveTypeDto);
    Page<LeaveType> getAllLeaveTypes(int page, int sizePage, String sortBy);
    LeaveType getSingleLeaveType(String id);
    ResponseDto updateLeaveType(LeaveTypeUpdateDto leaveTypeUpdateDto, String id);
    ResponseDto deleteLeaveType(String id);
}