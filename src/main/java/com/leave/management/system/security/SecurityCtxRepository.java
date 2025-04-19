package com.leave.management.system.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.DeferredSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;


@Component
public class SecurityCtxRepository implements SecurityContextRepository {

    private final AuthManager authManager;
    public SecurityCtxRepository(AuthManager authManager) {this.authManager = authManager;}

    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder exchange) {
        String token = exchange.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        token = token != null ? token : exchange.getRequest().getParameter("auth");
        if (token == null)
            return new SecurityContextImpl();
        if (token.startsWith("Bearer "))
            token = token.substring(7);

        String address = exchange.getRequest().getRemoteAddr();
        if (address == null)
            return new SecurityContextImpl();

        JwtAuthToken auth = new JwtAuthToken(token, address, new AuthenticatedUser(), null);
        Authentication verifiedAuth = authManager.authenticate(auth);
        return verifiedAuth.isAuthenticated()
                ? new SecurityContextImpl(verifiedAuth)
                : new SecurityContextImpl();
    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
    }

    @Override
    public boolean containsContext(HttpServletRequest request) {
        return false;
    }

    @Override
    public DeferredSecurityContext loadDeferredContext(HttpServletRequest request) {
        return new DeferredSecurityContext() {
            @Override
            public SecurityContext get() {
                String token = request.getHeader(HttpHeaders.AUTHORIZATION);
                token = token != null ? token : request.getParameter("auth");
                if (token == null)
                    return new SecurityContextImpl();
                if (token.startsWith("Bearer "))
                    token = token.substring(7);

                String address = request.getRemoteAddr();
                if (address == null)
                    return new SecurityContextImpl();

                JwtAuthToken auth = new JwtAuthToken(token, address, new AuthenticatedUser(), null);
                Authentication verifiedAuth = authManager.authenticate(auth);

                return verifiedAuth.isAuthenticated()
                        ? new SecurityContextImpl(verifiedAuth)
                        : new SecurityContextImpl();
            }

            @Override
            public boolean isGenerated() {
                return true;
            }
        };
    }
}
