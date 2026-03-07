package com.sqlapk.medicine.module.medication.service.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存策略服务
 */
@Slf4j
@Service
public class CacheStrategyService {

    @Autowired
    private CacheService cacheService;

    // 缓存统计
    private final ConcurrentHashMap<String, CacheStats> cacheStats = new ConcurrentHashMap<>();

    /**
     * 缓存统计信息
     */
    public static class CacheStats {
        private final AtomicLong hits = new AtomicLong(0);
        private final AtomicLong misses = new AtomicLong(0);
        private final AtomicLong evictions = new AtomicLong(0);
        private final AtomicLong puts = new AtomicLong(0);

        public long getHits() { return hits.get(); }
        public long getMisses() { return misses.get(); }
        public long getEvictions() { return evictions.get(); }
        public long getPuts() { return puts.get(); }
        public long getTotalRequests() { return hits.get() + misses.get(); }
        public double getHitRate() {
            long total = getTotalRequests();
            return total > 0 ? (double) hits.get() / total : 0.0;
        }

        public void recordHit() { hits.incrementAndGet(); }
        public void recordMiss() { misses.incrementAndGet(); }
        public void recordEviction() { evictions.incrementAndGet(); }
        public void recordPut() { puts.incrementAndGet(); }
    }

    /**
     * 智能缓存获取
     */
    public <T> T getWithStrategy(String key, Class<T> type, CacheStrategy strategy) {
        CacheStats stats = cacheStats.computeIfAbsent(key, k -> new CacheStats());
        
        Object cached = cacheService.get(key);
        if (cached != null) {
            stats.recordHit();
            log.debug("缓存命中: {}", key);
            return type.cast(cached);
        }
        
        stats.recordMiss();
        log.debug("缓存未命中: {}", key);
        return null;
    }

    /**
     * 智能缓存设置
     */
    public void setWithStrategy(String key, Object value, CacheStrategy strategy) {
        CacheStats stats = cacheStats.computeIfAbsent(key, k -> new CacheStats());
        
        Duration ttl = calculateTTL(strategy);
        cacheService.set(key, value, ttl);
        stats.recordPut();
        
        log.debug("缓存设置: {} (TTL: {})", key, ttl);
    }

    /**
     * 计算TTL
     */
    private Duration calculateTTL(CacheStrategy strategy) {
        switch (strategy) {
            case SHORT_TERM:
                return Duration.ofMinutes(5);
            case MEDIUM_TERM:
                return Duration.ofMinutes(30);
            case LONG_TERM:
                return Duration.ofHours(2);
            case PERSISTENT:
                return Duration.ofDays(1);
            default:
                return Duration.ofMinutes(15);
        }
    }

    /**
     * 缓存预热
     */
    public void warmupCache(String key, Object value, CacheStrategy strategy) {
        log.info("缓存预热: {}", key);
        setWithStrategy(key, value, strategy);
    }

    /**
     * 批量缓存预热
     */
    public void warmupCacheBatch(List<CacheItem> items) {
        log.info("批量缓存预热开始，项目数: {}", items.size());
        
        for (CacheItem item : items) {
            try {
                setWithStrategy(item.getKey(), item.getValue(), item.getStrategy());
            } catch (Exception e) {
                log.error("缓存预热失败: {}, 错误: {}", item.getKey(), e.getMessage());
            }
        }
        
        log.info("批量缓存预热完成");
    }

    /**
     * 缓存项
     */
    public static class CacheItem {
        private String key;
        private Object value;
        private CacheStrategy strategy;

        public CacheItem(String key, Object value, CacheStrategy strategy) {
            this.key = key;
            this.value = value;
            this.strategy = strategy;
        }

        // Getters
        public String getKey() { return key; }
        public Object getValue() { return value; }
        public CacheStrategy getStrategy() { return strategy; }
    }

    /**
     * 缓存策略枚举
     */
    public enum CacheStrategy {
        SHORT_TERM,    // 短期缓存（5分钟）
        MEDIUM_TERM,   // 中期缓存（30分钟）
        LONG_TERM,     // 长期缓存（2小时）
        PERSISTENT     // 持久缓存（1天）
    }

    /**
     * 获取缓存统计
     */
    public CacheStats getCacheStats(String key) {
        return cacheStats.get(key);
    }

    /**
     * 获取所有缓存统计
     */
    public ConcurrentHashMap<String, CacheStats> getAllCacheStats() {
        return new ConcurrentHashMap<>(cacheStats);
    }

