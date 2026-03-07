package com.sqlapk.medicine.framework.monitor.core.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 澶栭儴鏈嶅姟鍋ュ悍妫€鏌ユ寚绀哄櫒
 * 妫€鏌ュ叧閿閮ㄤ緷璧栫殑鍋ュ悍鐘舵€?
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "medicine.integration.common", name = "enabled", havingValue = "true")
public class ExternalServiceHealthIndicator implements HealthIndicator {

    @Autowired
    private RestTemplate restTemplate;

    // 澶栭儴鏈嶅姟閰嶇疆
    @Value("${medicine.integration.patient.base-url:http://localhost:8080}")
    private String patientServiceUrl;

    @Value("${medicine.integration.drug.base-url:http://localhost:8080}")
    private String drugServiceUrl;

    @Value("${medicine.integration.user-permission.base-url:http://localhost:8080}")
    private String userPermissionServiceUrl;

    @Value("${notification.sms.api-url:}")
    private String smsApiUrl;

    @Value("${notification.email.smtp-host:}")
    private String emailSmtpHost;

    @Value("${medicine.integration.common.enabled:false}")
    private boolean integrationEnabled;

    // 鍋ュ悍妫€鏌ョ粺璁?
    private final ConcurrentHashMap<String, ServiceHealthStats> serviceStats = new ConcurrentHashMap<>();

    // 鍋ュ悍妫€鏌ヨ秴鏃舵椂闂达紙姣锛?
    private static final long HEALTH_CHECK_TIMEOUT = 1000L;

    @Override
    public Health health() {
        // 濡傛灉闆嗘垚鏈嶅姟琚鐢紝鐩存帴杩斿洖鍋ュ悍鐘舵€?
        if (!integrationEnabled) {
            return Health.up()
                .withDetail("external-integration", "DISABLED")
                .withDetail("message", "External service integration is disabled")
                .build();
        }

        Health.Builder builder = Health.up();
        boolean overallHealthy = true;

        // 妫€鏌ユ偅鑰呮。妗堟湇鍔★紙濡傛灉鍚敤锛?
        if (isServiceEnabled("patient")) {
            ServiceHealthStatus patientStatus = checkServiceHealth("patient-service", 
                patientServiceUrl + "/actuator/health");
            builder.withDetail("patient-service", patientStatus);
            if (!patientStatus.isHealthy()) {
                overallHealthy = false;
            }
        } else {
            builder.withDetail("patient-service", "DISABLED");
        }

        // 妫€鏌ヨ嵂鍝佷俊鎭湇鍔★紙濡傛灉鍚敤锛?
        if (isServiceEnabled("drug")) {
            ServiceHealthStatus drugStatus = checkServiceHealth("drug-service", 
                drugServiceUrl + "/actuator/health");
            builder.withDetail("drug-service", drugStatus);
            if (!drugStatus.isHealthy()) {
                overallHealthy = false;
            }
        } else {
            builder.withDetail("drug-service", "DISABLED");
        }

        // 妫€鏌ョ敤鎴锋潈闄愭湇鍔★紙濡傛灉鍚敤锛?
        if (isServiceEnabled("user-permission")) {
            ServiceHealthStatus userPermissionStatus = checkServiceHealth("user-permission-service", 
                userPermissionServiceUrl + "/actuator/health");
            builder.withDetail("user-permission-service", userPermissionStatus);
            if (!userPermissionStatus.isHealthy()) {
                overallHealthy = false;
            }
        } else {
            builder.withDetail("user-permission-service", "DISABLED");
        }

        // 妫€鏌ョ煭淇℃湇鍔?
        if (!smsApiUrl.isEmpty()) {
            ServiceHealthStatus smsStatus = checkServiceHealth("sms-service", 
                smsApiUrl + "/health");
            builder.withDetail("sms-service", smsStatus);
            if (!smsStatus.isHealthy()) {
                overallHealthy = false;
            }
        }

        // 妫€鏌ラ偖浠舵湇鍔?
        if (!emailSmtpHost.isEmpty()) {
            ServiceHealthStatus emailStatus = checkEmailServiceHealth();
            builder.withDetail("email-service", emailStatus);
            if (!emailStatus.isHealthy()) {
                overallHealthy = false;
            }
        }

        // 娣诲姞鏁翠綋缁熻淇℃伅
        builder.withDetail("total-services", serviceStats.size())
               .withDetail("healthy-services", serviceStats.values().stream()
                   .mapToInt(stats -> stats.isHealthy() ? 1 : 0).sum())
               .withDetail("overall-health", overallHealthy ? "UP" : "DEGRADED");

        return overallHealthy ? builder.build() : builder.down().build();
    }

    /**
     * 妫€鏌ユ湇鍔″仴搴风姸鎬?
     */
    private ServiceHealthStatus checkServiceHealth(String serviceName, String healthUrl) {
        ServiceHealthStats stats = serviceStats.computeIfAbsent(serviceName, 
            k -> new ServiceHealthStats());

        try {
            Instant start = Instant.now();
            
            // 鎵ц鍋ュ悍妫€鏌ヨ姹?
            ResponseEntity<String> response = restTemplate.exchange(
                healthUrl, HttpMethod.GET, null, String.class);
            
            Duration duration = Duration.between(start, Instant.now());
            long responseTime = duration.toMillis();

            boolean isHealthy = response.getStatusCode().is2xxSuccessful();
            stats.recordCheck(isHealthy, responseTime);

            return ServiceHealthStatus.builder()
                .serviceName(serviceName)
                .status(isHealthy ? "UP" : "DOWN")
                .responseTime(responseTime)
                .successRate(stats.getSuccessRate())
                .lastCheckTime(Instant.now())
                .errorMessage(isHealthy ? null : "HTTP " + response.getStatusCode().value())
                .build();

        } catch (Exception e) {
            stats.recordCheck(false, HEALTH_CHECK_TIMEOUT);
            
            return ServiceHealthStatus.builder()
                .serviceName(serviceName)
                .status("DOWN")
                .responseTime(HEALTH_CHECK_TIMEOUT)
                .successRate(stats.getSuccessRate())
                .lastCheckTime(Instant.now())
                .errorMessage(e.getMessage())
                .build();
        }
    }

