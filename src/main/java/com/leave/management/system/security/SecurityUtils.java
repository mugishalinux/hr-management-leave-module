package com.leave.management.system.security;

import com.leave.management.system.exceptions.ApiRequestException;
import com.leave.management.system.model.User;
import com.leave.management.system.security.AuthenticatedUser;
import com.leave.management.system.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    private final UserRepository userRepository;

    public SecurityUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof AuthenticatedUser authUser) {
            return userRepository.findById(authUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        throw new ApiRequestException("User not authenticated");
    }
}