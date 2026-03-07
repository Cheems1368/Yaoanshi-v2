
package com.sqlapk.medicine.module.system.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * 工单系统性能监控器
 * 提供接口性能监控、业务指标收集和性能分析功能
 *
 * @author sqlapk
 */
@Component
@Slf4j
public class HelpOrderPerformanceMonitor {

    /**
     * 接口调用次数统计
     */
    private final Map<String, LongAdder> callCounters = new ConcurrentHashMap<>();

    /**
     * 接口响应时间统计
     */
    private final Map<String, AtomicLong> totalResponseTime = new ConcurrentHashMap<>();

    /**
     * 接口最大响应时间
     */
    private final Map<String, AtomicLong> maxResponseTime = new ConcurrentHashMap<>();

    /**
     * 接口最小响应时间
     */
    private final Map<String, AtomicLong> minResponseTime = new ConcurrentHashMap<>();

    /**
     * 业务指标统计
     */
    private final Map<String, LongAdder> businessMetrics = new ConcurrentHashMap<>();

    /**
     * 记录接口调用性能
     *
     * @param methodName 方法名
     * @param responseTime 响应时间（毫秒）
     * @param success 是否成功
     */
    public void recordApiCall(String methodName, long responseTime, boolean success) {
        String key = methodName + "_" + (success ? "SUCCESS" : "FAILURE");
        
        // 更新调用次数
        callCounters.computeIfAbsent(key, k -> new LongAdder()).increment();
        
        // 更新响应时间统计
        totalResponseTime.computeIfAbsent(key, k -> new AtomicLong(0)).addAndGet(responseTime);
        
        // 更新最大响应时间
        maxResponseTime.computeIfAbsent(key, k -> new AtomicLong(0))
                .updateAndGet(current -> Math.max(current, responseTime));
        
        // 更新最小响应时间
        minResponseTime.computeIfAbsent(key, k -> new AtomicLong(Long.MAX_VALUE))
                .updateAndGet(current -> Math.min(current, responseTime));
        
        // 记录慢查询
        if (responseTime > 1000) { // 超过1秒的查询
            log.warn("慢查询检测 - 方法: {}, 响应时间: {}ms", methodName, responseTime);
        }
        
        // 记录超时查询
        if (responseTime > 5000) { // 超过5秒的查询
            log.error("超时查询检测 - 方法: {}, 响应时间: {}ms", methodName, responseTime);
        }
    }

    /**
     * 记录业务指标
     *
     * @param metricName 指标名称
     * @param value 指标值
     */
    public void recordBusinessMetric(String metricName, long value) {
        businessMetrics.computeIfAbsent(metricName, k -> new LongAdder()).add(value);
        log.debug("业务指标记录 - 指标: {}, 值: {}", metricName, value);
    }

    /**
     * 记录工单创建指标
     *
     * @param patientId 患者ID
     * @param status 工单状态
     */
    public void recordOrderCreated(Long patientId, String status) {
        recordBusinessMetric("ORDER_CREATED", 1);
        recordBusinessMetric("ORDER_CREATED_STATUS_" + status, 1);
        recordBusinessMetric("ORDER_CREATED_PATIENT_" + patientId, 1);
        
        log.info("工单创建指标 - 患者ID: {}, 状态: {}", patientId, status);
    }

    /**
     * 记录工单状态变更指标
     *
     * @param orderId 工单ID
     * @param fromStatus 原状态
     * @param toStatus 目标状态
     */
    public void recordStatusChange(Long orderId, String fromStatus, String toStatus) {
        recordBusinessMetric("STATUS_CHANGE", 1);
        recordBusinessMetric("STATUS_CHANGE_" + fromStatus + "_TO_" + toStatus, 1);
        
        log.info("工单状态变更指标 - 工单ID: {}, 从状态: {} 到状态: {}", orderId, fromStatus, toStatus);
    }

    /**
     * 记录工单分配指标
     *
     * @param orderId 工单ID
     * @param assigneeType 分配类型
     * @param assigneeId 分配人ID
     */
    public void recordOrderAssigned(Long orderId, String assigneeType, Long assigneeId) {
        recordBusinessMetric("ORDER_ASSIGNED", 1);
        recordBusinessMetric("ORDER_ASSIGNED_" + assigneeType, 1);
        recordBusinessMetric("ORDER_ASSIGNED_" + assigneeType + "_" + assigneeId, 1);
        
        log.info("工单分配指标 - 工单ID: {}, 分配类型: {}, 分配人ID: {}", orderId, assigneeType, assigneeId);
    }

    /**
     * 记录工单完成指标
     *
     * @param orderId 工单ID
     * @param completionType 完成类型（DONE/REJECTED）
     * @param processingTime 处理时间（小时）
     */
    public void recordOrderCompleted(Long orderId, String completionType, double processingTime) {
        recordBusinessMetric("ORDER_COMPLETED", 1);
        recordBusinessMetric("ORDER_COMPLETED_" + completionType, 1);
        recordBusinessMetric("PROCESSING_TIME_HOURS", (long) (processingTime * 100)); // 转换为分钟
        
        log.info("工单完成指标 - 工单ID: {}, 完成类型: {}, 处理时间: {}小时", 
                orderId, completionType, processingTime);
    }

