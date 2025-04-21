package com.leave.management.system.service.leaveApplications;

import com.leave.management.system.dto.leaveApplication.LeaveApplicationDto;
import com.leave.management.system.dto.response.ResponseDto;
import com.leave.management.system.enums.LeaveApplicationStatus;
import com.leave.management.system.model.LeaveApplication;
import org.springframework.data.domain.Page;
import java.time.LocalDate;
import java.util.List;


public interface LeaveApplicationService {

    ResponseDto applyForLeave(LeaveApplicationDto dto);

    Page<LeaveApplication> getAllLeaveApplications(int page, int size, String sortBy);

    LeaveApplication getLeaveApplicationById(String id);

    ResponseDto updateLeaveApplication(String id, LeaveApplicationDto dto);

    ResponseDto deleteLeaveApplication(String id);

    Page<LeaveApplication> getMyLeaveApplications(int page, int size, String sortBy);

    List<LocalDate> getUpcomingHolidays();

    List<LeaveApplication> getCurrentTeamLeaves();

    ResponseDto approveOrRejectLeave(String applicationId, LeaveApplicationStatus status, String comment);

}
