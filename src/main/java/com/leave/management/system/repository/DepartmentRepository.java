package com.leave.management.system.repository;

import com.leave.management.system.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;


public interface DepartmentRepository extends JpaRepository<Department, String> {
    boolean existsByName(String name);
    Page<Department> findAll(Pageable pageable);
}
