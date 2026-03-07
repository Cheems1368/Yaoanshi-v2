package com.sqlapk.medicine.framework.monitor.core.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Optional business health indicator. Disabled by default to avoid synthetic
 * probe failures in environments where external dependencies are not
 * available.
 */
@Component
@ConditionalOnProperty(prefix = "medicine.monitor.health.business", name = "enabled", havingValue = "true")
public class BusinessHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health.up()
            .withDetail("business-health", "ENABLED")
            .withDetail("message", "Business probes run only when explicitly enabled.")
            .build();
    }
}