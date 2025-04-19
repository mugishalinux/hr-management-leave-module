package com.leave.management.system.repository;

import com.leave.management.system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    User findByUsername(String username); // ✅ already declared
    boolean existsByUsername(String username);
}
