package com.sqlapk.medicine.framework.monitor.core.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Optional help-order probe indicator. The original synthetic probes caused
 * intermittent DOWN states, so the indicator is now a simple opt-in check.
 */
@Component
@ConditionalOnProperty(prefix = "medicine.monitor.health.help-order-probe", name = "enabled", havingValue = "true")
public class HelpOrderProbeHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health.up()
            .withDetail("help-order-probe", "ENABLED")
            .withDetail("message", "Help-order probes execute when explicitly enabled.")
            .build();
    }
}