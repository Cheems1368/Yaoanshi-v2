package com.sqlapk.medicine.framework.monitor.core.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 短信服务健康检查指示器
 * 检查短信网关的健康状态和性能指标
 */
@Slf4j
@Component
public class SmsHealthIndicator implements HealthIndicator {

    @Autowired
    private RestTemplate restTemplate;

    // 短信服务配置
    @Value("${notification.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${notification.sms.api-url:}")
    private String smsApiUrl;

    @Value("${notification.sms.api-key:}")
    private String smsApiKey;

    @Value("${notification.sms.secret-key:}")
    private String smsSecretKey;

    @Value("${notification.sms.timeout:5000}")
    private int smsTimeout;

    // 短信服务健康统计
    private final ConcurrentHashMap<String, SmsHealthStats> smsStats = new ConcurrentHashMap<>();

    // 健康检查超时时间（毫秒）
    private static final long HEALTH_CHECK_TIMEOUT = 3000L;

    @Override
    public Health health() {
        if (!smsEnabled) {
            return Health.up()
                .withDetail("sms-service", "Disabled")
                .withDetail("status", "UP")
                .withDetail("reason", "SMS service is disabled")
                .build();
        }

        if (smsApiUrl.isEmpty()) {
            return Health.down()
                .withDetail("sms-service", "Not Configured")
                .withDetail("status", "DOWN")
                .withDetail("reason", "SMS API URL is not configured")
                .build();
        }

        Health.Builder builder = Health.up();
        boolean overallHealthy = true;

        // 检查短信API健康
        SmsHealthResult apiResult = checkSmsApiHealth();
        builder.withDetail("sms-api", apiResult);
        if (!apiResult.isHealthy()) {
            overallHealthy = false;
        }

        // 检查短信发送能力
        SmsHealthResult sendResult = checkSmsSendCapability();
        builder.withDetail("sms-send-capability", sendResult);
        if (!sendResult.isHealthy()) {
            overallHealthy = false;
        }

        // 检查短信配额和限制
        SmsHealthResult quotaResult = checkSmsQuota();
        builder.withDetail("sms-quota", quotaResult);
        if (!quotaResult.isHealthy()) {
            overallHealthy = false;
        }

        // 添加整体统计信息
        builder.withDetail("total-checks", smsStats.size())
               .withDetail("healthy-components", smsStats.values().stream()
                   .mapToInt(stats -> stats.isHealthy() ? 1 : 0).sum())
               .withDetail("overall-sms-health", overallHealthy ? "UP" : "DEGRADED");

        return overallHealthy ? builder.build() : builder.down().build();
    }