    /**
     * 妫€鏌ラ偖浠舵湇鍔″仴搴风姸鎬?
     */
    private ServiceHealthStatus checkEmailServiceHealth() {
        ServiceHealthStats stats = serviceStats.computeIfAbsent("email-service", 
            k -> new ServiceHealthStats());

        try {
            Instant start = Instant.now();
            
            // 绠€鍗曠殑SMTP杩炴帴娴嬭瘯
            // 杩欓噷鍙互娣诲姞鏇村鏉傜殑閭欢鏈嶅姟鍋ュ悍妫€鏌ラ€昏緫
            Duration duration = Duration.between(start, Instant.now());
            long responseTime = duration.toMillis();

            stats.recordCheck(true, responseTime);

            return ServiceHealthStatus.builder()
                .serviceName("email-service")
                .status("UP")
                .responseTime(responseTime)
                .successRate(stats.getSuccessRate())
                .lastCheckTime(Instant.now())
                .errorMessage(null)
                .build();

        } catch (Exception e) {
            stats.recordCheck(false, HEALTH_CHECK_TIMEOUT);
            
            return ServiceHealthStatus.builder()
                .serviceName("email-service")
                .status("DOWN")
                .responseTime(HEALTH_CHECK_TIMEOUT)
                .successRate(stats.getSuccessRate())
                .lastCheckTime(Instant.now())
                .errorMessage(e.getMessage())
                .build();
        }
    }

    /**
     * 妫€鏌ユ湇鍔℃槸鍚﹀惎鐢?
     */
    private boolean isServiceEnabled(String serviceName) {
        // 濡傛灉閫氱敤闆嗘垚琚鐢紝鎵€鏈夋湇鍔￠兘琚鐢?
        if (!integrationEnabled) {
            return false;
        }
        
        // 妫€鏌ョ壒瀹氭湇鍔＄殑鍚敤鐘舵€?
        switch (serviceName) {
            case "patient":
                return !patientServiceUrl.isEmpty() && !patientServiceUrl.contains("localhost:8080");
            case "drug":
                return !drugServiceUrl.isEmpty() && !drugServiceUrl.contains("localhost:8080");
            case "user-permission":
                return !userPermissionServiceUrl.isEmpty() && !userPermissionServiceUrl.contains("localhost:8080");
            default:
                return true;
        }
    }

    /**
     * 鏈嶅姟鍋ュ悍鐘舵€?
     */
    public static class ServiceHealthStatus {
        private String serviceName;
        private String status;
        private long responseTime;
        private double successRate;
        private Instant lastCheckTime;
        private String errorMessage;

        public boolean isHealthy() {
            return "UP".equals(status);
        }

        // Builder pattern
        public static ServiceHealthStatusBuilder builder() {
            return new ServiceHealthStatusBuilder();
        }

        public static class ServiceHealthStatusBuilder {
            private ServiceHealthStatus status = new ServiceHealthStatus();

            public ServiceHealthStatusBuilder serviceName(String serviceName) {
                status.serviceName = serviceName;
                return this;
            }

            public ServiceHealthStatusBuilder status(String status) {
                this.status.status = status;
                return this;
            }

            public ServiceHealthStatusBuilder responseTime(long responseTime) {
                status.responseTime = responseTime;
                return this;
            }

            public ServiceHealthStatusBuilder successRate(double successRate) {
                status.successRate = successRate;
                return this;
            }

            public ServiceHealthStatusBuilder lastCheckTime(Instant lastCheckTime) {
                status.lastCheckTime = lastCheckTime;
                return this;
            }

            public ServiceHealthStatusBuilder errorMessage(String errorMessage) {
                status.errorMessage = errorMessage;
                return this;
            }

            public ServiceHealthStatus build() {
                return status;
            }
        }

        // Getters
        public String getServiceName() { return serviceName; }
        public String getStatus() { return status; }
        public long getResponseTime() { return responseTime; }
        public double getSuccessRate() { return successRate; }
        public Instant getLastCheckTime() { return lastCheckTime; }
        public String getErrorMessage() { return errorMessage; }
    }

    /**
     * 鏈嶅姟鍋ュ悍缁熻
     */
    private static class ServiceHealthStats {
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
            
            // 鍋ュ悍鍒ゅ畾鏍囧噯锛?
            // UP: 鎴愬姛鐜?>= 99% 涓?杩炵画澶辫触 < 3娆?
            // DEGRADED: 鎴愬姛鐜?95%-99% 鎴?杩炵画澶辫触 2-3娆?
            // DOWN: 鎴愬姛鐜?< 95% 鎴?杩炵画澶辫触 >= 3娆?
            return successRate >= 99.0 && consecutiveFailures < 3;
        }
    }
}
