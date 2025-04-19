package com.leave.management.system.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.stream.Collectors;

public class JwtAuthToken extends AbstractAuthenticationToken {
    private AuthenticatedUser principal;
    private String jwt;
    private String address;

    /**
     * Creates a token with the supplied array of authorities.
     *
     * @param authorities the collection of <tt>GrantedAuthority</tt>s for the principal
     *                    represented by this authentication object.
     */
    public JwtAuthToken(String token, String address, AuthenticatedUser principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.jwt = token;
        this.address = address;
        this.principal = principal;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return principal.getAuthorityList().stream().map(item -> (GrantedAuthority)item).collect(Collectors.toList());
    }

    public static JwtAuthToken getDefault() {
        JwtAuthToken item = new JwtAuthToken(null, null, null, null);
        item.setAuthenticated(false);
        return item;
    }


    @Override
    public Object getCredentials() {
        return jwt;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    public String getAddress() {
        return address;
    }
}