    /**
     * 检查短信API健康状态
     */
    private SmsHealthResult checkSmsApiHealth() {
        SmsHealthStats stats = smsStats.computeIfAbsent("sms-api", 
            k -> new SmsHealthStats());

        try {
            Instant start = Instant.now();
            
            // 执行短信API健康检查
            String healthUrl = smsApiUrl + "/health";
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                healthUrl, HttpMethod.GET, null, (Class<Map<String, Object>>) (Class<?>) Map.class);
            
            Duration duration = Duration.between(start, Instant.now());
            long responseTime = duration.toMillis();

            boolean isHealthy = response.getStatusCode().is2xxSuccessful();
            stats.recordCheck(isHealthy, responseTime);

            Map<String, Object> responseBody = response.getBody();
            String status = responseBody != null ? 
                (String) responseBody.getOrDefault("status", "unknown") : "unknown";

            return SmsHealthResult.builder()
                .componentName("sms-api")
                .status(isHealthy ? "UP" : "DOWN")
                .responseTime(responseTime)
                .successRate(stats.getSuccessRate())
                .lastCheckTime(Instant.now())
                .errorMessage(isHealthy ? null : "HTTP " + response.getStatusCode().value())
                .details(Map.of(
                    "api-status", status,
                    "api-url", smsApiUrl,
                    "timeout", smsTimeout
                ))
                .build();

        } catch (Exception e) {
            stats.recordCheck(false, HEALTH_CHECK_TIMEOUT);
            
            return SmsHealthResult.builder()
                .componentName("sms-api")
                .status("DOWN")
                .responseTime(HEALTH_CHECK_TIMEOUT)
                .successRate(stats.getSuccessRate())
                .lastCheckTime(Instant.now())
                .errorMessage(e.getMessage())
                .details(Map.of(
                    "api-url", smsApiUrl,
                    "timeout", smsTimeout
                ))
                .build();
        }
    }

    /**
     * 检查短信发送能力
     */
    private SmsHealthResult checkSmsSendCapability() {
        SmsHealthStats stats = smsStats.computeIfAbsent("sms-send", 
            k -> new SmsHealthStats());

        try {
            Instant start = Instant.now();
            
            // 执行短信发送能力测试（使用测试号码）
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("phone", "13800138000"); // 测试号码
            requestBody.put("message", "Health Check Test");
            requestBody.put("apiKey", smsApiKey);
            requestBody.put("secretKey", smsSecretKey);
            requestBody.put("test", true); // 标记为测试

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                smsApiUrl + "/send", request, (Class<Map<String, Object>>) (Class<?>) Map.class);
            
            Duration duration = Duration.between(start, Instant.now());
            long responseTime = duration.toMillis();

            boolean isHealthy = response.getStatusCode().is2xxSuccessful();
            stats.recordCheck(isHealthy, responseTime);

            Map<String, Object> responseBody = response.getBody();
            String sendStatus = responseBody != null ? 
                (String) responseBody.getOrDefault("status", "unknown") : "unknown";

            return SmsHealthResult.builder()
                .componentName("sms-send")
                .status(isHealthy ? "UP" : "DOWN")
                .responseTime(responseTime)
                .successRate(stats.getSuccessRate())
                .lastCheckTime(Instant.now())
                .errorMessage(isHealthy ? null : "Send test failed")
                .details(Map.of(
                    "send-status", sendStatus,
                    "test-phone", "13800138000",
                    "api-key-configured", !smsApiKey.isEmpty()
                ))
                .build();

        } catch (Exception e) {
            stats.recordCheck(false, HEALTH_CHECK_TIMEOUT);
            
            return SmsHealthResult.builder()
                .componentName("sms-send")
                .status("DOWN")
                .responseTime(HEALTH_CHECK_TIMEOUT)
                .successRate(stats.getSuccessRate())
                .lastCheckTime(Instant.now())
                .errorMessage(e.getMessage())
                .details(Map.of(
                    "test-phone", "13800138000",
                    "api-key-configured", !smsApiKey.isEmpty()
                ))
                .build();
        }
    }

    /**
     * 检查短信配额
     */
    private SmsHealthResult checkSmsQuota() {
        SmsHealthStats stats = smsStats.computeIfAbsent("sms-quota", 
            k -> new SmsHealthStats());

        try {
            Instant start = Instant.now();
            
            // 查询短信配额信息
            String quotaUrl = smsApiUrl + "/quota";
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                quotaUrl, HttpMethod.GET, null, (Class<Map<String, Object>>) (Class<?>) Map.class);
            
            Duration duration = Duration.between(start, Instant.now());
            long responseTime = duration.toMillis();

            boolean isHealthy = response.getStatusCode().is2xxSuccessful();
            stats.recordCheck(isHealthy, responseTime);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null) {
                Integer remaining = (Integer) responseBody.getOrDefault("remaining", 0);
                Integer total = (Integer) responseBody.getOrDefault("total", 0);
                double usagePercent = total > 0 ? (double) (total - remaining) / total * 100 : 0;

                // 配额健康判定
                boolean quotaHealthy = remaining > 100 && usagePercent < 90;

                return SmsHealthResult.builder()
                    .componentName("sms-quota")
                    .status(quotaHealthy ? "UP" : "DEGRADED")
                    .responseTime(responseTime)
                    .successRate(stats.getSuccessRate())
                    .lastCheckTime(Instant.now())
                    .errorMessage(quotaHealthy ? null : "Quota low or usage high")
                    .details(Map.of(
                        "remaining", remaining,
                        "total", total,
                        "usage-percent", String.format("%.1f%%", usagePercent),
                        "quota-healthy", quotaHealthy
                    ))
                    .build();
            } else {
                return SmsHealthResult.builder()
                    .componentName("sms-quota")
                    .status("DOWN")
                    .responseTime(responseTime)
                    .successRate(stats.getSuccessRate())
                    .lastCheckTime(Instant.now())
                    .errorMessage("No quota information available")
                    .build();
            }

        } catch (Exception e) {
            stats.recordCheck(false, HEALTH_CHECK_TIMEOUT);
            
            return SmsHealthResult.builder()
                .componentName("sms-quota")
                .status("DOWN")
                .responseTime(HEALTH_CHECK_TIMEOUT)
                .successRate(stats.getSuccessRate())
                .lastCheckTime(Instant.now())
                .errorMessage(e.getMessage())
                .build();
        }
    }

    /**
     * 短信健康结果
     */
    public static class SmsHealthResult {
        private String componentName;
        private String status;
        private long responseTime;
        private double successRate;
        private Instant lastCheckTime;
        private String errorMessage;
        private Map<String, Object> details;

        public boolean isHealthy() {
            return "UP".equals(status);
        }

        // Builder pattern
        public static SmsHealthResultBuilder builder() {
            return new SmsHealthResultBuilder();
        }

        public static class SmsHealthResultBuilder {
            private SmsHealthResult result = new SmsHealthResult();

            public SmsHealthResultBuilder componentName(String componentName) {
                result.componentName = componentName;
                return this;
            }

            public SmsHealthResultBuilder status(String status) {
                result.status = status;
                return this;
            }

            public SmsHealthResultBuilder responseTime(long responseTime) {
                result.responseTime = responseTime;
                return this;
            }

            public SmsHealthResultBuilder successRate(double successRate) {
                result.successRate = successRate;
                return this;
            }

            public SmsHealthResultBuilder lastCheckTime(Instant lastCheckTime) {
                result.lastCheckTime = lastCheckTime;
                return this;
            }

            public SmsHealthResultBuilder errorMessage(String errorMessage) {
                result.errorMessage = errorMessage;
                return this;
            }

            public SmsHealthResultBuilder details(Map<String, Object> details) {
                result.details = details;
                return this;
            }

            public SmsHealthResult build() {
                return result;
            }
        }

        // Getters
        public String getComponentName() { return componentName; }
        public String getStatus() { return status; }
        public long getResponseTime() { return responseTime; }
        public double getSuccessRate() { return successRate; }
        public Instant getLastCheckTime() { return lastCheckTime; }
        public String getErrorMessage() { return errorMessage; }
        public Map<String, Object> getDetails() { return details; }
    }

    /**
     * 短信健康统计
     */
    private static class SmsHealthStats {
        private final AtomicInteger totalChecks = new AtomicInteger(0);
        private final AtomicInteger successfulChecks = new AtomicInteger(0);
        private final AtomicLong totalResponseTime = new AtomicLong(0);
        private final AtomicInteger consecutiveFailures = new AtomicInteger(0);

        public void recordCheck(boolean success, long responseTime) {
            totalChecks.incrementAndGet();
            totalResponseTime.addAndGet(responseTime);
            
            if (success) {
                successfulChecks.incrementAndGet();
                consecutiveFailures.set(0);
            } else {
                consecutiveFailures.incrementAndGet();
            }
        }

        public double getSuccessRate() {
            int total = totalChecks.get();
            return total > 0 ? (double) successfulChecks.get() / total * 100 : 0.0;
        }

        public long getAverageResponseTime() {
            int total = totalChecks.get();
            return total > 0 ? totalResponseTime.get() / total : 0;
        }

        public boolean isHealthy() {
            double successRate = getSuccessRate();
            int consecutiveFailures = this.consecutiveFailures.get();
            
            // 短信服务健康判定标准：
            // UP: 成功率 >= 99% 且 连续失败 < 3次
            // DEGRADED: 成功率 95%-99% 或 连续失败 2-3次
            // DOWN: 成功率 < 95% 或 连续失败 >= 3次
            return successRate >= 99.0 && consecutiveFailures < 3;
        }
    }
}
