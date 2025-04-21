package com.leave.management.system.service.leaveType;

import com.leave.management.system.dto.leaveType.LeaveTypeDto;
import com.leave.management.system.dto.leaveType.LeaveTypeUpdateDto;
import com.leave.management.system.dto.response.ResponseDto;
import com.leave.management.system.enums.LeaveTypeStatus;
import com.leave.management.system.exceptions.ApiRequestException;
import com.leave.management.system.model.Department;
import com.leave.management.system.model.LeaveType;
import com.leave.management.system.model.User;
import com.leave.management.system.repository.LeaveTypeRepository;
import com.leave.management.system.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LeaveTypeServiceImpl implements LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;
    private final SecurityUtils securityUtils;

    public LeaveTypeServiceImpl(LeaveTypeRepository leaveTypeRepository, SecurityUtils securityUtils) {
        this.leaveTypeRepository = leaveTypeRepository;
        this.securityUtils = securityUtils;
    }

    @Override
    public ResponseDto createLeaveType(LeaveTypeDto leaveTypeDto) {

        try {
            if (leaveTypeRepository.existsByName(leaveTypeDto.getName())) {
                throw new ApiRequestException("Leave type name already exists");
            }
            LeaveType leaveType = new LeaveType();
            leaveType.setName(leaveTypeDto.getName());
            leaveType.setDescription(leaveTypeDto.getDescription());
            leaveType.setLeaveTypeRequireReason(leaveTypeDto.isLeaveTypeRequireReason());
            leaveType.setLeaveTypeRequiresAttachment(leaveTypeDto.isLeaveTypeRequiresAttachment());
            User user = securityUtils.getCurrentUser();
            leaveType.setCreatedBy(user);
            leaveType.setAffectsBalance(leaveTypeDto.isAffectsBalance());
            leaveType.setUpdatedBy(user);
            return new ResponseDto(HttpStatus.CREATED,"Department created successfully",leaveTypeRepository.save(leaveType).getId());
        } catch (Exception e) {
            throw new ApiRequestException(e.getMessage());
        }
    }

    @Override
    public Page<LeaveType> getAllLeaveTypes(int page, int sizePage, String sortBy) {
        return leaveTypeRepository.findAll(PageRequest.of(page, sizePage,  Sort.by(Sort.Direction.ASC, sortBy)));
    }


    @Override
    public LeaveType getSingleLeaveType(String id) {
        return leaveTypeRepository.findById(id).orElseThrow(() -> new ApiRequestException("Leave type not found"));
    }

    @Override
    public ResponseDto updateLeaveType(LeaveTypeUpdateDto leaveTypeUpdateDto, String id) {
        try {
            LeaveType leaveType = leaveTypeRepository.findById(id)
                    .orElseThrow(() -> new ApiRequestException("Leave type not found"));
            leaveType.setName(leaveTypeUpdateDto.getName());
            leaveType.setLeaveTypeRequireReason(leaveTypeUpdateDto.isLeaveTypeRequireReason());
            leaveType.setDescription(leaveTypeUpdateDto.getDescription());
            leaveType.setLeaveTypeRequiresAttachment(leaveTypeUpdateDto.isLeaveTypeRequiresAttachment());
//            leaveType.setStatus(leaveTypeUpdateDto.getLeaveTypeStatus());
            leaveType.setAffectsBalance(leaveTypeUpdateDto.isAffectsBalance());
            User user = securityUtils.getCurrentUser();
            leaveType.setUpdatedBy(user);
            try {
                LeaveTypeStatus status = LeaveTypeStatus.valueOf(leaveTypeUpdateDto.getLeaveTypeStatus().name());
                leaveType.setStatus(status);
            } catch (IllegalArgumentException e) {
                throw new ApiRequestException("Invalid leave status");
            }
            return new ResponseDto(HttpStatus.OK, "Leave type successfully updated",leaveTypeRepository.save(leaveType).getId());
        } catch (Exception e) {
            throw new ApiRequestException(e.getMessage());
        }
    }

    @Override
    public ResponseDto deleteLeaveType(String id) {
        LeaveType leaveType = leaveTypeRepository.findById(id).orElseThrow(() -> new ApiRequestException("Leave type not found"));
        try {
            return new ResponseDto(HttpStatus.OK, "Leave type deleted successfully", id);
        } catch (Exception e) {
            throw new ApiRequestException(e.getMessage());
        }
    }
}