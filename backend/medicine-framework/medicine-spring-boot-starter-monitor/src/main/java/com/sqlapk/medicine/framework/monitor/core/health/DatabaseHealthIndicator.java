package com.sqlapk.medicine.framework.monitor.core.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库健康检查指示器
 * 检查数据库连接状态和基本功能
 */
@Slf4j
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    @Autowired
    private DataSource dataSource;

    @Override
    public Health health() {
        try {
            // 检查数据库连接
            try (Connection connection = dataSource.getConnection()) {
                if (connection.isValid(5)) {
                    // 执行简单查询验证数据库功能
                    try (var statement = connection.createStatement();
                         var resultSet = statement.executeQuery("SELECT 1 as test")) {
                        
                        if (resultSet.next() && resultSet.getInt("test") == 1) {
                            return Health.up()
                                .withDetail("database", "MySQL")
                                .withDetail("status", "Connected")
                                .withDetail("validationQuery", "SELECT 1")
                                .withDetail("connectionValid", true)
                                .withDetail("url", connection.getMetaData().getURL())
                                .withDetail("driver", connection.getMetaData().getDriverName())
                                .withDetail("version", connection.getMetaData().getDatabaseProductVersion())
                                .build();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Database health check failed", e);
            return Health.down()
                .withDetail("database", "MySQL")
                .withDetail("error", e.getMessage())
                .withDetail("sqlState", e.getSQLState())
                .withDetail("errorCode", e.getErrorCode())
                .build();
        } catch (Exception e) {
            log.error("Unexpected error during database health check", e);
            return Health.down()
                .withDetail("database", "MySQL")
                .withDetail("error", e.getMessage())
                .withDetail("errorType", e.getClass().getSimpleName())
                .build();
        }
        
        return Health.down()
            .withDetail("database", "MySQL")
            .withDetail("error", "Connection validation failed")
            .build();
    }
}
