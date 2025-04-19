package com.leave.management.system.service;

import com.leave.management.system.dto.LoginResponseDto;
import com.leave.management.system.dto.RegisterUserDto;
import com.leave.management.system.exceptions.ApiRequestException;
import com.leave.management.system.model.User;
import com.leave.management.system.repository.UserRepository;
import com.leave.management.system.security.JwtService;
import io.swagger.annotations.Api;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
@Autowired
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public User registerUser(RegisterUserDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPermissions(dto.getPermissions());
        user.setAccountEnabled(true);

        return userRepository.save(user);
    }
    public User updateUser(String id, RegisterUserDto dto) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = optionalUser.get();

        if (dto.getUsername() != null) {
            user.setUsername(dto.getUsername());
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (dto.getPermissions() != null) {
            user.setPermissions(dto.getPermissions());
        }

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    public String test() {
        return "hello it works";
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }



    public LoginResponseDto loginWithDetails(String username, String password, HttpServletRequest request) {
        User user = userRepository.findByUsername(username);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new ApiRequestException("Invalid username or password");
        }

        String token = jwtService.generateToken(user, request);

        return new LoginResponseDto(user.getId(), token, user.getPermissions());
    }


    public void logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            String userId = jwtService.validate(token, request.getRemoteAddr()).getSubject();
            jwtService.removeKey(userId);
        }
    }


}
