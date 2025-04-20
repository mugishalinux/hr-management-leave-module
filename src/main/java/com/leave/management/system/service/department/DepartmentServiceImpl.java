package com.leave.management.system.service.department;

import com.leave.management.system.dto.department.CreateDepartmentDto;
import com.leave.management.system.dto.response.ResponseDto;
import com.leave.management.system.exceptions.ApiRequestException;
import com.leave.management.system.model.Department;
import com.leave.management.system.model.Team;
import com.leave.management.system.model.User;
import com.leave.management.system.repository.DepartmentRepository;
import com.leave.management.system.security.SecurityUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final SecurityUtils securityUtils;

    private final ResponseDto responseDto;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository, SecurityUtils securityUtils, ResponseDto responseDto) {
        this.departmentRepository = departmentRepository;
        this.securityUtils = securityUtils;
        this.responseDto = responseDto;
    }

    @Override
    public ResponseDto createDepartment(CreateDepartmentDto createDepartmentDto) {
        try {
            Department department = new Department();
            if(departmentRepository.existsByName(createDepartmentDto.getDepartmentName())){
                throw new ApiRequestException("Department name already exists");
            }
            department.setName(createDepartmentDto.getDepartmentName());
            department.setDescription(createDepartmentDto.getDepartmentDesc());
            User user = securityUtils.getCurrentUser();
            department.setCreatedBy(user);
            department.setUpdatedBy(user);
            return new ResponseDto(HttpStatus.CREATED,"Department created successfully",departmentRepository.save(department).getId());
        } catch (Exception e) {
            throw new ApiRequestException(e.getMessage());
        }
    }

    @Override
    public Page<Department> getAllDepartments(int page, int sizePage, String sortBy) {
        return departmentRepository.findAll(PageRequest.of(page, sizePage,  Sort.by(Sort.Direction.ASC, sortBy)));
    }

    @Override
    public Department getDepartmentById(String id) {
        return departmentRepository.findById(id).orElseThrow(() -> new ApiRequestException("Department not found"));
    }

    @Override
    public ResponseDto updateDepartment(String id, CreateDepartmentDto createDepartmentDto) {
        try {
            Department department = departmentRepository.findById(id)
                    .orElseThrow(() -> new ApiRequestException("Department not found"));
            if(departmentRepository.existsByName(createDepartmentDto.getDepartmentName())){
                throw new ApiRequestException("Department name already exists");
            }
            department.setId(id);
            department.setName(createDepartmentDto.getDepartmentName());
            department.setDescription(createDepartmentDto.getDepartmentDesc());
            User user = securityUtils.getCurrentUser();
            department.setCreatedBy(user);
            departmentRepository.save(department);
            return new ResponseDto(HttpStatus.OK,"Department updated successfully", department.getId());
        } catch (Exception e) {
            throw new ApiRequestException(e.getMessage());
        }

    }

    @Override
    public ResponseDto deleteDepartment(String id) {
        try {
            Department department = departmentRepository.findById(id)
                    .orElseThrow(() -> new ApiRequestException("Department not found"));
            departmentRepository.delete(department);
            return new ResponseDto(HttpStatus.OK, "Department deleted successfully", department.getId());
        } catch (Exception e) {
            throw new ApiRequestException(e.getMessage());
        }
    }
}
