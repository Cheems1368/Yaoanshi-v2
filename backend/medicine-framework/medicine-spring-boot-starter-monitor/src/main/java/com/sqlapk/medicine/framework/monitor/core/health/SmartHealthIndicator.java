package com.sqlapk.medicine.framework.monitor.core.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Simplified smart health indicator. Only contributes to the health endpoint
 * when explicitly enabled through the `medicine.monitor.health.smart.enabled`
 * property.
 */
@Component
@ConditionalOnProperty(prefix = "medicine.monitor.health.smart", name = "enabled", havingValue = "true")
public class SmartHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health.up()
            .withDetail("smart-health", "ENABLED")
            .withDetail("message", "Smart health probes are active by configuration.")
            .build();
    }
}