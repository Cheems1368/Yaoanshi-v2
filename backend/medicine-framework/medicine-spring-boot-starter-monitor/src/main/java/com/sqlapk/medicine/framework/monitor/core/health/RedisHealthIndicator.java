package com.sqlapk.medicine.framework.monitor.core.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Redis健康检查指示器
 * 检查Redis连接状态和基本功能
 */
@Slf4j
@Component
public class RedisHealthIndicator implements HealthIndicator {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Health health() {
        try {
            // 检查Redis连接
            String pong = redisTemplate.getConnectionFactory()
                .getConnection().ping();
            
            if ("PONG".equals(pong)) {
                // 获取Redis服务器信息
                Properties info = redisTemplate.getConnectionFactory()
                    .getConnection().info();
                
                return Health.up()
                    .withDetail("redis", "Connected")
                    .withDetail("ping", pong)
                    .withDetail("version", info.getProperty("redis_version"))
                    .withDetail("mode", info.getProperty("redis_mode"))
                    .withDetail("uptime", info.getProperty("uptime_in_seconds"))
                    .withDetail("connectedClients", info.getProperty("connected_clients"))
                    .withDetail("usedMemory", info.getProperty("used_memory_human"))
                    .withDetail("maxMemory", info.getProperty("maxmemory_human"))
                    .build();
            }
        } catch (Exception e) {
            log.error("Redis health check failed", e);
            return Health.down()
                .withDetail("redis", "Disconnected")
                .withDetail("error", e.getMessage())
                .withDetail("errorType", e.getClass().getSimpleName())
                .build();
        }
        
        return Health.down()
            .withDetail("redis", "Connection failed")
            .withDetail("ping", "No response")
            .build();
    }
}
