package com.sqlapk.medicine.module.medication.service.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务
 */
@Service
@Slf4j
public class CacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 缓存键前缀
    private static final String PATIENT_PROFILE_PREFIX = "patient:profile:";
    private static final String MEDICATION_PLAN_PREFIX = "medication:plan:";
    private static final String TODAY_TASK_PREFIX = "today:task:";
    private static final String ADHERENCE_STATS_PREFIX = "adherence:stats:";
    private static final String REPORT_PREFIX = "report:";

    // 默认过期时间
    private static final Duration DEFAULT_TTL = Duration.ofHours(1);
    private static final Duration PATIENT_PROFILE_TTL = Duration.ofMinutes(30);
    private static final Duration MEDICATION_PLAN_TTL = Duration.ofMinutes(15);
    private static final Duration TODAY_TASK_TTL = Duration.ofMinutes(5);
    private static final Duration ADHERENCE_STATS_TTL = Duration.ofHours(2);
    private static final Duration REPORT_TTL = Duration.ofHours(6);

    /**
     * 设置缓存
     */
    public void set(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
            log.debug("Cache set: key={}, ttl={}", key, ttl);
        } catch (Exception e) {
            log.error("Failed to set cache: key={}, error={}", key, e.getMessage());
        }
    }

    /**
     * 设置缓存（使用默认TTL）
     */
    public void set(String key, Object value) {
        set(key, value, DEFAULT_TTL);
    }

    /**
     * 获取缓存
     */
    public Object get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.debug("Cache hit: key={}", key);
            } else {
                log.debug("Cache miss: key={}", key);
            }
            return value;
        } catch (Exception e) {
            log.error("Failed to get cache: key={}, error={}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 删除缓存
     */
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("Cache deleted: key={}", key);
        } catch (Exception e) {
            log.error("Failed to delete cache: key={}, error={}", key, e.getMessage());
        }
    }

    /**
     * 检查缓存是否存在
     */
    public boolean exists(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("Failed to check cache existence: key={}, error={}", key, e.getMessage());
            return false;
        }
    }

    /**
     * 设置过期时间
     */
    public void expire(String key, Duration ttl) {
        try {
            redisTemplate.expire(key, ttl);
            log.debug("Cache expire set: key={}, ttl={}", key, ttl);
        } catch (Exception e) {
            log.error("Failed to set cache expire: key={}, error={}", key, e.getMessage());
        }
    }

    /**
     * 获取剩余过期时间
     */
    public Duration getExpire(String key) {
        try {
            Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (expire != null && expire > 0) {
                return Duration.ofSeconds(expire);
            }
            return Duration.ZERO;
        } catch (Exception e) {
            log.error("Failed to get cache expire: key={}, error={}", key, e.getMessage());
            return Duration.ZERO;
        }
    }

    // 患者档案缓存方法

    /**
     * 缓存患者档案
     */
    public void cachePatientProfile(Long patientId, Object profile) {
        String key = PATIENT_PROFILE_PREFIX + patientId;
        set(key, profile, PATIENT_PROFILE_TTL);
    }

    /**
     * 获取患者档案缓存
     */
    public Object getPatientProfile(Long patientId) {
        String key = PATIENT_PROFILE_PREFIX + patientId;
        return get(key);
    }

    /**
     * 删除患者档案缓存
     */
    public void evictPatientProfile(Long patientId) {
        String key = PATIENT_PROFILE_PREFIX + patientId;
        delete(key);
    }

    // 用药计划缓存方法

    /**
     * 缓存用药计划
     */
    public void cacheMedicationPlan(Long planId, Object plan) {
        String key = MEDICATION_PLAN_PREFIX + planId;
        set(key, plan, MEDICATION_PLAN_TTL);
    }

    /**
     * 获取用药计划缓存
     */
    public Object getMedicationPlan(Long planId) {
        String key = MEDICATION_PLAN_PREFIX + planId;
        return get(key);
    }

    /**
     * 删除用药计划缓存
     */
    public void evictMedicationPlan(Long planId) {
        String key = MEDICATION_PLAN_PREFIX + planId;
        delete(key);
    }

    // 今日待办缓存方法

    /**
     * 缓存今日待办
     */
    public void cacheTodayTask(Long patientId, Object tasks) {
        String key = TODAY_TASK_PREFIX + patientId;
        set(key, tasks, TODAY_TASK_TTL);
    }

    /**
     * 获取今日待办缓存
     */
    public Object getTodayTask(Long patientId) {
        String key = TODAY_TASK_PREFIX + patientId;
        return get(key);
    }

    /**
     * 删除今日待办缓存
     */
    public void evictTodayTask(Long patientId) {
        String key = TODAY_TASK_PREFIX + patientId;
        delete(key);
    }

    // 依从统计缓存方法

    /**
     * 缓存依从统计
     */
    public void cacheAdherenceStats(Long patientId, Object stats) {
        String key = ADHERENCE_STATS_PREFIX + patientId;
        set(key, stats, ADHERENCE_STATS_TTL);
    }

    /**
     * 获取依从统计缓存
     */
    public Object getAdherenceStats(Long patientId) {
        String key = ADHERENCE_STATS_PREFIX + patientId;
        return get(key);
    }

    /**
     * 删除依从统计缓存
     */
    public void evictAdherenceStats(Long patientId) {
        String key = ADHERENCE_STATS_PREFIX + patientId;
        delete(key);
    }

    // 报告缓存方法

    /**
     * 缓存报告
     */
    public void cacheReport(Long reportId, Object report) {
        String key = REPORT_PREFIX + reportId;
        set(key, report, REPORT_TTL);
    }

    /**
     * 获取报告缓存
     */
    public Object getReport(Long reportId) {
        String key = REPORT_PREFIX + reportId;
        return get(key);
    }

    /**
     * 删除报告缓存
     */
    public void evictReport(Long reportId) {
        String key = REPORT_PREFIX + reportId;
        delete(key);
    }

    /**
     * 批量删除缓存
     */
    public void evictByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Cache evicted by pattern: {}", pattern);
            }
        } catch (Exception e) {
            log.error("Failed to evict cache by pattern: pattern={}, error={}", pattern, e.getMessage());
        }
    }

    /**
     * 清空所有缓存
     */
    public void clearAll() {
        try {
            var connectionFactory = redisTemplate.getConnectionFactory();
            if (connectionFactory != null) {
                var connection = connectionFactory.getConnection();
                connection.flushDb();
                log.info("All cache cleared");
            }
        } catch (Exception e) {
            log.error("Failed to clear all cache: {}", e.getMessage());
        }
    }

}
