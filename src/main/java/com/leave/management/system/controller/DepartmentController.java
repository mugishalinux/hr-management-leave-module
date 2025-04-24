package com.leave.management.system.controller;


import com.leave.management.system.dto.department.CreateDepartmentDto;
import com.leave.management.system.dto.response.ResponseDto;
import com.leave.management.system.model.Department;
import com.leave.management.system.service.department.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;
    @PostMapping("/create")
    public ResponseDto create(@Valid @RequestBody CreateDepartmentDto createDepartmentDto) {
        return departmentService.createDepartment(createDepartmentDto);
    }

    @GetMapping("/list")
    public ResponseEntity<Page<Department>> getAllDepartments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int sizePage,
            @RequestParam(defaultValue = "name") String sortBy) {
        return ResponseEntity.ok(departmentService.getAllDepartments(page, sizePage, sortBy));
    }
    @GetMapping("/{id}")
    public Department getById(@PathVariable String id) {
        return departmentService.getDepartmentById(id);
    }

    @PutMapping("/update/{id}")
    public ResponseDto update(@Valid @PathVariable String id, @RequestBody CreateDepartmentDto createDepartmentDto) {
        return departmentService.updateDepartment(id, createDepartmentDto);
    }

    @DeleteMapping("delete/{id}")
    public ResponseDto delete(@PathVariable String id) {
        return departmentService.deleteDepartment(id);
    }
}

