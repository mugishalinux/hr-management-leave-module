package com.leave.management.system.service.leavePolicy;

import com.leave.management.system.dto.leavePolicy.LeavePolicyDto;
import com.leave.management.system.dto.response.ResponseDto;

public interface LeavePolicyService {
    ResponseDto createPolicy(LeavePolicyDto dto);
    LeavePolicyDto getPolicy();
    ResponseDto updatePolicy(LeavePolicyDto dto);
}