    /**
     * 清理缓存统计
     */
    public void clearCacheStats() {
        cacheStats.clear();
        log.info("缓存统计已清理");
    }

    /**
     * 缓存健康检查
     */
    public CacheHealthStatus checkCacheHealth() {
        CacheHealthStatus status = new CacheHealthStatus();
        
        long totalHits = 0;
        long totalMisses = 0;
        long totalEvictions = 0;
        long totalPuts = 0;
        
        for (CacheStats stats : cacheStats.values()) {
            totalHits += stats.getHits();
            totalMisses += stats.getMisses();
            totalEvictions += stats.getEvictions();
            totalPuts += stats.getPuts();
        }
        
        status.setTotalHits(totalHits);
        status.setTotalMisses(totalMisses);
        status.setTotalEvictions(totalEvictions);
        status.setTotalPuts(totalPuts);
        status.setHitRate(totalHits + totalMisses > 0 ? (double) totalHits / (totalHits + totalMisses) : 0.0);
        status.setCheckTime(LocalDateTime.now());
        
        // 判断健康状态
        if (status.getHitRate() >= 0.8) {
            status.setHealthStatus("健康");
        } else if (status.getHitRate() >= 0.6) {
            status.setHealthStatus("一般");
        } else {
            status.setHealthStatus("需要优化");
        }
        
        return status;
    }

    /**
     * 缓存健康状态
     */
    public static class CacheHealthStatus {
        private long totalHits;
        private long totalMisses;
        private long totalEvictions;
        private long totalPuts;
        private double hitRate;
        private String healthStatus;
        private LocalDateTime checkTime;

        // Getters and Setters
        public long getTotalHits() { return totalHits; }
        public void setTotalHits(long totalHits) { this.totalHits = totalHits; }

        public long getTotalMisses() { return totalMisses; }
        public void setTotalMisses(long totalMisses) { this.totalMisses = totalMisses; }

        public long getTotalEvictions() { return totalEvictions; }
        public void setTotalEvictions(long totalEvictions) { this.totalEvictions = totalEvictions; }

        public long getTotalPuts() { return totalPuts; }
        public void setTotalPuts(long totalPuts) { this.totalPuts = totalPuts; }

        public double getHitRate() { return hitRate; }
        public void setHitRate(double hitRate) { this.hitRate = hitRate; }

        public String getHealthStatus() { return healthStatus; }
        public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }

        public LocalDateTime getCheckTime() { return checkTime; }
        public void setCheckTime(LocalDateTime checkTime) { this.checkTime = checkTime; }
    }

    /**
     * 缓存注解支持方法
     */
    
    @Cacheable(value = "patient-profile", key = "#patientId")
    public Object getPatientProfile(Long patientId) {
        log.debug("从数据库获取患者档案: {}", patientId);
        
        // 这里应该从数据库获取患者档案信息
        // 暂时返回一个模拟的患者档案对象
        java.util.Map<String, Object> profile = new java.util.HashMap<>();
        profile.put("patientId", patientId);
        profile.put("name", "患者" + patientId);
        profile.put("age", 30);
        profile.put("gender", "男");
        profile.put("lastUpdateTime", java.time.LocalDateTime.now());
        
        return profile;
    }

    @CachePut(value = "patient-profile", key = "#patientId")
    public Object updatePatientProfile(Long patientId, Object profile) {
        log.debug("更新患者档案缓存: {}", patientId);
        return profile;
    }

    @CacheEvict(value = "patient-profile", key = "#patientId")
    public void evictPatientProfile(Long patientId) {
        log.debug("清除患者档案缓存: {}", patientId);
    }

    @Cacheable(value = "medication-plan", key = "#planId")
    public Object getMedicationPlan(Long planId) {
        log.debug("从数据库获取用药计划: {}", planId);
        return null; // 实际应该从数据库获取
    }

    @CachePut(value = "medication-plan", key = "#planId")
    public Object updateMedicationPlan(Long planId, Object plan) {
        log.debug("更新用药计划缓存: {}", planId);
        return plan;
    }

    @CacheEvict(value = "medication-plan", key = "#planId")
    public void evictMedicationPlan(Long planId) {
        log.debug("清除用药计划缓存: {}", planId);
    }

    @Cacheable(value = "today-task", key = "#patientId")
    public Object getTodayTask(Long patientId) {
        log.debug("从数据库获取今日待办: {}", patientId);
        return null; // 实际应该从数据库获取
    }

    @CacheEvict(value = "today-task", key = "#patientId")
    public void evictTodayTask(Long patientId) {
        log.debug("清除今日待办缓存: {}", patientId);
    }
}
