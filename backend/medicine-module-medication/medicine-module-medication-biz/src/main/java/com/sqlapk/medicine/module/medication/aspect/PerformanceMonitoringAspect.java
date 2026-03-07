package com.sqlapk.medicine.module.medication.aspect;

import com.sqlapk.medicine.module.medication.service.performance.PerformanceMetrics;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * 性能监控切面
 */
@Slf4j
@Aspect
@Component
public class PerformanceMonitoringAspect {

    @Autowired
    private PerformanceMetrics performanceMetrics;

    /**
     * 监控所有Controller方法的性能
     */
    @Around("execution(* com.sqlapk.medicine.module.medication.controller..*.*(..))")
    public Object monitorControllerPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        return monitorPerformance(joinPoint, "controller");
    }

    /**
     * 监控所有Service方法的性能
     */
    @Around("execution(* com.sqlapk.medicine.module.medication.service..*.*(..)) && !within(com.sqlapk.medicine.module.medication.service.performance..*)")
    public Object monitorServicePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        return monitorPerformance(joinPoint, "service");
    }

    /**
     * 通用性能监控方法
     */
    private Object monitorPerformance(ProceedingJoinPoint joinPoint, String layer) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String fullMethodName = className + "." + methodName;

        // 增加活跃请求数?
        performanceMetrics.incrementActiveRequests();

        Instant startTime = Instant.now();
        Object result = null;
        boolean hasError = false;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            hasError = true;
            performanceMetrics.recordErrorRequest();
            log.error("Error in {} method: {}", fullMethodName, e.getMessage());
            throw e;
        } finally {
            // 减少活跃请求数?
            performanceMetrics.decrementActiveRequests();

            // 计算执行时间
            Duration duration = Duration.between(startTime, Instant.now());
            
            // 记录性能指标
            performanceMetrics.recordApiResponseTime(duration);

            // 记录慢方法?
            if (duration.toMillis() > 500) {
                log.warn("Slow {} method detected: {} - {}ms", layer, fullMethodName, duration.toMillis());
            }

            // 记录方法执行日志
            if (log.isDebugEnabled()) {
                log.debug("{} method {} executed in {}ms", 
                         layer, fullMethodName, duration.toMillis());
            }
        }
    }

}
