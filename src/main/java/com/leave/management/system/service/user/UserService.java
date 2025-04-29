package com.leave.management.system.service.user;
import org.json.JSONObject;
import com.leave.management.system.dto.user.*;
import com.leave.management.system.dto.response.ResponseDto;
import com.leave.management.system.enums.UserPermission;
import com.leave.management.system.exceptions.ApiRequestException;
import com.leave.management.system.model.Department;
import com.leave.management.system.model.LeaveBalance;
import com.leave.management.system.model.User;
import com.leave.management.system.repository.DepartmentRepository;
import com.leave.management.system.repository.TeamRepository;
import com.leave.management.system.repository.UserRepository;
import com.leave.management.system.security.JwtService;
import com.leave.management.system.security.SecurityUtils;
import com.leave.management.system.service.leaveBalance.LeaveBalanceService;
import com.leave.management.system.util.helpers.PermissionUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
@Autowired
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SecurityUtils securityUtils;
    private final LeaveBalanceService leaveBalanceService;
    @Value("${microsoft.graph.api.url}")
    private String microsoftGraphApiUrl;

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

            user.setPassword(passwordEncoder.encode("admin"));
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
    public List<User> getAllUsers(){
        try{
            User user = securityUtils.getCurrentUser();
            Department department = new Department();
            department = user.getDepartment();
            return userRepository.findAllByDepartment(department);
        }catch(Exception e){
            throw new ApiRequestException(e.getMessage());
        }
    }
    public Page<UserDTO> fetchingAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);

        List<UserDTO> userDtos = users.stream().map(user -> {
            UserDTO dto = new UserDTO();
            dto.setId(user.getId());
            dto.setFullName(user.getFullName());
            dto.setEmail(user.getEmail());
            dto.setProfile(user.getProfile());
            dto.setPermissions(user.getPermissions().name());
            dto.setAccountEnabled(user.isAccountEnabled());

            if (user.getTeam() != null) {
                dto.setTeamId(user.getTeam().getId().toString());
                dto.setTeamName(user.getTeam().getName());
            }
            if (user.getDepartment() != null) {
                dto.setDepartmentId(user.getDepartment().getId().toString());
                dto.setDepartmentName(user.getDepartment().getName());
            }
            return dto;
        }).collect(Collectors.toList());

        return new PageImpl<>(userDtos, pageable, users.getTotalElements());
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
                user.setPermissions(permission);
            }
            if (dto.getDepartmentId() != null) {
                Department department = departmentRepository.findById(dto.getDepartmentId())
                        .orElseThrow(() -> new ApiRequestException("Department ID does not exist."));
                user.setDepartment(department);
            }

            return userRepository.save(user);
        }catch (Exception e) {
            throw new ApiRequestException(e.getMessage());
        }
    }

    public Optional<User> getUserById() {
        return Optional.ofNullable(securityUtils.getCurrentUser());
    }


    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }



    public LoginResponseDto loginWithDetails(String username, String password, HttpServletRequest request) {
        User user = userRepository.findByEmail(username);

        if (username.toLowerCase().endsWith("@ist.com")) {
            throw new ApiRequestException("Login with @ist.com domain is not allowed.");
        }
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new ApiRequestException("Invalid username or password");
        }else if(!user.isAccountEnabled()) {
            throw new ApiRequestException("Your account is disabled, contact your administrator to enable it.");
        }else if(user.isAccountLocked()) {
            throw new ApiRequestException("Your account is locked, contact your administrator to unlock it.");
        }

        String token = jwtService.generateToken(user, request);
        String dep = "-";
        if(user.getPermissions().name().equals("ADMIN")) {
            return new LoginResponseDto(user.getId(), token, user.getPermissions().name(),dep);
        }else{
            return new LoginResponseDto(user.getId(), token, user.getPermissions().name(), user.getDepartment().getId());
        }

    }


    public Page<User> getAllUsersByTeamId(String teamId, int page, int size, String sortBy) {
        try{
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
            return userRepository.findAllByTeamId(teamId, pageable);
        } catch (RuntimeException e) {
            throw new ApiRequestException(e.getMessage());
        }
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
//    public User findUserBy(){
//        try{
//            return securityUtils.getCurrentUser();
//        } catch (Exception e) {
//            throw new ApiRequestException(e.getMessage());
//        }
//    }

public LoginResponseDto validateToken(String accessToken,HttpServletRequest request) {
    try {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        ResponseEntity<String> response = restTemplate.exchange(
                microsoftGraphApiUrl + "/me",
                HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(headers),
                String.class
        );
        System.out.println(response.getBody());
        if(!response.getStatusCode().is2xxSuccessful()) throw new ApiRequestException("User authentication failed or token expired");
        JSONObject responseBody = new JSONObject(response.getBody());
        String email = responseBody.getString("userPrincipalName");
        User user = userRepository.findByEmail(email);
        if(user==null) throw new ApiRequestException("You are not allowed to use system please contact system administrator");
        String token = jwtService.generateToken(user, request);
        return new LoginResponseDto(user.getId(), token, user.getPermissions().name(), user.getDepartment().getId());
    } catch (HttpClientErrorException.Unauthorized e) {
        throw new ApiRequestException("Unauthorized user or token is invalid");
    } catch (Exception e) {
        e.printStackTrace();
        throw new ApiRequestException("OTP verification failed");
    }
}

}