    /**
     * 获取接口性能统计
     *
     * @param methodName 方法名
     * @return 性能统计信息
     */
    public PerformanceStats getApiPerformanceStats(String methodName) {
        String successKey = methodName + "_SUCCESS";
        String failureKey = methodName + "_FAILURE";
        
        long successCount = callCounters.getOrDefault(successKey, new LongAdder()).sum();
        long failureCount = callCounters.getOrDefault(failureKey, new LongAdder()).sum();
        long totalCount = successCount + failureCount;
        
        if (totalCount == 0) {
            return new PerformanceStats(0, 0, 0, 0, 0, 0, 0);
        }
        
        long successTotalTime = totalResponseTime.getOrDefault(successKey, new AtomicLong(0)).get();
        long failureTotalTime = totalResponseTime.getOrDefault(failureKey, new AtomicLong(0)).get();
        long totalTime = successTotalTime + failureTotalTime;
        
        long maxTime = Math.max(
                maxResponseTime.getOrDefault(successKey, new AtomicLong(0)).get(),
                maxResponseTime.getOrDefault(failureKey, new AtomicLong(0)).get()
        );
        
        long minTime = Math.min(
                minResponseTime.getOrDefault(successKey, new AtomicLong(Long.MAX_VALUE)).get(),
                minResponseTime.getOrDefault(failureKey, new AtomicLong(Long.MAX_VALUE)).get()
        );
        
        if (minTime == Long.MAX_VALUE) {
            minTime = 0;
        }
        
        double avgTime = (double) totalTime / totalCount;
        double successRate = (double) successCount / totalCount * 100;
        
        return new PerformanceStats(totalCount, successCount, failureCount, avgTime, maxTime, minTime, successRate);
    }

    /**
     * 获取业务指标统计
     *
     * @param metricName 指标名称
     * @return 指标值
     */
    public long getBusinessMetric(String metricName) {
        return businessMetrics.getOrDefault(metricName, new LongAdder()).sum();
    }

    /**
     * 获取所有业务指标
     *
     * @return 业务指标Map
     */
    public Map<String, Long> getAllBusinessMetrics() {
        Map<String, Long> result = new ConcurrentHashMap<>();
        businessMetrics.forEach((key, value) -> result.put(key, value.sum()));
        return result;
    }

    /**
     * 生成性能报告
     *
     * @return 性能报告字符串
     */
    public String generatePerformanceReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== 工单系统性能报告 ===\n");
        report.append("生成时间: ").append(LocalDateTime.now()).append("\n\n");
        
        // 接口性能统计
        report.append("接口性能统计:\n");
        report.append("-" .repeat(30)).append("\n");
        
        String[] apiMethods = {
                "createHelpOrder", "updateHelpOrder", "deleteHelpOrder", 
                "getHelpOrder", "getHelpOrderPage", "assignToPharmacist",
                "escalateToDoctor", "rejectHelpOrder", "updateHelpOrderStatus",
                "getHelpOrderListForExport"
        };
        
        for (String method : apiMethods) {
            PerformanceStats stats = getApiPerformanceStats(method);
            if (stats.getTotalCount() > 0) {
                report.append(String.format("%-25s: 调用次数=%d, 成功率=%.2f%%, 平均响应时间=%.2fms, 最大响应时间=%dms\n",
                        method, stats.getTotalCount(), stats.getSuccessRate(), 
                        stats.getAvgResponseTime(), stats.getMaxResponseTime()));
            }
        }
        
        // 业务指标统计
        report.append("\n业务指标统计:\n");
        report.append("-" .repeat(30)).append("\n");
        
        Map<String, Long> metrics = getAllBusinessMetrics();
        metrics.forEach((key, value) -> {
            report.append(String.format("%-30s: %d\n", key, value));
        });
        
        return report.toString();
    }

    /**
     * 重置性能统计
     */
    public void resetPerformanceStats() {
        callCounters.clear();
        totalResponseTime.clear();
        maxResponseTime.clear();
        minResponseTime.clear();
        businessMetrics.clear();
        log.info("性能统计已重置");
    }

    /**
     * 性能统计数据结构
     */
    public static class PerformanceStats {
        private final long totalCount;
        private final long successCount;
        private final long failureCount;
        private final double avgResponseTime;
        private final long maxResponseTime;
        private final long minResponseTime;
        private final double successRate;

        public PerformanceStats(long totalCount, long successCount, long failureCount, 
                              double avgResponseTime, long maxResponseTime, long minResponseTime, double successRate) {
            this.totalCount = totalCount;
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.avgResponseTime = avgResponseTime;
            this.maxResponseTime = maxResponseTime;
            this.minResponseTime = minResponseTime;
            this.successRate = successRate;
        }

        // Getters
        public long getTotalCount() { return totalCount; }
        public long getSuccessCount() { return successCount; }
        public long getFailureCount() { return failureCount; }
        public double getAvgResponseTime() { return avgResponseTime; }
        public long getMaxResponseTime() { return maxResponseTime; }
        public long getMinResponseTime() { return minResponseTime; }
        public double getSuccessRate() { return successRate; }
    }
}
