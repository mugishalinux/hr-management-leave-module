package com.leave.management.system.controller;

import com.leave.management.system.dto.user.*;
import com.leave.management.system.dto.response.ResponseDto;
import com.leave.management.system.model.User;
import com.leave.management.system.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterUserDto dto) {
        return ResponseEntity.ok(userService.registerUser(dto));
    }

    @PutMapping("/update-account/{id}")
    public ResponseEntity<User> updateUser(@Valid @PathVariable String id, @RequestBody UpdateUserDto dto) {
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }
    @PutMapping("/enable-account")
    public ResponseEntity<ResponseDto> accountEnable(
            @Valid @RequestBody AccountEnableUpdateDto statusDto
    ) {
        return ResponseEntity.ok(userService.enablingAndDisablingAccount(statusDto));
    }
    @PutMapping("/lock-account")
    public ResponseEntity<ResponseDto> accountLockUpdate(
            @Valid @RequestBody AccountLockUpdateDto statusDto
    ) {
        return ResponseEntity.ok(userService.lockAndUnlockAccount(statusDto));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(userService.loginWithDetails(request.getEmail(), request.getPassword(), httpRequest));
    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        userService.logout(request);
        return ResponseEntity.ok("Logged out successfully");
    }

}
