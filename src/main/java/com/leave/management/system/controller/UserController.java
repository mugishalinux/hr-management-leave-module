package com.leave.management.system.controller;

import com.leave.management.system.dto.user.*;
import com.leave.management.system.dto.response.ResponseDto;
import com.leave.management.system.model.LeaveApplication;
import com.leave.management.system.model.User;
import com.leave.management.system.repository.UserRepository;
import com.leave.management.system.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

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
    @GetMapping("/list")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    @GetMapping("/list/all")
    public ResponseEntity<Page<User>> listAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        Sort.Direction direction = Sort.Direction.fromString(sort[1]);
        String sortBy = sort[0];
        Pageable pageable = PageRequest.of(page, size, Sort.by(new Sort.Order(direction, sortBy)));

        Page<User> users = userRepository.findAll(pageable);
        return ResponseEntity.ok(users);
    }
    @GetMapping("/single")
    public ResponseEntity<User> getSingleUserByid() {
        return userService.getUserById()
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
    @GetMapping("/team/{teamId}")
    public ResponseEntity<Page<User>> getUsersByTeamId(
            @PathVariable String teamId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fullName") String sortBy
    ) {
        Page<User> users = userService.getAllUsersByTeamId(teamId, page, size, sortBy);
        return ResponseEntity.ok(users);
    }
}
