package com.leave.management.system.controller.department;


import com.leave.management.system.dto.department.CreateDepartmentDto;
import com.leave.management.system.dto.response.ResponseDto;
import com.leave.management.system.model.Department;
import com.leave.management.system.service.department.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    public ResponseDto create(@Valid @RequestBody CreateDepartmentDto createDepartmentDto) {
        return departmentService.createDepartment(createDepartmentDto);
    }

    @GetMapping
    public ResponseEntity<Page<Department>> getAllDepartments(
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(departmentService.getAllDepartments(pageable));
    }
    @GetMapping("/{id}")
    public Department getById(@PathVariable String id) {
        return departmentService.getDepartmentById(id);
    }

    @PutMapping("/{id}")
    public ResponseDto update(@Valid @PathVariable String id, @RequestBody CreateDepartmentDto createDepartmentDto) {
        return departmentService.updateDepartment(id, createDepartmentDto);
    }

    @DeleteMapping("/{id}")
    public ResponseDto delete(@PathVariable String id) {
        return departmentService.deleteDepartment(id);
    }
}

