package com.sqlapk.medicine.module.system.monitoring;

import com.sqlapk.medicine.module.system.enums.HelpOrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * 工单指标收集器
 * 收集和记录工单系统的各种业务指标和性能指标
 *
 * @author sqlapk
 */
@Component
@Slf4j
public class HelpOrderMetricsCollector {

    // 业务指标
    private final LongAdder totalOrdersCreated = new LongAdder();
    private final LongAdder totalOrdersCompleted = new LongAdder();
    private final LongAdder totalOrdersRejected = new LongAdder();
    private final ConcurrentHashMap<HelpOrderStatus, LongAdder> statusCounts = new ConcurrentHashMap<>();
    
    // 性能指标
    private final ConcurrentHashMap<String, AtomicLong> operationCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> operationTotalTime = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> operationMaxTime = new ConcurrentHashMap<>();
    
    // 错误指标
    private final LongAdder totalErrors = new LongAdder();
    private final ConcurrentHashMap<String, LongAdder> errorCounts = new ConcurrentHashMap<>();
    
    // 用户指标
    private final ConcurrentHashMap<Long, LongAdder> userOrderCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, LongAdder> userErrorCounts = new ConcurrentHashMap<>();

    /**
     * 记录工单创建
     */
    public void recordOrderCreated(Long patientId, HelpOrderStatus status) {
        totalOrdersCreated.increment();
        statusCounts.computeIfAbsent(status, k -> new LongAdder()).increment();
        userOrderCounts.computeIfAbsent(patientId, k -> new LongAdder()).increment();
        
        log.debug("记录工单创建 - 患者ID: {}, 状态: {}, 总数: {}", 
            patientId, status, totalOrdersCreated.sum());
    }

    /**
     * 记录工单完成
     */
    public void recordOrderCompleted(Long orderId, HelpOrderStatus fromStatus, HelpOrderStatus toStatus) {
        totalOrdersCompleted.increment();
        statusCounts.computeIfAbsent(toStatus, k -> new LongAdder()).increment();
        
        log.debug("记录工单完成 - 工单ID: {}, 从 {} 到 {}, 完成总数: {}", 
            orderId, fromStatus, toStatus, totalOrdersCompleted.sum());
    }

    /**
     * 记录工单驳回
     */
    public void recordOrderRejected(Long orderId, String reason) {
        totalOrdersRejected.increment();
        statusCounts.computeIfAbsent(HelpOrderStatus.REJECTED, k -> new LongAdder()).increment();
        
        log.debug("记录工单驳回 - 工单ID: {}, 原因: {}, 驳回总数: {}", 
            orderId, reason, totalOrdersRejected.sum());
    }

    /**
     * 记录状态转换
     */
    public void recordStatusTransition(Long orderId, HelpOrderStatus fromStatus, HelpOrderStatus toStatus, long durationMs) {
        String operationKey = "status_transition_" + fromStatus + "_to_" + toStatus;
        recordOperation(operationKey, durationMs);
        
        log.debug("记录状态转换 - 工单ID: {}, 从 {} 到 {}, 耗时: {}ms", 
            orderId, fromStatus, toStatus, durationMs);
    }

    /**
     * 记录操作性能
     */
    public void recordOperation(String operationName, long durationMs) {
        operationCounts.computeIfAbsent(operationName, k -> new AtomicLong()).incrementAndGet();
        operationTotalTime.computeIfAbsent(operationName, k -> new AtomicLong()).addAndGet(durationMs);
        
        // 更新最大耗时
        operationMaxTime.computeIfAbsent(operationName, k -> new AtomicLong())
            .updateAndGet(current -> Math.max(current, durationMs));
        
        log.debug("记录操作性能 - 操作: {}, 耗时: {}ms", operationName, durationMs);
    }

    /**
     * 记录错误
     */
    public void recordError(String errorType, String errorMessage, Long userId) {
        totalErrors.increment();
        errorCounts.computeIfAbsent(errorType, k -> new LongAdder()).increment();
        
        if (userId != null) {
            userErrorCounts.computeIfAbsent(userId, k -> new LongAdder()).increment();
        }
        
        log.warn("记录错误 - 类型: {}, 消息: {}, 用户ID: {}, 错误总数: {}", 
            errorType, errorMessage, userId, totalErrors.sum());
    }

