package com.leave.management.system.service;

import com.leave.management.system.model.User;
import com.leave.management.system.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserCoreService {
    private final UserRepository userRepository;

    public User findUserById(String id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent())
            return user.get();
        else
            return null;
    }
}
