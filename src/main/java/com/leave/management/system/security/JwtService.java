package com.leave.management.system.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.leave.management.system.exceptions.InvalidRequestOrigin;
import com.leave.management.system.exceptions.InvalidSubjectException;
import com.leave.management.system.model.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JwtService {
    public final long LONGEVITY_JWT = 60 * 60 * 1000;
    private final long LONGEVITY_OTP = 45 * 600 * 1000;
    private final int KEY_SIZE = 32;
    private final String ISSUER_NAME = "Otp";
    private final String CLAIM_ROLE = "roles";
    private final String CLAIM_USERNAME = "username";
    private final String letters = "abcdefghijklmpqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final String numbers = "0123456789";
    private final String specials = "!@#&*()-=_+[]<>?:";
    private final List<String> types = Arrays.asList(letters, numbers, specials);

    private final Map<String, JWTData> jwtMapping = new HashMap<>();


    @Builder
    public static class JWTData {
        public String key;
        public String address;
        public String otp;
        public Date expirationDate;
    }

    public int clearExpiredKeys() {
        Date current = new Date();
        int count = 0;
        if (!jwtMapping.keySet().isEmpty()) {
            for (String key : jwtMapping.keySet()) {
                JWTData data = jwtMapping.get(key);
                if (current.after(data.expirationDate)) {
                    jwtMapping.remove(key);
                    count++;
                }
            }
        }
        return count;
    }

    public JWTData removeKey(String userId) {
        return jwtMapping.remove(userId);
    }

    public String generateKey() {
        StringBuilder builder = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < KEY_SIZE; i++) {
            String temp = types.get(rand.nextInt(types.size()));
            builder.append(temp.charAt(rand.nextInt(temp.length())));
        }
        return builder.toString();
    }

    public DecodedJWT validate(String token, String address) {
        if (token == null)
            throw new InvalidSubjectException("Null token");
        DecodedJWT decodedJWT;
        try {
            decodedJWT = JWT.decode(token);
        } catch (Exception e) {
            throw new JWTVerificationException("Invalid JWT");
        }
        String subject = decodedJWT.getSubject();
        JWTData data = jwtMapping.get(subject);
        if (subject == null || data == null)
            throw new InvalidSubjectException("Invalid subject " + subject);

        if (!data.address.equals(address))
            throw new InvalidRequestOrigin("Invalid Request origin");

        return JWT.require(Algorithm.HMAC256(data.key.getBytes(StandardCharsets.UTF_8)))
                .build()
                .verify(decodedJWT);
    }

    public String getUsername(DecodedJWT decodedJWT) {
        return decodedJWT.getClaim(CLAIM_USERNAME).as(String.class);
    }

    public List<SimpleGrantedAuthority> getRoles(DecodedJWT decodedJWT) {
        return decodedJWT.getClaim(CLAIM_ROLE).asList(String.class)
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public String generateToken(User user, HttpServletRequest request) {
        Date expirationDate = new Date(System.currentTimeMillis() + LONGEVITY_OTP);
        return performGenerateToken(user.getId(), user.getUsername(), user.getAuthorityList(), expirationDate, null, request);
    }

    public String performGenerateToken(String userId, String username, List<SimpleGrantedAuthority> authorities, Date expirationDate, String otp, HttpServletRequest request) {
        String inetSocketAddress = request.getRemoteAddr();
        if (inetSocketAddress == null)
            return null;
        String address = inetSocketAddress;
        String key = generateKey();
        Algorithm algorithm = Algorithm.HMAC256(key.getBytes(StandardCharsets.UTF_8));
        String jwt = JWT.create()
                .withSubject(userId)
                .withIssuer(ISSUER_NAME)
                .withIssuedAt(new Date())
                .withExpiresAt(expirationDate)
                .withClaim(CLAIM_ROLE, authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .withClaim(CLAIM_USERNAME, username)
                .sign(algorithm);
        jwtMapping.put(userId, JWTData.builder().otp(otp).address(address).key(key).expirationDate(expirationDate).build());
        return jwt;
    }

}
