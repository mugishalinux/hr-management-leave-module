package com.leave.management.system.service;

import com.leave.management.system.dto.user.*;
import com.leave.management.system.dto.response.ResponseDto;
import com.leave.management.system.enums.UserPermission;
import com.leave.management.system.exceptions.ApiRequestException;
import com.leave.management.system.model.Department;
import com.leave.management.system.model.LeaveBalance;
import com.leave.management.system.model.Team;
import com.leave.management.system.model.User;
import com.leave.management.system.repository.DepartmentRepository;
import com.leave.management.system.repository.TeamRepository;
import com.leave.management.system.repository.UserRepository;
import com.leave.management.system.security.JwtService;
import com.leave.management.system.service.leaveBalance.LeaveBalanceService;
import com.leave.management.system.util.helpers.PermissionUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
@Autowired
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LeaveBalanceService leaveBalanceService;


    public User registerUser(RegisterUserDto dto) {
        try{
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new ApiRequestException("Email already taken");
            }
            User user = new User();
            user.setEmail(dto.getEmail());
            user.setProfile(dto.getProfileImg());
            user.setFullName(dto.getFullName());
            if (dto.getDepartmentId() != null) {
                Department department = departmentRepository.findById(dto.getDepartmentId())
                        .orElseThrow(() -> new ApiRequestException("Department ID does not exist."));
                user.setDepartment(department);
            }
            if(!dto.getTeamId().isBlank() || !dto.getTeamId().isEmpty()) {
                Team team = teamRepository.findById(dto.getTeamId()).orElseThrow(() -> new ApiRequestException("Team ID does not exist."));
                user.setTeam(team);
            }
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            UserPermission permission = PermissionUtils.validateAndParsePermission(dto.getPermissions()); // returns UserPermission.ADMIN
            user.setPermissions(permission);
            user.setAccountEnabled(false);
            User userSaved = userRepository.save(user);
            LeaveBalance leaveBalance = new LeaveBalance();
            leaveBalanceService.createBalanceForUser(userSaved.getId());
            return userSaved;
        }catch (Exception e){
            throw new ApiRequestException(e.getMessage());
        }
    }
    public User updateUser(String id, UpdateUserDto dto) {
        try {
            Optional<User> optionalUser = userRepository.findById(id);
            if (optionalUser.isEmpty()) {
                throw new ApiRequestException("User not found");
            }
            User user = optionalUser.get();
            user.setFullName(dto.getFullName());
            user.setProfile(dto.getProfileImg());
            if (dto.getEmail() != null) {
                user.setEmail(dto.getEmail());
            }
            if (dto.getPermissions() != null) {
                UserPermission permission = PermissionUtils.validateAndParsePermission(dto.getPermissions()); //
                if(permission.name().equals("ADMIN")) {
                    throw new ApiRequestException("Admin permission is denied");
                }// returns UserPermission.ADMIN
                user.setPermissions(permission);
            }
            if (dto.getDepartmentId() != null) {
                Department department = departmentRepository.findById(dto.getDepartmentId())
                        .orElseThrow(() -> new ApiRequestException("Department ID does not exist."));
                user.setDepartment(department);
            }
            if(!dto.getTeamId().isBlank() || !dto.getTeamId().isEmpty()) {
                Team team = teamRepository.findById(dto.getTeamId()).orElseThrow(() -> new ApiRequestException("Team ID does not exist."));
                user.setTeam(team);
            }
            return userRepository.save(user);
        }catch (Exception e) {
            throw new ApiRequestException(e.getMessage());
        }
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    public Optional<User> getUserById(String id) {
        return Optional.ofNullable(userRepository.findById(id).orElseThrow(() -> new ApiRequestException("User not found.")));
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }



    public LoginResponseDto loginWithDetails(String username, String password, HttpServletRequest request) {
        User user = userRepository.findByEmail(username);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new ApiRequestException("Invalid username or password");
        }else if(!user.isAccountEnabled()) {
            throw new ApiRequestException("Your account is disabled, contact your administrator to enable it.");
        }else if(user.isAccountLocked()) {
            throw new ApiRequestException("Your account is locked, contact your administrator to unlock it.");
        }

        String token = jwtService.generateToken(user, request);
        return new LoginResponseDto(user.getId(), token, user.getPermissions().name());
    }


    public void logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            String userId = jwtService.validate(token, request.getRemoteAddr()).getSubject();
            jwtService.removeKey(userId);
        }
    }
    public ResponseDto enablingAndDisablingAccount(AccountEnableUpdateDto statusDto) {
        try {
            Optional<User> optionalUser = userRepository.findById(statusDto.getUserId());
            if (optionalUser.isEmpty()) {
                throw new ApiRequestException("User not found");
            }
            User user = optionalUser.get();
            user.setAccountEnabled(statusDto.getStatus());
            userRepository.save(user);
            String message = "";
            if(statusDto.getStatus()) {
                message = "enabled";
            }else{
                message = "disabled";
            }
            return new ResponseDto(HttpStatus.OK, STR."Account successfully \{message}", user.getId());
        }catch (Exception e) {
           throw new ApiRequestException(STR."Update failed\{e.getMessage()}");
        }
    }
    public ResponseDto lockAndUnlockAccount(AccountLockUpdateDto statusDto) {
        try {
            Optional<User> optionalUser = userRepository.findById(statusDto.getUserId());
            if (optionalUser.isEmpty()) {
                throw new ApiRequestException("User not found");
            }
            User user = optionalUser.get();
            user.setAccountLocked(statusDto.getStatus());
            userRepository.save(user);
            String message = "";
            if(statusDto.getStatus()) {
                message = "locked";
            }else{
                message = "unlocked";
            }
            return new ResponseDto(HttpStatus.OK, STR."Account successfully \{message}", user.getId());
        }catch (Exception e) {
            throw new ApiRequestException(STR."Update failed\{e.getMessage()}");
        }
    }
}
