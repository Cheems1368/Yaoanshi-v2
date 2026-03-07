package com.sqlapk.medicine.module.medication.service.performance;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 性能指标收集器
 */
@Slf4j
@Component
public class PerformanceMetrics {

    private final Timer apiResponseTimer;
    private final Counter slowRequestCounter;
    private final Counter errorRequestCounter;
    private final Gauge activeRequestsGauge;
    private final Counter medicationPlanCreatedCounter;
    private final Counter medicationPlanCompletedCounter;
    private final Counter checkinSubmittedCounter;
    private final Counter reportGeneratedCounter;
    private final Counter notificationSentCounter;

    private final AtomicLong activeRequests = new AtomicLong(0);

    @Autowired
    public PerformanceMetrics(MeterRegistry meterRegistry) {
        // API响应时间监控
        this.apiResponseTimer = Timer.builder("medicine.api.response.time")
                .description("API response time")
                .register(meterRegistry);

        // 慢请求计数器
        this.slowRequestCounter = Counter.builder("medicine.api.slow.requests")
                .description("Number of slow requests (>500ms)")
                .register(meterRegistry);

        // 错误请求计数器
        this.errorRequestCounter = Counter.builder("medicine.api.error.requests")
                .description("Number of error requests")
                .register(meterRegistry);

        // 活跃请求数监控
        this.activeRequestsGauge = Gauge.builder("medicine.api.active.requests", activeRequests, AtomicLong::get)
                .description("Number of active requests")
                .register(meterRegistry);

        // 用药计划创建计数器
        this.medicationPlanCreatedCounter = Counter.builder("medicine.medication.plan.created")
                .description("Number of medication plans created")
                .register(meterRegistry);

        // 用药计划完成计数器
        this.medicationPlanCompletedCounter = Counter.builder("medicine.medication.plan.completed")
                .description("Number of medication plans completed")
                .register(meterRegistry);

        // 打卡提交计数器
        this.checkinSubmittedCounter = Counter.builder("medicine.checkin.submitted")
                .description("Number of checkins submitted")
                .register(meterRegistry);

        // 报告生成计数器
        this.reportGeneratedCounter = Counter.builder("medicine.report.generated")
                .description("Number of reports generated")
                .register(meterRegistry);

        // 通知发送计数器
        this.notificationSentCounter = Counter.builder("medicine.notification.sent")
                .description("Number of notifications sent")
                .register(meterRegistry);
    }

    /**
     * 记录API响应时间
     */
    public void recordApiResponseTime(Duration duration) {
        apiResponseTimer.record(duration);
        
        // 检查是否为慢请求
        if (duration.toMillis() > 500) {
            slowRequestCounter.increment();
            log.warn("Slow API request detected: {}ms", duration.toMillis());
        }
    }

    /**
     * 记录错误请求
     */
    public void recordErrorRequest() {
        errorRequestCounter.increment();
    }

    /**
     * 增加活跃请求数
     */
    public void incrementActiveRequests() {
        activeRequests.incrementAndGet();
    }

    /**
     * 减少活跃请求数
     */
    public void decrementActiveRequests() {
        activeRequests.decrementAndGet();
    }

    /**
     * 获取活跃请求数
     */
    public double getActiveRequests() {
        return activeRequests.get();
    }

    /**
     * 记录用药计划创建
     */
    public void recordMedicationPlanCreated() {
        medicationPlanCreatedCounter.increment();
    }

    /**
     * 记录用药计划完成
     */
    public void recordMedicationPlanCompleted() {
        medicationPlanCompletedCounter.increment();
    }

    /**
     * 记录打卡提交
     */
    public void recordCheckinSubmitted() {
        checkinSubmittedCounter.increment();
    }

    /**
     * 记录报告生成
     */
    public void recordReportGenerated() {
        reportGeneratedCounter.increment();
    }

    /**
     * 记录通知发送
     */
    public void recordNotificationSent() {
        notificationSentCounter.increment();
    }

}
