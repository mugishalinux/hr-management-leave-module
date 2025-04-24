package com.leave.management.system.repository;

import com.leave.management.system.enums.UserPermission;
import com.leave.management.system.model.Department;
import com.leave.management.system.model.Team;
import com.leave.management.system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.domain.*;


@Repository
public interface UserRepository extends JpaRepository<User, String> {
    User findByEmail(String email); // ✅ already declared
    boolean existsByEmail(String email);
    List<User> findAllByPermissions(UserPermission permission);
    List<User> findAllByDepartment(Department department);
    Page<User> findAllByTeamId(String teamId, Pageable pageable);
}
