package com.sqlapk.medicine.framework.monitor.core.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Optional prescription probe indicator. Disabled by default so that local and
 * containerised environments are not marked unhealthy by synthetic checks.
 */
@Component
@ConditionalOnProperty(prefix = "medicine.monitor.health.prescription-probe", name = "enabled", havingValue = "true")
public class PrescriptionProbeHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health.up()
            .withDetail("prescription-probe", "ENABLED")
            .withDetail("message", "Prescription probes run only when explicitly enabled.")
            .build();
    }
}