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
                        .requestMatchers("/api/users/enable-account").hasAuthority("ADMIN")
                        .requestMatchers("/api/users/lock-account").hasAuthority("ADMIN")
                        .requestMatchers("/api/users/list").authenticated()
                        .requestMatchers("/api/users/team").authenticated()
                        .requestMatchers("/api/users/single").authenticated()
                        .requestMatchers("/api/update-account").hasAuthority("MANAGER")
                        .requestMatchers("/api/update-account").hasAuthority("STAFF")
                        .requestMatchers("/api/update-account").hasAuthority("ADMIN")
                        .requestMatchers("/api/leave-balances/update").hasAuthority("ADMIN")
                        .requestMatchers("/api/leave-applications/submit").authenticated()
                        .requestMatchers("/api/leave-applications/submitted").hasAuthority("ADMIN")
                        .requestMatchers("/api/leave-applications/history").authenticated()
                        .requestMatchers("/api/leave-applications/byId").authenticated()
                        .requestMatchers("/api/leave-applications/update").authenticated()
                        .requestMatchers("/api/leave-applications/delete").authenticated()
                        .requestMatchers("/api/leave-applications/upcoming-holidays").authenticated()
                        .requestMatchers("/api/leave-applications/team-members-on-leave").authenticated()
                        .requestMatchers("/api/leave-applications/team-on-leave").authenticated()
                        .requestMatchers("/api/leave-applications/approve-or-reject").authenticated()
                        .requestMatchers("/api/leave-balance-overview").authenticated()
                        .requestMatchers("/api/leave-balance-overview/details").authenticated()
                        .requestMatchers("/api/departments/list").permitAll()
                        .requestMatchers("/api/departments/create").hasAuthority("ADMIN")
                        .requestMatchers("/api/departments/update").hasAuthority("ADMIN")
                        .requestMatchers("/api/departments/delete").hasAuthority("ADMIN")
                        .requestMatchers("/api/teams").hasAuthority("MANAGER")
                        .requestMatchers("/api/leave-type").hasAuthority("ADMIN")
                        .requestMatchers("/api/leave-type/list").authenticated()
                        .requestMatchers("/api/leave-policy").hasAuthority("ADMIN")
                        .requestMatchers("/approve-pending-applications").authenticated()
                        .anyRequest().permitAll() // ✅ allow any other request for now (dev mode)
                );

        return http.build();
    }
}