    /**
     * 记录分配操作
     */
    public void recordAssignment(Long orderId, Long assigneeId, String assignType, long durationMs) {
        String operationKey = "assign_" + assignType.toLowerCase();
        recordOperation(operationKey, durationMs);
        
        log.debug("记录分配操作 - 工单ID: {}, 分配者ID: {}, 类型: {}, 耗时: {}ms", 
            orderId, assigneeId, assignType, durationMs);
    }

    /**
     * 记录导出操作
     */
    public void recordExport(Long userId, int recordCount, long durationMs) {
        String operationKey = "export_data";
        recordOperation(operationKey, durationMs);
        
        log.info("记录导出操作 - 用户ID: {}, 记录数: {}, 耗时: {}ms", 
            userId, recordCount, durationMs);
    }

    /**
     * 记录查询操作
     */
    public void recordQuery(String queryType, int resultCount, long durationMs) {
        String operationKey = "query_" + queryType;
        recordOperation(operationKey, durationMs);
        
        log.debug("记录查询操作 - 类型: {}, 结果数: {}, 耗时: {}ms", 
            queryType, resultCount, durationMs);
    }

    /**
     * 获取业务指标摘要
     */
    public BusinessMetricsSummary getBusinessMetricsSummary() {
        BusinessMetricsSummary summary = new BusinessMetricsSummary();
        
        summary.setTotalOrdersCreated(totalOrdersCreated.sum());
        summary.setTotalOrdersCompleted(totalOrdersCompleted.sum());
        summary.setTotalOrdersRejected(totalOrdersRejected.sum());
        summary.setTotalErrors(totalErrors.sum());
        
        // 计算完成率
        long totalProcessed = totalOrdersCompleted.sum() + totalOrdersRejected.sum();
        if (totalProcessed > 0) {
            summary.setCompletionRate((double) totalOrdersCompleted.sum() / totalProcessed * 100);
        }
        
        // 计算错误率
        long totalOperations = totalOrdersCreated.sum();
        if (totalOperations > 0) {
            summary.setErrorRate((double) totalErrors.sum() / totalOperations * 100);
        }
        
        log.debug("生成业务指标摘要 - 创建: {}, 完成: {}, 驳回: {}, 错误: {}", 
            summary.getTotalOrdersCreated(), summary.getTotalOrdersCompleted(), 
            summary.getTotalOrdersRejected(), summary.getTotalErrors());
        
        return summary;
    }

    /**
     * 获取性能指标摘要
     */
    public PerformanceMetricsSummary getPerformanceMetricsSummary() {
        PerformanceMetricsSummary summary = new PerformanceMetricsSummary();
        
        operationCounts.forEach((operation, count) -> {
            long totalTime = operationTotalTime.getOrDefault(operation, new AtomicLong()).get();
            long maxTime = operationMaxTime.getOrDefault(operation, new AtomicLong()).get();
            
            PerformanceMetric metric = new PerformanceMetric();
            metric.setOperationName(operation);
            metric.setTotalCount(count.get());
            metric.setTotalTime(totalTime);
            metric.setAverageTime(totalTime / Math.max(count.get(), 1));
            metric.setMaxTime(maxTime);
            
            summary.addMetric(metric);
        });
        
        log.debug("生成性能指标摘要 - 操作数: {}", summary.getMetrics().size());
        return summary;
    }

    /**
     * 获取状态分布
     */
    public StatusDistribution getStatusDistribution() {
        StatusDistribution distribution = new StatusDistribution();
        
        statusCounts.forEach((status, count) -> {
            distribution.addStatusCount(status, count.sum());
        });
        
        log.debug("生成状态分布 - 状态数: {}", distribution.getStatusCounts().size());
        return distribution;
    }

