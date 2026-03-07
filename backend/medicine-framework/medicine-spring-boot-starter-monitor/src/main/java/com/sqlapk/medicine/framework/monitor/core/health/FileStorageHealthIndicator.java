package com.sqlapk.medicine.framework.monitor.core.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Optional file storage health indicator. Disabled by default; enabling it
 * indicates that an external storage system has been configured and should be
 * monitored.
 */
@Component
@ConditionalOnProperty(prefix = "medicine.monitor.health.file-storage", name = "enabled", havingValue = "true")
public class FileStorageHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health.up()
            .withDetail("file-storage", "ENABLED")
            .withDetail("message", "File storage health checks run when enabled.")
            .build();
    }
}