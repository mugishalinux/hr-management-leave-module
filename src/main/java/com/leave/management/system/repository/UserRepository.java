package com.leave.management.system.repository;

import com.leave.management.system.enums.UserPermission;
import com.leave.management.system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    User findByEmail(String email); // ✅ already declared
    boolean existsByEmail(String email);
    List<User> findAllByPermissions(UserPermission permission);
}
