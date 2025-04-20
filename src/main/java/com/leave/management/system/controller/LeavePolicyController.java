package com.leave.management.system.controller;

import com.leave.management.system.dto.leavePolicy.LeavePolicyDto;
import com.leave.management.system.dto.response.ResponseDto;
import com.leave.management.system.service.leavePolicy.LeavePolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leave-policy")
@RequiredArgsConstructor
public class LeavePolicyController {

    private final LeavePolicyService leavePolicyService;

    @PostMapping
    public ResponseEntity<ResponseDto> create(@RequestBody @Valid LeavePolicyDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leavePolicyService.createPolicy(dto));
    }

    @GetMapping
    public ResponseEntity<LeavePolicyDto> get() {
        return ResponseEntity.ok(leavePolicyService.getPolicy());
    }

    @PutMapping
    public ResponseEntity<ResponseDto> update(@RequestBody @Valid LeavePolicyDto dto) {
        return ResponseEntity.ok(leavePolicyService.updatePolicy(dto));
    }
}

