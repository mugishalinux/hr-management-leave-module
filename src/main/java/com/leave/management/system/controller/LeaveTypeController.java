package com.leave.management.system.controller;

import com.leave.management.system.dto.leaveType.LeaveTypeDto;
import com.leave.management.system.dto.leaveType.LeaveTypeUpdateDto;
import com.leave.management.system.dto.response.ResponseDto;
import com.leave.management.system.model.LeaveType;
import com.leave.management.system.service.leaveType.LeaveTypeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("api/leave-type")
public class LeaveTypeController {

    private final LeaveTypeService leaveTypeService;

    public LeaveTypeController(LeaveTypeService leaveTypeService) {
        this.leaveTypeService = leaveTypeService;
    }

    @PostMapping
    public ResponseEntity<ResponseDto> createLeaveType(@Valid @RequestBody LeaveTypeDto leaveTypeDto) {
        return new ResponseEntity<>(leaveTypeService.createLeaveType(leaveTypeDto), HttpStatus.CREATED);
    }

    @GetMapping("")
    public Page<LeaveType> getAllLeaveTypes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int sizePage,
            @RequestParam(defaultValue = "name") String sortBy) {
        return leaveTypeService.getAllLeaveTypes(page, sizePage, sortBy);
    }
    @GetMapping("/{id}")
    public ResponseEntity<LeaveType> getLeaveTypeById(@PathVariable String id) {
        return ResponseEntity.ok( leaveTypeService.getSingleLeaveType(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto> updateLeaveType(@PathVariable String id, @Valid @RequestBody LeaveTypeUpdateDto leaveTypeDto) {
        return ResponseEntity.ok(leaveTypeService.updateLeaveType(leaveTypeDto,id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto> deleteLeaveType(@PathVariable String id) {
        return ResponseEntity.ok(leaveTypeService.deleteLeaveType(id));
    }
}