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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 绗笁鏂硅嵂鍝佹绱PI鍋ュ悍妫€鏌ユ寚绀哄櫒
 * 妫€鏌ヨ嵂鍝佷俊鎭疉PI鍜岃繃鏁忔鏌PI鐨勫仴搴风姸鎬?
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "medicine.integration.drug", name = "enabled", havingValue = "true")
public class DrugApiHealthIndicator implements HealthIndicator {

    @Autowired
    private RestTemplate restTemplate;

    // 鑽搧API閰嶇疆
    @Value("${medicine.integration.drug.base-url:http://localhost:8080}")
    private String drugApiBaseUrl;

    @Value("${medicine.integration.drug.allergy-check-url:http://localhost:8080}")
    private String allergyCheckUrl;

    @Value("${medicine.integration.drug.connect-timeout:5000}")
    private int connectTimeout;

    @Value("${medicine.integration.drug.read-timeout:10000}")
    private int readTimeout;

    @Value("${medicine.integration.drug.retry-count:3}")
    private int retryCount;

    @Value("${medicine.integration.drug.enabled:false}")
    private boolean drugIntegrationEnabled;

    // 鑽搧API鍋ュ悍缁熻
    private final ConcurrentHashMap<String, DrugApiHealthStats> apiStats = new ConcurrentHashMap<>();

    // 鍋ュ悍妫€鏌ヨ秴鏃舵椂闂达紙姣锛?
    private static final long HEALTH_CHECK_TIMEOUT = 3000L;

    @Override
    public Health health() {
        // 濡傛灉鑽搧闆嗘垚鏈嶅姟琚鐢紝鐩存帴杩斿洖鍋ュ悍鐘舵€?
        if (!drugIntegrationEnabled) {
            return Health.up()
                .withDetail("drug-integration", "DISABLED")
                .withDetail("message", "Drug integration service is disabled")
                .build();
        }

        Health.Builder builder = Health.up();
        boolean overallHealthy = true;

        // 妫€鏌ヨ嵂鍝佷俊鎭疉PI鍋ュ悍
        DrugApiHealthResult drugInfoResult = checkDrugInfoApiHealth();
        builder.withDetail("drug-info-api", drugInfoResult);
        if (!drugInfoResult.isHealthy()) {
            overallHealthy = false;
        }

        // 妫€鏌ヨ繃鏁忔鏌PI鍋ュ悍
        DrugApiHealthResult allergyResult = checkAllergyCheckApiHealth();
        builder.withDetail("allergy-check-api", allergyResult);
        if (!allergyResult.isHealthy()) {
            overallHealthy = false;
        }

        // 妫€鏌ヨ嵂鍝佹悳绱PI鍋ュ悍
        DrugApiHealthResult searchResult = checkDrugSearchApiHealth();
        builder.withDetail("drug-search-api", searchResult);
        if (!searchResult.isHealthy()) {
            overallHealthy = false;
        }

        // 妫€鏌PI鍝嶅簲鏃堕棿
        DrugApiHealthResult performanceResult = checkApiPerformance();
        builder.withDetail("api-performance", performanceResult);
        if (!performanceResult.isHealthy()) {
            overallHealthy = false;
        }

        // 娣诲姞鏁翠綋缁熻淇℃伅
        builder.withDetail("total-apis", apiStats.size())
               .withDetail("healthy-apis", apiStats.values().stream()
                   .mapToInt(stats -> stats.isHealthy() ? 1 : 0).sum())
               .withDetail("overall-drug-api-health", overallHealthy ? "UP" : "DEGRADED");

        return overallHealthy ? builder.build() : builder.down().build();
    }

