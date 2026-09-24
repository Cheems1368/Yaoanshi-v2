package com.medicine.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    @Test
    void shouldAllowTheSecureCapacitorOriginForCredentialedNativeRequests() {
        SecurityConfig securityConfig = new SecurityConfig(null, new ObjectMapper(), new MockEnvironment());
        CorsConfiguration configuration = securityConfig.corsConfigurationSource()
                .getCorsConfiguration(new MockHttpServletRequest("OPTIONS", "/app-api/auth/send-otp"));

        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowCredentials()).isTrue();
        assertThat(configuration.checkOrigin("https://localhost")).isEqualTo("https://localhost");
        assertThat(configuration.checkOrigin("http://localhost")).isNull();
        assertThat(configuration.checkOrigin("https://evil.example")).isNull();
        assertThat(configuration.getAllowedMethods()).contains("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }

    @Test
    void shouldAllowTheHttpCapacitorOriginOnlyInTheLocalProfile() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("local");
        SecurityConfig securityConfig = new SecurityConfig(null, new ObjectMapper(), environment);
        CorsConfiguration configuration = securityConfig.corsConfigurationSource()
                .getCorsConfiguration(new MockHttpServletRequest("OPTIONS", "/app-api/auth/session/refresh"));

        assertThat(configuration).isNotNull();
        assertThat(configuration.checkOrigin("http://localhost")).isEqualTo("http://localhost");
    }
}
