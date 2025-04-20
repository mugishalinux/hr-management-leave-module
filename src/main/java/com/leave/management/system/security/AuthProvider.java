package com.leave.management.system.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AuthProvider  implements AuthenticationProvider {

    private final JwtService jwtService;

    public AuthProvider(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        JwtAuthToken auth = (JwtAuthToken) authentication;
        DecodedJWT decodedJWT;
        try {
            decodedJWT = jwtService.validate((String) auth.getCredentials(), auth.getAddress());
        } catch (Exception e) {
            System.out.println("JWT exception message: " + e.getMessage());
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

    @Override
    public boolean supports(Class<?> aClass) {
        return false;
    }
}
