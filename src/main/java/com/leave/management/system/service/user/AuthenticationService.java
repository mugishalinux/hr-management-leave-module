//package com.leave.management.system.service;
//
//
//import com.leave.management.system.exceptions.AuthException;
//import com.leave.management.system.model.SimpleUserAuth;
//import com.leave.management.system.model.TokenResponse;
//import com.leave.management.system.model.User;
//import com.leave.management.system.repository.UserRepository;
//import com.leave.management.system.security.JwtService;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.util.Optional;
//
//
//@Service
//@RequiredArgsConstructor
//public class AuthenticationService {
//    public final static String CERT_WEBSITE_EMAIL = "CERT-WEB";
//    private final static int MAX_RISK = 3;
//    private final JwtService jwtService;
//    private final UserRepository userCoreRepo;
//    private final PasswordEncoder encoder;
//
//    public Boolean logOutUser(String userId) {
//        JwtService.JWTData data = jwtService.removeKey(userId);
//        return data != null;
//    }
//
//    public TokenResponse authenticateUser(SimpleUserAuth userAuth, HttpServletRequest request) {
//        Optional<User> userCore = userCoreRepo.findByUsername(userAuth.getUsername());
//        if (userCore == null)
//            throw new AuthException("User not found");
//
//        if (!userCore.isAccountEnabled() || userCore.isAccountLocked() || userCore.isAccountExpired()) {
//            throw new AuthException("The account is disabled");
//        }
//        if (userAuth.getPassword() == null || !encoder.matches(userAuth.getPassword(), userCore.getPassword())) {
//            userCore.setRisk(userCore.getRisk() + 1);
//            if (userCore.getRisk() + 1 > MAX_RISK)
//                userCore.setAccountEnabled(false);
//            userCoreRepo.save(userCore);
//            throw new AuthException("Invalid credentials");
//        }
//        userCore.setRisk(0);
//        userCoreRepo.save(userCore);
//
//        jwtService.clearExpiredKeys();
//        String token = jwtService.generateToken(userCore, request);
//        return new TokenResponse(token,"success");
//    }
//}
