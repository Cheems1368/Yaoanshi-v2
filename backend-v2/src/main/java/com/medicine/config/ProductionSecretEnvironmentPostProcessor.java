package com.medicine.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Profiles;
import org.springframework.util.StringUtils;

import java.util.Set;

public class ProductionSecretEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final Set<String> FORBIDDEN_VALUES = Set.of(
            "replace-me",
            "replace-with-secure-secret",
            "replace-access-key",
            "replace-secret-key",
            "local-dev-jwt-secret-change-me"
    );

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!environment.acceptsProfiles(Profiles.of("prod"))) {
            return;
        }

        assertConfigured(environment, "medicine.jwt.secret");
        assertConfigured(environment, "medicine.datasource.password");
        assertConfigured(environment, "medicine.storage.access-key");
        assertConfigured(environment, "medicine.storage.secret-key");

        String legacyUrl = environment.getProperty("medicine.legacy-auth-datasource.url");
        if (StringUtils.hasText(legacyUrl)) {
            assertConfigured(environment, "medicine.legacy-auth-datasource.password");
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private void assertConfigured(ConfigurableEnvironment environment, String propertyName) {
        String value = environment.getProperty(propertyName);
        if (!StringUtils.hasText(value) || FORBIDDEN_VALUES.contains(value.trim())) {
            throw new IllegalStateException("Production startup blocked: " + propertyName + " is blank or placeholder");
        }
    }
}
