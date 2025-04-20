package com.leave.management.system.repository;

import com.leave.management.system.model.LeavePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeavePolicyRepository extends JpaRepository<LeavePolicy, String> {
}
