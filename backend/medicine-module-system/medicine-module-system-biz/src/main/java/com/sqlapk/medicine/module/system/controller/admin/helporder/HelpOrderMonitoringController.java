package com.sqlapk.medicine.module.system.controller.admin.helporder;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.module.system.monitoring.HelpOrderPerformanceMonitor;
import com.sqlapk.medicine.module.system.service.helporder.HelpOrderErrorHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

import static com.sqlapk.medicine.framework.common.pojo.CommonResult.success;

/**
 * 工单系统监控控制器
 * 提供性能监控、错误统计和系统健康检查功能
 *
 * @author sqlapk
 */
@Tag(name = "管理后台 - 工单监控")
@RestController
@RequestMapping("/admin-api/help-orders/monitoring")
public class HelpOrderMonitoringController {

    @Resource
    private HelpOrderPerformanceMonitor performanceMonitor;

    @Resource
    private HelpOrderErrorHandler errorHandler;

    @GetMapping("/performance-stats")
    @Operation(summary = "获取接口性能统计")
    @PreAuthorize("@ss.hasPermission('system:help-order:monitor')")
    public CommonResult<Map<String, Object>> getPerformanceStats(@RequestParam String methodName) {
        HelpOrderPerformanceMonitor.PerformanceStats stats = performanceMonitor.getApiPerformanceStats(methodName);
        
        Map<String, Object> result = Map.of(
                "methodName", methodName,
                "totalCount", stats.getTotalCount(),
                "successCount", stats.getSuccessCount(),
                "failureCount", stats.getFailureCount(),
                "avgResponseTime", stats.getAvgResponseTime(),
                "maxResponseTime", stats.getMaxResponseTime(),
                "minResponseTime", stats.getMinResponseTime(),
                "successRate", stats.getSuccessRate()
        );
        
        return success(result);
    }

    @GetMapping("/business-metrics")
    @Operation(summary = "获取业务指标统计")
    @PreAuthorize("@ss.hasPermission('system:help-order:monitor')")
    public CommonResult<Map<String, Long>> getBusinessMetrics() {
        Map<String, Long> metrics = performanceMonitor.getAllBusinessMetrics();
        return success(metrics);
    }

    @GetMapping("/error-statistics")
    @Operation(summary = "获取错误统计信息")
    @PreAuthorize("@ss.hasPermission('system:help-order:monitor')")
    public CommonResult<Map<String, Integer>> getErrorStatistics() {
        Map<String, Integer> statistics = errorHandler.getErrorStatistics();
        return success(statistics);
    }

    @GetMapping("/performance-report")
    @Operation(summary = "生成性能报告")
    @PreAuthorize("@ss.hasPermission('system:help-order:monitor')")
    public CommonResult<String> generatePerformanceReport() {
        String report = performanceMonitor.generatePerformanceReport();
        return success(report);
    }

    @GetMapping("/error-recovery-suggestion")
    @Operation(summary = "获取错误恢复建议")
    @PreAuthorize("@ss.hasPermission('system:help-order:monitor')")
    public CommonResult<String> getErrorRecoverySuggestion(@RequestParam String errorType) {
        String suggestion = errorHandler.getErrorRecoverySuggestion(errorType);
        return success(suggestion);
    }

    @PostMapping("/reset-performance-stats")
    @Operation(summary = "重置性能统计")
    @PreAuthorize("@ss.hasPermission('system:help-order:monitor')")
    public CommonResult<Boolean> resetPerformanceStats() {
        performanceMonitor.resetPerformanceStats();
        return success(true);
    }

    @PostMapping("/reset-error-statistics")
    @Operation(summary = "重置错误统计")
    @PreAuthorize("@ss.hasPermission('system:help-order:monitor')")
    public CommonResult<Boolean> resetErrorStatistics() {
        errorHandler.resetErrorStatistics();
        return success(true);
    }

    @GetMapping("/health-check")
    @Operation(summary = "系统健康检查")
    @PreAuthorize("@ss.hasPermission('system:help-order:monitor')")
    public CommonResult<Map<String, Object>> healthCheck() {
        Map<String, Long> businessMetrics = performanceMonitor.getAllBusinessMetrics();
        Map<String, Integer> errorStats = errorHandler.getErrorStatistics();
        
        // 计算系统健康度
        long totalOrders = businessMetrics.getOrDefault("ORDER_CREATED", 0L);
        long completedOrders = businessMetrics.getOrDefault("ORDER_COMPLETED", 0L);
        long errorCount = errorStats.values().stream().mapToInt(Integer::intValue).sum();
        
        double completionRate = totalOrders > 0 ? (double) completedOrders / totalOrders * 100 : 0;
        double errorRate = totalOrders > 0 ? (double) errorCount / totalOrders * 100 : 0;
        
        String healthStatus;
        if (completionRate >= 90 && errorRate <= 5) {
            healthStatus = "HEALTHY";
        } else if (completionRate >= 80 && errorRate <= 10) {
            healthStatus = "WARNING";
        } else {
            healthStatus = "CRITICAL";
        }
        
        Map<String, Object> healthInfo = Map.of(
                "status", healthStatus,
                "totalOrders", totalOrders,
                "completedOrders", completedOrders,
                "completionRate", completionRate,
                "errorCount", errorCount,
                "errorRate", errorRate,
                "timestamp", System.currentTimeMillis()
        );
        
        return success(healthInfo);
    }
}
