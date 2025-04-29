package com.leave.management.system.repository;


import com.leave.management.system.model.Team;
import com.leave.management.system.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, String> {
    Page<Team> findAll(Pageable pageable);
    boolean existsByLeadId(String leadId);
    boolean existsByName(String name);
    Optional<Team> findByLeadId(String userId);
    List<Team> findByDepartmentId(String departmentId);
}
