package com.sqlapk.medicine.framework.monitor.core.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 业务指标收集器
 * 收集用药打卡系统的关键业务指标
 */
@Slf4j
@Component
public class BusinessMetrics {

    // 工单相关指标
    private final Counter workOrderCreatedCounter;
    private final Counter workOrderCompletedCounter;
    private final Counter workOrderRejectedCounter;
    private final Timer workOrderProcessingTimer;
    private final AtomicLong pendingWorkOrders = new AtomicLong(0);

    // 患者相关指标
    private final Counter patientRegisteredCounter;
    private final Counter patientActiveCounter;
    private final AtomicLong totalPatients = new AtomicLong(0);

    // 用药相关指标
    private final Counter medicationPlanCreatedCounter;
    private final Counter medicationCheckinCounter;
    private final Counter medicationMissedCounter;
    private final AtomicLong activeMedicationPlans = new AtomicLong(0);

    // 系统性能指标
    private final Counter apiRequestCounter;
    private final Counter apiErrorCounter;
    private final Timer apiResponseTimer;

    public BusinessMetrics(MeterRegistry meterRegistry) {
        // 工单指标
        this.workOrderCreatedCounter = Counter.builder("medicine.workorder.created")
            .description("Number of work orders created")
            .register(meterRegistry);
            
        this.workOrderCompletedCounter = Counter.builder("medicine.workorder.completed")
            .description("Number of work orders completed")
            .register(meterRegistry);
            
        this.workOrderRejectedCounter = Counter.builder("medicine.workorder.rejected")
            .description("Number of work orders rejected")
            .register(meterRegistry);
            
        this.workOrderProcessingTimer = Timer.builder("medicine.workorder.processing.time")
            .description("Time taken to process work orders")
            .register(meterRegistry);

        // 患者指标
        this.patientRegisteredCounter = Counter.builder("medicine.patient.registered")
            .description("Number of patients registered")
            .register(meterRegistry);
            
        this.patientActiveCounter = Counter.builder("medicine.patient.active")
            .description("Number of active patients")
            .register(meterRegistry);

        // 用药指标
        this.medicationPlanCreatedCounter = Counter.builder("medicine.medication.plan.created")
            .description("Number of medication plans created")
            .register(meterRegistry);
            
        this.medicationCheckinCounter = Counter.builder("medicine.medication.checkin")
            .description("Number of medication check-ins")
            .register(meterRegistry);
            
        this.medicationMissedCounter = Counter.builder("medicine.medication.missed")
            .description("Number of missed medications")
            .register(meterRegistry);

        // API指标
        this.apiRequestCounter = Counter.builder("medicine.api.requests")
            .description("Number of API requests")
            .register(meterRegistry);
            
        this.apiErrorCounter = Counter.builder("medicine.api.errors")
            .description("Number of API errors")
            .register(meterRegistry);
            
        this.apiResponseTimer = Timer.builder("medicine.api.response.time")
            .description("API response time")
            .register(meterRegistry);

        // 注册Gauge指标 - 使用正确的方法签名
        Gauge.builder("medicine.workorder.pending", this, BusinessMetrics::getPendingWorkOrders)
            .description("Number of pending work orders")
            .register(meterRegistry);
            
        Gauge.builder("medicine.patient.total", this, BusinessMetrics::getTotalPatients)
            .description("Total number of patients")
            .register(meterRegistry);
            
        Gauge.builder("medicine.medication.plan.active", this, BusinessMetrics::getActiveMedicationPlans)
            .description("Number of active medication plans")
            .register(meterRegistry);
    }

    // 工单指标方法
    public void incrementWorkOrderCreated() {
        workOrderCreatedCounter.increment();
        log.debug("Work order created counter incremented");
    }

    public void incrementWorkOrderCompleted() {
        workOrderCompletedCounter.increment();
        log.debug("Work order completed counter incremented");
    }

    public void incrementWorkOrderRejected() {
        workOrderRejectedCounter.increment();
        log.debug("Work order rejected counter incremented");
    }

    public void recordWorkOrderProcessingTime(Duration duration) {
        workOrderProcessingTimer.record(duration);
        log.debug("Work order processing time recorded: {}ms", duration.toMillis());
    }

    public void setPendingWorkOrders(long count) {
        pendingWorkOrders.set(count);
        log.debug("Pending work orders count updated: {}", count);
    }

    // 患者指标方法
    public void incrementPatientRegistered() {
        patientRegisteredCounter.increment();
        log.debug("Patient registered counter incremented");
    }

    public void incrementPatientActive() {
        patientActiveCounter.increment();
        log.debug("Patient active counter incremented");
    }

    public void setTotalPatients(long count) {
        totalPatients.set(count);
        log.debug("Total patients count updated: {}", count);
    }

    // 用药指标方法
    public void incrementMedicationPlanCreated() {
        medicationPlanCreatedCounter.increment();
        log.debug("Medication plan created counter incremented");
    }

    public void incrementMedicationCheckin() {
        medicationCheckinCounter.increment();
        log.debug("Medication checkin counter incremented");
    }

    public void incrementMedicationMissed() {
        medicationMissedCounter.increment();
        log.debug("Medication missed counter incremented");
    }

    public void setActiveMedicationPlans(long count) {
        activeMedicationPlans.set(count);
        log.debug("Active medication plans count updated: {}", count);
    }

    // API指标方法
    public void incrementApiRequest() {
        apiRequestCounter.increment();
    }

    public void incrementApiError() {
        apiErrorCounter.increment();
    }

    public void recordApiResponseTime(Duration duration) {
        apiResponseTimer.record(duration);
    }

    // Gauge指标获取方法
    private double getPendingWorkOrders() {
        return pendingWorkOrders.get();
    }

    private double getTotalPatients() {
        return totalPatients.get();
    }

    private double getActiveMedicationPlans() {
        return activeMedicationPlans.get();
    }
}