package com.leave.management.system.service.leavePolicy;

import com.leave.management.system.dto.leavePolicy.LeavePolicyDto;
import com.leave.management.system.dto.response.ResponseDto;
import com.leave.management.system.exceptions.ApiRequestException;
import com.leave.management.system.model.LeavePolicy;
import com.leave.management.system.repository.LeavePolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeavePolicyServiceImpl implements LeavePolicyService {

    private final LeavePolicyRepository policyRepository;

    @Override
    public ResponseDto createPolicy(LeavePolicyDto dto) {
        try {
            if (policyRepository.count() > 0) {
                throw new ApiRequestException("Leave policy already exists. Only one policy is allowed.");
            }

            LeavePolicy policy = LeavePolicy.builder()
                    .accrualRate(dto.getAccrualRate())
                    .maxCarryForwardDays(dto.getMaxCarryForwardDays())
                    .build();

            LeavePolicy saved = policyRepository.save(policy);
            return new ResponseDto(HttpStatus.CREATED, "Leave policy successfully created", saved.getId());
        } catch (Exception e) {
            throw new ApiRequestException(e.getMessage());
        }
    }

    @Override
    public LeavePolicyDto getPolicy() {
        LeavePolicy policy = policyRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new ApiRequestException("Leave policy not found"));
        return new LeavePolicyDto(policy.getAccrualRate(), policy.getMaxCarryForwardDays());
    }

    @Override
    public ResponseDto updatePolicy(LeavePolicyDto dto) {
        try {
            LeavePolicy policy = policyRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new ApiRequestException("Leave policy not found"));

            policy.setAccrualRate(dto.getAccrualRate());
            policy.setMaxCarryForwardDays(dto.getMaxCarryForwardDays());

            LeavePolicy updated = policyRepository.save(policy);
            return new ResponseDto(HttpStatus.OK, "Leave policy successfully updated", policy.getId());
        } catch (Exception e) {
            throw new ApiRequestException(e.getMessage());
        }
    }
}
