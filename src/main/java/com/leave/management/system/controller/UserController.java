package com.leave.management.system.controller;

import com.leave.management.system.dto.LoginRequest;
import com.leave.management.system.dto.LoginResponseDto;
import com.leave.management.system.dto.RegisterUserDto;
import com.leave.management.system.model.User;
import com.leave.management.system.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterUserDto dto) {
        return ResponseEntity.ok(userService.registerUser(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody RegisterUserDto dto) {
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(userService.loginWithDetails(request.getUsername(), request.getPassword(), httpRequest));
    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        userService.logout(request);
        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/test")

    public ResponseEntity<String> test() {
        return ResponseEntity.ok(userService.test());
    }
}
