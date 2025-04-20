package com.leave.management.system.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class AuthManager implements AuthenticationManager {
    private final Logger logger = LoggerFactory.getLogger(AuthManager.class);
    private final JwtService jwtService;

    public AuthManager(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public static class SecAuthException extends AuthenticationException {
        public SecAuthException(String msg) {
            super(msg);
        }

        public SecAuthException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        JwtAuthToken auth = (JwtAuthToken) authentication;
        DecodedJWT decodedJWT;

        try {
            decodedJWT = jwtService.validate((String) auth.getCredentials(), auth.getAddress());
        } catch (Exception e) {
            logger.error("JWT exception message: " + e.getMessage());
            authentication.setAuthenticated(false);
            return authentication;
        }
        authentication.setAuthenticated(true);
        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
        principal.setId(decodedJWT.getSubject());
        principal.setEmail(jwtService.getUsername(decodedJWT));
        principal.setAuthorityList(jwtService.getRoles(decodedJWT));
        return authentication;
    }
}
