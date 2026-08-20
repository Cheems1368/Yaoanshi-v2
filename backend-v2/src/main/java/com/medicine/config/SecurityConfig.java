package com.medicine.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.common.model.CommonResult;
import com.medicine.service.auth.JwtAuthenticationFilter;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;
    private final Environment environment;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            ObjectMapper objectMapper,
            Environment environment
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
        this.environment = environment;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, ex) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setCharacterEncoding("UTF-8");
                            response.setContentType("application/json");
                            CommonResult<Void> body = CommonResult.fail(HttpStatus.UNAUTHORIZED.value(), "UNAUTHORIZED");
                            response.getWriter().write(objectMapper.writeValueAsString(body));
                        })
                        .accessDeniedHandler((request, response, ex) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setCharacterEncoding("UTF-8");
                            response.setContentType("application/json");
                            CommonResult<Void> body = CommonResult.fail(HttpStatus.FORBIDDEN.value(), "FORBIDDEN");
                            response.getWriter().write(objectMapper.writeValueAsString(body));
                        })
                )
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(
                            "/health",
                            "/app-api/auth/send-otp",
                            "/app-api/auth/login",
                            "/app-api/auth/refresh-token",
                            "/app-api/auth/session/login",
                            "/app-api/auth/session/refresh",
                            "/app-api/auth/session/logout",
                            "/admin-api/system/auth/login",
                            "/admin-api/system/auth/refresh-token",
                            "/admin-api/system/auth/session/login",
                            "/admin-api/system/auth/session/refresh",
                            "/admin-api/system/auth/session/logout"
                    ).permitAll();
                    if (environment.acceptsProfiles(Profiles.of("local"))) {
                        auth.requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll();
                    }
                    auth.requestMatchers("/app-api/**").authenticated();
                    auth.requestMatchers("/admin-api/**").authenticated();
                    auth.anyRequest().permitAll();
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
