package com.sqlapk.medicine.framework.monitor.core.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * 自定义健康检查组件
 * 支持 Spring Boot 2.3+ 的 readiness/liveness 探针
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "medicine.health.custom", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CustomHealthIndicator implements HealthIndicator {

    @Autowired(required = false)
    private DataSource dataSource;

    @Autowired(required = false)
    private RedisHealthIndicator redisHealthIndicator;

    @Override
    public Health health() {
        Health.Builder builder = Health.up();
        boolean overallHealthy = true;

        try {
            // 检查数据库连接
            if (dataSource != null) {
                Health dbHealth = checkDatabaseHealth();
                builder.withDetail("database", dbHealth.getDetails());
                if (dbHealth.getStatus() != Status.UP) {
                    overallHealthy = false;
                }
            }

            // 检查Redis连接
            if (redisHealthIndicator != null) {
                Health redisHealth = redisHealthIndicator.health();
                builder.withDetail("redis", redisHealth.getDetails());
                if (redisHealth.getStatus() != Status.UP) {
                    overallHealthy = false;
                }
            }

            // 添加系统信息
            builder.withDetail("system", Map.of(
                "java.version", System.getProperty("java.version"),
                "os.name", System.getProperty("os.name"),
                "os.version", System.getProperty("os.version"),
                "available.processors", Runtime.getRuntime().availableProcessors(),
                "free.memory", Runtime.getRuntime().freeMemory(),
                "total.memory", Runtime.getRuntime().totalMemory(),
                "max.memory", Runtime.getRuntime().maxMemory()
            ));

        } catch (Exception e) {
            log.error("健康检查过程中发生异常", e);
            builder.down().withDetail("error", e.getMessage());
            overallHealthy = false;
        }

        return overallHealthy ? builder.build() : builder.down().build();
    }

    /**
     * 检查数据库健康状态
     */
    private Health checkDatabaseHealth() {
        try {
            long startTime = System.currentTimeMillis();
            
            try (Connection connection = dataSource.getConnection()) {
                boolean isValid = connection.isValid(5); // 5秒超时
                long responseTime = System.currentTimeMillis() - startTime;
                
                if (isValid) {
                    return Health.up()
                        .withDetail("status", "Connected")
                        .withDetail("responseTime", responseTime + "ms")
                        .withDetail("url", connection.getMetaData().getURL())
                        .withDetail("driver", connection.getMetaData().getDriverName())
                        .withDetail("version", connection.getMetaData().getDatabaseProductVersion())
                        .build();
                } else {
                    return Health.down()
                        .withDetail("status", "Connection Invalid")
                        .withDetail("responseTime", responseTime + "ms")
                        .build();
                }
            }
        } catch (SQLException e) {
            log.error("数据库健康检查失败", e);
            return Health.down()
                .withDetail("status", "Connection Failed")
                .withDetail("error", e.getMessage())
                .withDetail("sqlState", e.getSQLState())
                .withDetail("errorCode", e.getErrorCode())
                .build();
        }
    }
}
