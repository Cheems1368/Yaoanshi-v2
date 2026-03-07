package com.sqlapk.medicine.module.system.config;

import com.sqlapk.medicine.module.system.monitoring.HelpOrderMetricsCollector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;

/**
 * 工单监控配置
 * 配置监控相关的定时任务和指标收集
 *
 * @author sqlapk
 */
@Configuration
@EnableScheduling
@Slf4j
public class HelpOrderMonitoringConfig {

    @Resource
    private HelpOrderMetricsCollector metricsCollector;

    /**
     * 定时输出业务指标摘要
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void logBusinessMetrics() {
        try {
            HelpOrderMetricsCollector.BusinessMetricsSummary summary = metricsCollector.getBusinessMetricsSummary();
            
            log.info("=== 工单业务指标摘要 ===");
            log.info("总创建工单数: {}", summary.getTotalOrdersCreated());
            log.info("总完成工单数: {}", summary.getTotalOrdersCompleted());
            log.info("总驳回工单数: {}", summary.getTotalOrdersRejected());
            log.info("总错误数: {}", summary.getTotalErrors());
            log.info("完成率: {:.2f}%", summary.getCompletionRate());
            log.info("错误率: {:.2f}%", summary.getErrorRate());
            log.info("========================");
            
        } catch (Exception e) {
            log.error("输出业务指标摘要失败: {}", e.getMessage());
        }
    }

    /**
     * 定时输出性能指标摘要
     * 每10分钟执行一次
     */
    @Scheduled(fixedRate = 600000) // 10分钟
    public void logPerformanceMetrics() {
        try {
            HelpOrderMetricsCollector.PerformanceMetricsSummary summary = metricsCollector.getPerformanceMetricsSummary();
            
            log.info("=== 工单性能指标摘要 ===");
            summary.getMetrics().forEach(metric -> {
                log.info("操作: {}, 调用次数: {}, 总耗时: {}ms, 平均耗时: {}ms, 最大耗时: {}ms",
                    metric.getOperationName(),
                    metric.getTotalCount(),
                    metric.getTotalTime(),
                    metric.getAverageTime(),
                    metric.getMaxTime()
                );
            });
            log.info("========================");
            
        } catch (Exception e) {
            log.error("输出性能指标摘要失败: {}", e.getMessage());
        }
    }

    /**
     * 定时输出状态分布
     * 每15分钟执行一次
     */
    @Scheduled(fixedRate = 900000) // 15分钟
    public void logStatusDistribution() {
        try {
            HelpOrderMetricsCollector.StatusDistribution distribution = metricsCollector.getStatusDistribution();
            
            log.info("=== 工单状态分布 ===");
            distribution.getStatusCounts().forEach((status, count) -> {
                log.info("状态: {}, 数量: {}", status.getCode(), count);
            });
            log.info("==================");
            
        } catch (Exception e) {
            log.error("输出状态分布失败: {}", e.getMessage());
        }
    }

    /**
     * 定时检查系统健康状态
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60000) // 1分钟
    public void checkSystemHealth() {
        try {
            HelpOrderMetricsCollector.BusinessMetricsSummary summary = metricsCollector.getBusinessMetricsSummary();
            
            // 检查错误率
            if (summary.getErrorRate() > 10.0) { // 错误率超过10%
                log.warn("系统健康检查警告: 错误率过高 - {:.2f}%", summary.getErrorRate());
            }
            
            // 检查完成率
            if (summary.getCompletionRate() < 80.0) { // 完成率低于80%
                log.warn("系统健康检查警告: 完成率过低 - {:.2f}%", summary.getCompletionRate());
            }
            
            log.debug("系统健康检查完成 - 错误率: {:.2f}%, 完成率: {:.2f}%", 
                summary.getErrorRate(), summary.getCompletionRate());
            
        } catch (Exception e) {
            log.error("系统健康检查失败: {}", e.getMessage());
        }
    }

    /**
     * 定时重置指标
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void resetDailyMetrics() {
        try {
            log.info("开始重置每日指标");
            metricsCollector.resetMetrics();
            log.info("每日指标重置完成");
            
        } catch (Exception e) {
            log.error("重置每日指标失败: {}", e.getMessage());
        }
    }

    /**
     * 获取系统监控信息
     */
    public SystemMonitoringInfo getSystemMonitoringInfo() {
        SystemMonitoringInfo info = new SystemMonitoringInfo();
        
        try {
            HelpOrderMetricsCollector.BusinessMetricsSummary businessSummary = metricsCollector.getBusinessMetricsSummary();
            HelpOrderMetricsCollector.PerformanceMetricsSummary performanceSummary = metricsCollector.getPerformanceMetricsSummary();
            HelpOrderMetricsCollector.StatusDistribution statusDistribution = metricsCollector.getStatusDistribution();
            
            info.setBusinessMetrics(businessSummary);
            info.setPerformanceMetrics(performanceSummary);
            info.setStatusDistribution(statusDistribution);
            info.setTimestamp(System.currentTimeMillis());
            
        } catch (Exception e) {
            log.error("获取系统监控信息失败: {}", e.getMessage());
            info.setError(e.getMessage());
        }
        
        return info;
    }

    /**
     * 系统监控信息
     */
    public static class SystemMonitoringInfo {
        private HelpOrderMetricsCollector.BusinessMetricsSummary businessMetrics;
        private HelpOrderMetricsCollector.PerformanceMetricsSummary performanceMetrics;
        private HelpOrderMetricsCollector.StatusDistribution statusDistribution;
        private long timestamp;
        private String error;

        // Getters and Setters
        public HelpOrderMetricsCollector.BusinessMetricsSummary getBusinessMetrics() { return businessMetrics; }
        public void setBusinessMetrics(HelpOrderMetricsCollector.BusinessMetricsSummary businessMetrics) { this.businessMetrics = businessMetrics; }
        
        public HelpOrderMetricsCollector.PerformanceMetricsSummary getPerformanceMetrics() { return performanceMetrics; }
        public void setPerformanceMetrics(HelpOrderMetricsCollector.PerformanceMetricsSummary performanceMetrics) { this.performanceMetrics = performanceMetrics; }
        
        public HelpOrderMetricsCollector.StatusDistribution getStatusDistribution() { return statusDistribution; }
        public void setStatusDistribution(HelpOrderMetricsCollector.StatusDistribution statusDistribution) { this.statusDistribution = statusDistribution; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}
