package com.leave.management.system.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final AuthManager authManager;
    private final SecurityCtxRepository securityCtxRepository;

    // Allow Swagger, OpenAPI, and general public access for doc routes
    private final String[] whiteList = {
            "/swagger-resources/**",
            "/swagger-ui.html",
            "/v2/api-docs",
            "/v3/api-docs",
            "/swagger-ui/**",
            "/webjars/**"
    };

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.addAllowedOriginPattern("*"); // ✅ allow all origins
        cors.addAllowedHeader("*");        // ✅ allow all headers
        cors.addAllowedMethod("*");        // ✅ allow all HTTP methods
        cors.setAllowCredentials(false);   // ✅ false because wildcard origin is used

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // ✅ wire in CORS bean
                .csrf(csrf -> csrf.disable()) // ✅ disable CSRF for simplicity
                .httpBasic(HttpBasicConfigurer::disable)
                .formLogin(FormLoginConfigurer::disable)
                .authenticationManager(authManager)
                .securityContext(context -> context.securityContextRepository(securityCtxRepository))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(whiteList).permitAll()
                        .requestMatchers("/api/users/test").hasAuthority("ADMIN")
                        .requestMatchers("/api/departments").hasAuthority("ADMIN")
                        .requestMatchers("/api/teams").hasAuthority("MANAGER")
                        .requestMatchers("/api/leave-type").hasAuthority("ADMIN")
                        .requestMatchers("/api/leave-policy").hasAuthority("ADMIN")
                        .anyRequest().permitAll() // ✅ allow any other request for now (dev mode)
                );

        return http.build();
    }
}
