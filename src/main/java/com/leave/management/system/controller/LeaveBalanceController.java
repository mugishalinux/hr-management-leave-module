package com.leave.management.system.controller;


import com.leave.management.system.dto.leaveBalance.UpdateLeaveBalanceDto;
import com.leave.management.system.model.LeaveBalance;
import com.leave.management.system.service.leaveBalance.LeaveBalanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leave-balances")
@RequiredArgsConstructor
public class LeaveBalanceController {

    private final LeaveBalanceService leaveBalanceService;

    @PutMapping("/update")
    public ResponseEntity<LeaveBalance> updateLeaveBalance(@Valid @RequestBody UpdateLeaveBalanceDto dto) {
        return ResponseEntity.ok(leaveBalanceService.updateLeaveBalance(dto));
    }
    @GetMapping("")
    public ResponseEntity<LeaveBalance> getMyLeaveBalance() {
        return ResponseEntity.ok(leaveBalanceService.getMyLeaveBalance());
    }
}