    /**
     * 妫€鏌ヨ嵂鍝佷俊鎭疉PI鍋ュ悍鐘舵€?
     */
    private DrugApiHealthResult checkDrugInfoApiHealth() {
        DrugApiHealthStats stats = apiStats.computeIfAbsent("drug-info-api", 
            k -> new DrugApiHealthStats());

        try {
            Instant start = Instant.now();
            
            // 鎵ц鑽搧淇℃伅API鍋ュ悍妫€鏌?
            String healthUrl = drugApiBaseUrl + "/actuator/health";
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                healthUrl, HttpMethod.GET, null, (Class<Map<String, Object>>) (Class<?>) Map.class);
            
            Duration duration = Duration.between(start, Instant.now());
            long responseTime = duration.toMillis();

            boolean isHealthy = response.getStatusCode().is2xxSuccessful();
            stats.recordCheck(isHealthy, responseTime);

            Map<String, Object> responseBody = response.getBody();
            String status = responseBody != null ? 
                (String) responseBody.getOrDefault("status", "unknown") : "unknown";

            return DrugApiHealthResult.builder()
                .apiName("drug-info-api")
                .status(isHealthy ? "UP" : "DOWN")
                .responseTime(responseTime)
                .successRate(stats.getSuccessRate())
                .lastCheckTime(Instant.now())
                .errorMessage(isHealthy ? null : "HTTP " + response.getStatusCode().value())
                .details(Map.of(
                    "api-status", status,
                    "api-url", drugApiBaseUrl,
                    "connect-timeout", connectTimeout,
                    "read-timeout", readTimeout,
                    "retry-count", retryCount
                ))
                .build();

        } catch (Exception e) {
            stats.recordCheck(false, HEALTH_CHECK_TIMEOUT);
            
            return DrugApiHealthResult.builder()
                .apiName("drug-info-api")
                .status("DOWN")
                .responseTime(HEALTH_CHECK_TIMEOUT)
                .successRate(stats.getSuccessRate())
                .lastCheckTime(Instant.now())
                .errorMessage(e.getMessage())
                .details(Map.of(
                    "api-url", drugApiBaseUrl,
                    "connect-timeout", connectTimeout,
                    "read-timeout", readTimeout
                ))
                .build();
        }
    }

    /**
     * 妫€鏌ヨ繃鏁忔鏌PI鍋ュ悍鐘舵€?
     */
    private DrugApiHealthResult checkAllergyCheckApiHealth() {
        DrugApiHealthStats stats = apiStats.computeIfAbsent("allergy-check-api", 
            k -> new DrugApiHealthStats());

        try {
            Instant start = Instant.now();
            
            // 鎵ц杩囨晱妫€鏌PI鍋ュ悍妫€鏌?
            String healthUrl = allergyCheckUrl + "/actuator/health";
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                healthUrl, HttpMethod.GET, null, (Class<Map<String, Object>>) (Class<?>) Map.class);
            
            Duration duration = Duration.between(start, Instant.now());
            long responseTime = duration.toMillis();

            boolean isHealthy = response.getStatusCode().is2xxSuccessful();
            stats.recordCheck(isHealthy, responseTime);

            Map<String, Object> responseBody = response.getBody();
            String status = responseBody != null ? 
                (String) responseBody.getOrDefault("status", "unknown") : "unknown";

            return DrugApiHealthResult.builder()
                .apiName("allergy-check-api")
                .status(isHealthy ? "UP" : "DOWN")
                .responseTime(responseTime)
                .successRate(stats.getSuccessRate())
                .lastCheckTime(Instant.now())
                .errorMessage(isHealthy ? null : "HTTP " + response.getStatusCode().value())
                .details(Map.of(
                    "api-status", status,
                    "api-url", allergyCheckUrl,
                    "connect-timeout", connectTimeout,
                    "read-timeout", readTimeout
                ))
                .build();

        } catch (Exception e) {
            stats.recordCheck(false, HEALTH_CHECK_TIMEOUT);
            
            return DrugApiHealthResult.builder()
                .apiName("allergy-check-api")
                .status("DOWN")
                .responseTime(HEALTH_CHECK_TIMEOUT)
                .successRate(stats.getSuccessRate())
                .lastCheckTime(Instant.now())
                .errorMessage(e.getMessage())
                .details(Map.of(
                    "api-url", allergyCheckUrl,
                    "connect-timeout", connectTimeout,
                    "read-timeout", readTimeout
                ))
                .build();
        }
    }

    /**
     * 妫€鏌ヨ嵂鍝佹悳绱PI鍋ュ悍鐘舵€?
     */
    private DrugApiHealthResult checkDrugSearchApiHealth() {
        DrugApiHealthStats stats = apiStats.computeIfAbsent("drug-search-api", 
            k -> new DrugApiHealthStats());

        try {
            Instant start = Instant.now();
            
            // 鎵ц鑽搧鎼滅储API娴嬭瘯
            String searchUrl = drugApiBaseUrl + "/api/drug/search?name=test";
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                searchUrl, HttpMethod.GET, null, (Class<Map<String, Object>>) (Class<?>) Map.class);
            
            Duration duration = Duration.between(start, Instant.now());
            long responseTime = duration.toMillis();

            boolean isHealthy = response.getStatusCode().is2xxSuccessful();
            stats.recordCheck(isHealthy, responseTime);

            Map<String, Object> responseBody = response.getBody();
            String searchStatus = responseBody != null ? 
                (String) responseBody.getOrDefault("status", "unknown") : "unknown";

            return DrugApiHealthResult.builder()
                .apiName("drug-search-api")
                .status(isHealthy ? "UP" : "DOWN")
                .responseTime(responseTime)
                .successRate(stats.getSuccessRate())
                .lastCheckTime(Instant.now())
                .errorMessage(isHealthy ? null : "Search API test failed")
                .details(Map.of(
                    "search-status", searchStatus,
                    "search-url", searchUrl,
                    "test-query", "test",
                    "response-size", responseBody != null ? responseBody.size() : 0
                ))
                .build();

        } catch (Exception e) {
            stats.recordCheck(false, HEALTH_CHECK_TIMEOUT);
            
            return DrugApiHealthResult.builder()
                .apiName("drug-search-api")
                .status("DOWN")
                .responseTime(HEALTH_CHECK_TIMEOUT)
                .successRate(stats.getSuccessRate())
                .lastCheckTime(Instant.now())
                .errorMessage(e.getMessage())
                .details(Map.of(
                    "search-url", drugApiBaseUrl + "/api/drug/search",
                    "test-query", "test"
                ))
                .build();
        }
    }

    /**
     * 妫€鏌PI鎬ц兘
     */
    private DrugApiHealthResult checkApiPerformance() {
        DrugApiHealthStats stats = apiStats.computeIfAbsent("api-performance", 
            k -> new DrugApiHealthStats());

        try {
            Instant start = Instant.now();
            
            // 鎵ц鎬ц兘娴嬭瘯
            boolean performanceHealthy = performPerformanceTest();
            
            Duration duration = Duration.between(start, Instant.now());
            long responseTime = duration.toMillis();

            stats.recordCheck(performanceHealthy, responseTime);

            return DrugApiHealthResult.builder()
                .apiName("api-performance")
                .status(performanceHealthy ? "UP" : "DEGRADED")
                .responseTime(responseTime)
                .successRate(stats.getSuccessRate())
                .lastCheckTime(Instant.now())
                .errorMessage(performanceHealthy ? null : "Performance test failed")
                .details(Map.of(
                    "performance-healthy", performanceHealthy,
                    "test-type", "concurrent-requests",
                    "target-response-time", "500ms",
                    "actual-response-time", responseTime + "ms"
                ))
                .build();

        } catch (Exception e) {
            stats.recordCheck(false, HEALTH_CHECK_TIMEOUT);
            
            return DrugApiHealthResult.builder()
                .apiName("api-performance")
                .status("DOWN")
                .responseTime(HEALTH_CHECK_TIMEOUT)
                .successRate(stats.getSuccessRate())
                .lastCheckTime(Instant.now())
                .errorMessage(e.getMessage())
                .build();
        }
    }

    /**
     * 鎵ц鎬ц兘娴嬭瘯
     */
    private boolean performPerformanceTest() {
        try {
            // 妯℃嫙骞跺彂璇锋眰娴嬭瘯
            long startTime = System.currentTimeMillis();
            
            // 鎵ц澶氫釜骞跺彂璇锋眰
            for (int i = 0; i < 5; i++) {
                String testUrl = drugApiBaseUrl + "/api/drug/search?name=test" + i;
                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    testUrl, HttpMethod.GET, null, (Class<Map<String, Object>>) (Class<?>) Map.class);
                
                if (!response.getStatusCode().is2xxSuccessful()) {
                    return false;
                }
            }
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            // 鎬ц兘鍒ゅ畾锛?涓姹傛€绘椂闂?< 2.5绉掞紙骞冲潎姣忎釜 < 500ms锛?
            return totalTime < 2500;
            
        } catch (Exception e) {
            log.warn("鎬ц兘娴嬭瘯澶辫触", e);
            return false;
        }
    }

    /**
     * 鑽搧API鍋ュ悍缁撴灉
     */
    public static class DrugApiHealthResult {
        private String apiName;
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
        public static DrugApiHealthResultBuilder builder() {
            return new DrugApiHealthResultBuilder();
        }

        public static class DrugApiHealthResultBuilder {
            private DrugApiHealthResult result = new DrugApiHealthResult();

            public DrugApiHealthResultBuilder apiName(String apiName) {
                result.apiName = apiName;
                return this;
            }

            public DrugApiHealthResultBuilder status(String status) {
                result.status = status;
                return this;
            }

            public DrugApiHealthResultBuilder responseTime(long responseTime) {
                result.responseTime = responseTime;
                return this;
            }

            public DrugApiHealthResultBuilder successRate(double successRate) {
                result.successRate = successRate;
                return this;
            }

            public DrugApiHealthResultBuilder lastCheckTime(Instant lastCheckTime) {
                result.lastCheckTime = lastCheckTime;
                return this;
            }

            public DrugApiHealthResultBuilder errorMessage(String errorMessage) {
                result.errorMessage = errorMessage;
                return this;
            }

            public DrugApiHealthResultBuilder details(Map<String, Object> details) {
                result.details = details;
                return this;
            }

            public DrugApiHealthResult build() {
                return result;
            }
        }

        // Getters
        public String getApiName() { return apiName; }
        public String getStatus() { return status; }
        public long getResponseTime() { return responseTime; }
        public double getSuccessRate() { return successRate; }
        public Instant getLastCheckTime() { return lastCheckTime; }
        public String getErrorMessage() { return errorMessage; }
        public Map<String, Object> getDetails() { return details; }
    }

    /**
     * 鑽搧API鍋ュ悍缁熻
     */
    private static class DrugApiHealthStats {
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
            
            // 鑽搧API鍋ュ悍鍒ゅ畾鏍囧噯锛?
            // UP: 鎴愬姛鐜?>= 99% 涓?杩炵画澶辫触 < 3娆?
            // DEGRADED: 鎴愬姛鐜?95%-99% 鎴?杩炵画澶辫触 2-3娆?
            // DOWN: 鎴愬姛鐜?< 95% 鎴?杩炵画澶辫触 >= 3娆?
            return successRate >= 99.0 && consecutiveFailures < 3;
        }
    }
}
