package com.leave.management.system.repository;

import com.leave.management.system.model.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, String> {
    boolean existsByName(String name);
}