    /**
     * 获取用户指标
     */
    public UserMetrics getUserMetrics(Long userId) {
        UserMetrics metrics = new UserMetrics();
        metrics.setUserId(userId);
        metrics.setOrderCount(userOrderCounts.getOrDefault(userId, new LongAdder()).sum());
        metrics.setErrorCount(userErrorCounts.getOrDefault(userId, new LongAdder()).sum());
        
        log.debug("生成用户指标 - 用户ID: {}, 工单数: {}, 错误数: {}", 
            userId, metrics.getOrderCount(), metrics.getErrorCount());
        
        return metrics;
    }

    /**
     * 重置所有指标
     */
    public void resetMetrics() {
        totalOrdersCreated.reset();
        totalOrdersCompleted.reset();
        totalOrdersRejected.reset();
        totalErrors.reset();
        
        statusCounts.clear();
        operationCounts.clear();
        operationTotalTime.clear();
        operationMaxTime.clear();
        errorCounts.clear();
        userOrderCounts.clear();
        userErrorCounts.clear();
        
        log.info("重置所有指标");
    }

    /**
     * 业务指标摘要
     */
    public static class BusinessMetricsSummary {
        private long totalOrdersCreated;
        private long totalOrdersCompleted;
        private long totalOrdersRejected;
        private long totalErrors;
        private double completionRate;
        private double errorRate;

        // Getters and Setters
        public long getTotalOrdersCreated() { return totalOrdersCreated; }
        public void setTotalOrdersCreated(long totalOrdersCreated) { this.totalOrdersCreated = totalOrdersCreated; }
        
        public long getTotalOrdersCompleted() { return totalOrdersCompleted; }
        public void setTotalOrdersCompleted(long totalOrdersCompleted) { this.totalOrdersCompleted = totalOrdersCompleted; }
        
        public long getTotalOrdersRejected() { return totalOrdersRejected; }
        public void setTotalOrdersRejected(long totalOrdersRejected) { this.totalOrdersRejected = totalOrdersRejected; }
        
        public long getTotalErrors() { return totalErrors; }
        public void setTotalErrors(long totalErrors) { this.totalErrors = totalErrors; }
        
        public double getCompletionRate() { return completionRate; }
        public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }
        
        public double getErrorRate() { return errorRate; }
        public void setErrorRate(double errorRate) { this.errorRate = errorRate; }
    }

    /**
     * 性能指标摘要
     */
    public static class PerformanceMetricsSummary {
        private java.util.List<PerformanceMetric> metrics = new java.util.ArrayList<>();

        public void addMetric(PerformanceMetric metric) {
            metrics.add(metric);
        }

        public java.util.List<PerformanceMetric> getMetrics() { return metrics; }
    }

    /**
     * 性能指标
     */
    public static class PerformanceMetric {
        private String operationName;
        private long totalCount;
        private long totalTime;
        private long averageTime;
        private long maxTime;

        // Getters and Setters
        public String getOperationName() { return operationName; }
        public void setOperationName(String operationName) { this.operationName = operationName; }
        
        public long getTotalCount() { return totalCount; }
        public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
        
        public long getTotalTime() { return totalTime; }
        public void setTotalTime(long totalTime) { this.totalTime = totalTime; }
        
        public long getAverageTime() { return averageTime; }
        public void setAverageTime(long averageTime) { this.averageTime = averageTime; }
        
        public long getMaxTime() { return maxTime; }
        public void setMaxTime(long maxTime) { this.maxTime = maxTime; }
    }

    /**
     * 状态分布
     */
    public static class StatusDistribution {
        private java.util.Map<HelpOrderStatus, Long> statusCounts = new java.util.HashMap<>();

        public void addStatusCount(HelpOrderStatus status, long count) {
            statusCounts.put(status, count);
        }

        public java.util.Map<HelpOrderStatus, Long> getStatusCounts() { return statusCounts; }
    }

    /**
     * 用户指标
     */
    public static class UserMetrics {
        private Long userId;
        private long orderCount;
        private long errorCount;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public long getOrderCount() { return orderCount; }
        public void setOrderCount(long orderCount) { this.orderCount = orderCount; }
        
        public long getErrorCount() { return errorCount; }
        public void setErrorCount(long errorCount) { this.errorCount = errorCount; }
    }
}
