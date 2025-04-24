package com.leave.management.system.controller;

import com.leave.management.system.dto.leaveApplication.HolidayResponseDto;
import com.leave.management.system.dto.leaveApplication.LeaveApplicationDto;
import com.leave.management.system.dto.leaveApplication.*;
import com.leave.management.system.dto.response.ResponseDto;
import com.leave.management.system.enums.LeaveApplicationStatus;
import com.leave.management.system.model.LeaveApplication;
import com.leave.management.system.service.leaveApplications.LeaveApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/leave-applications")
@RequiredArgsConstructor
public class LeaveApplicationController {

    private final LeaveApplicationService leaveApplicationService;

    @PostMapping("/submit")
    public ResponseEntity<ResponseDto> applyForLeave(@Valid @RequestBody LeaveApplicationDto dto) {
        return ResponseEntity.ok(leaveApplicationService.applyForLeave(dto));
    }

    @GetMapping("/submitted")
    public ResponseEntity<Page<LeaveApplication>> getAllLeaveApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        return ResponseEntity.ok(leaveApplicationService.getAllLeaveApplications(page, size, sortBy));
    }
    @GetMapping("/history")
    public ResponseEntity<Page<LeaveApplication>> getMyLeaveApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        return ResponseEntity.ok(leaveApplicationService.getMyLeaveApplications(page,size,sortBy));
    }


    @GetMapping("/byId/{id}")
    public ResponseEntity<LeaveApplication> getLeaveApplicationById(@PathVariable String id) {
        return ResponseEntity.ok(leaveApplicationService.getLeaveApplicationById(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseDto> updateLeaveApplication(
            @PathVariable String id,
            @Valid @RequestBody LeaveApplicationDto dto) {
        return ResponseEntity.ok(leaveApplicationService.updateLeaveApplication(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseDto> deleteLeaveApplication(@PathVariable String id) {
        return ResponseEntity.ok(leaveApplicationService.deleteLeaveApplication(id));
    }
    @GetMapping("/upcoming-holidays")
    public ResponseEntity<List<HolidayResponseDto>> getUpcomingHolidays() {
        return ResponseEntity.ok(leaveApplicationService.getUpcomingHolidays());
    }

    @GetMapping("/team-on-leave")
    public ResponseEntity<TeamOnLeaveDto> getTeamMembersOnLeaveToday() {
        return ResponseEntity.ok(leaveApplicationService.getTodayTeamMembersOnLeave());
    }

    @PutMapping("/approve-or-reject/{id}")
    public ResponseEntity<ResponseDto> approveOrRejectLeave(
            @PathVariable String id,
            @RequestParam LeaveApplicationStatus status,
            @RequestParam(required = false) String comment) {
        return ResponseEntity.ok(leaveApplicationService.approveOrRejectLeave(id, status, comment));
    }
    @GetMapping("/approve/pending-applications")
    public ResponseEntity<Page<LeaveApplication>> getPendingApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        Sort.Order order = new Sort.Order(Sort.Direction.fromString(sort[1]), sort[0]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(order));
        return ResponseEntity.ok(leaveApplicationService.getPendingApplicationsForApprover(pageable));
    }
    @GetMapping("/team-calendar")
    public ResponseEntity<List<TeamLeaveCalendarDto>> getTeamCalendar(
            @RequestParam(required = false) String teamId,
            @RequestParam(required = false) String departmentId) {
        System.out.println("teamId = " + teamId);
        System.out.println("departmentId = " + departmentId);
        List<TeamLeaveCalendarDto> calendarEntries =
                leaveApplicationService.getTeamCalendar(teamId, departmentId);
        return ResponseEntity.ok(calendarEntries);
    }

}
