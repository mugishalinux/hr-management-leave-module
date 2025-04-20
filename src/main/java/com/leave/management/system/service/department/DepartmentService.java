package com.leave.management.system.service.department;

import com.leave.management.system.dto.department.CreateDepartmentDto;
import com.leave.management.system.dto.response.ResponseDto;
import com.leave.management.system.model.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface DepartmentService {
    ResponseDto createDepartment(CreateDepartmentDto createDepartmentDto);
    Page<Department> getAllDepartments(int page, int sizePage, String sortBy);
    Department getDepartmentById(String id);
    ResponseDto updateDepartment(String id, CreateDepartmentDto createDepartmentDto);
    ResponseDto deleteDepartment(String id);
}