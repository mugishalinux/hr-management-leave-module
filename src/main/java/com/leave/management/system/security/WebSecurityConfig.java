package com.leave.management.system.security;

import com.leave.management.system.constants.ServerRoutes;
import com.leave.management.system.model.Permissions;
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
                        .requestMatchers("/api/users/test").authenticated()
                        .requestMatchers(ServerRoutes.POST).hasAuthority(Permissions.POST_MANAGEMENT.name())
                        .requestMatchers(ServerRoutes.POST_GET).hasAuthority(Permissions.POST_MANAGEMENT.name())
                        .requestMatchers(ServerRoutes.POST_DELETE).hasAuthority(Permissions.POST_MANAGEMENT.name())
                        .requestMatchers(ServerRoutes.POST_UPDATE).hasAuthority(Permissions.POST_MANAGEMENT.name())
                        .requestMatchers(ServerRoutes.POST_GET_BY_ID).hasAuthority(Permissions.POST_MANAGEMENT.name())
                        .requestMatchers(ServerRoutes.POST_SEARCH).hasAuthority(Permissions.POST_MANAGEMENT.name())
                        .requestMatchers(ServerRoutes.LIKE).hasAuthority(Permissions.POST_MANAGEMENT.name())
                        .requestMatchers(ServerRoutes.COMMENT).hasAuthority(Permissions.POST_MANAGEMENT.name())
                        .anyRequest().permitAll() // ✅ allow any other request for now (dev mode)
                );

        return http.build();
    }
}
