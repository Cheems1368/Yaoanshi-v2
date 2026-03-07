package com.sqlapk.medicine.module.medication.service.circuitbreaker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 降级开关服务
 */
@Slf4j
@Service
public class CircuitBreakerService {

    @Value("${circuit-breaker.enabled:true}")
    private boolean circuitBreakerEnabled;

    @Value("${circuit-breaker.failure-rate-threshold:50}")
    private int failureRateThreshold;

    @Value("${circuit-breaker.slow-call-rate-threshold:50}")
    private int slowCallRateThreshold;

    @Value("${circuit-breaker.slow-call-duration-threshold:2000}")
    private long slowCallDurationThreshold;

    @Value("${circuit-breaker.wait-duration-in-open-state:10000}")
    private long waitDurationInOpenState;

    @Value("${circuit-breaker.permitted-number-of-calls-in-half-open-state:3}")
    private int permittedNumberOfCallsInHalfOpenState;

    @Value("${circuit-breaker.sliding-window-size:10}")
    private int slidingWindowSize;

    @Value("${circuit-breaker.minimum-number-of-calls:5}")
    private int minimumNumberOfCalls;

    // 熔断器状态存储
    private final ConcurrentHashMap<String, CircuitBreakerState> circuitBreakers = new ConcurrentHashMap<>();

    /**
     * 熔断器状态
     */
    public enum State {
        CLOSED,    // 关闭状态，正常调用
        OPEN,      // 开启状态，拒绝调用
        HALF_OPEN  // 半开状态，允许少量调用
    }

    /**
     * 熔断器状态信息
     */
    private static class CircuitBreakerState {
        private volatile State state = State.CLOSED;
        private final AtomicInteger successCount = new AtomicInteger(0);
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private final AtomicInteger slowCallCount = new AtomicInteger(0);
        private final AtomicLong totalCalls = new AtomicLong(0);
        private volatile LocalDateTime lastFailureTime;
        private volatile LocalDateTime stateTransitionTime;
        private final AtomicInteger halfOpenCalls = new AtomicInteger(0);
    }

    /**
     * 检查是否可以执行调用
     */
    public boolean canExecute(String serviceName) {
        if (!circuitBreakerEnabled) {
            return true;
        }

        CircuitBreakerState state = circuitBreakers.computeIfAbsent(serviceName, k -> new CircuitBreakerState());

        switch (state.state) {
            case CLOSED:
                return true;
            case OPEN:
                // 检查是否可以转换为半开状态
                if (shouldAttemptReset(state)) {
                    transitionToHalfOpen(state);
                    return true;
                }
                return false;
            case HALF_OPEN:
                // 半开状态允许少量调用
                if (state.halfOpenCalls.get() < permittedNumberOfCallsInHalfOpenState) {
                    state.halfOpenCalls.incrementAndGet();
                    return true;
                }
                return false;
            default:
                return true;
        }
    }

    /**
     * 记录调用成功
     */
    public void recordSuccess(String serviceName) {
        if (!circuitBreakerEnabled) {
            return;
        }

        CircuitBreakerState state = circuitBreakers.get(serviceName);
        if (state == null) {
            return;
        }

        state.successCount.incrementAndGet();
        state.totalCalls.incrementAndGet();

        // 如果是半开状态，成功调用后转换为关闭状态
        if (state.state == State.HALF_OPEN) {
            transitionToClosed(state);
            log.info("Circuit breaker for {} transitioned to CLOSED after successful call", serviceName);
        }
    }

    /**
     * 记录调用失败
     */
    public void recordFailure(String serviceName) {
        if (!circuitBreakerEnabled) {
            return;
        }

        CircuitBreakerState state = circuitBreakers.computeIfAbsent(serviceName, k -> new CircuitBreakerState());
        state.failureCount.incrementAndGet();
        state.totalCalls.incrementAndGet();
        state.lastFailureTime = LocalDateTime.now();

        // 检查是否需要转换为开启状态
        if (shouldOpenCircuitBreaker(state)) {
            transitionToOpen(state);
            log.warn("Circuit breaker for {} opened due to high failure rate", serviceName);
        }
    }

    /**
     * 记录慢调用
     */
    public void recordSlowCall(String serviceName, long duration) {
        if (!circuitBreakerEnabled) {
            return;
        }

        CircuitBreakerState state = circuitBreakers.computeIfAbsent(serviceName, k -> new CircuitBreakerState());
        
        if (duration > slowCallDurationThreshold) {
            state.slowCallCount.incrementAndGet();
            state.totalCalls.incrementAndGet();

            // 检查是否需要转换为开启状态
            if (shouldOpenCircuitBreaker(state)) {
                transitionToOpen(state);
                log.warn("Circuit breaker for {} opened due to high slow call rate", serviceName);
            }
        }
    }

    /**
     * 检查是否应该开启熔断器
     */
    private boolean shouldOpenCircuitBreaker(CircuitBreakerState state) {
        long totalCalls = state.totalCalls.get();
        
        // 调用次数不足，不开启熔断器
        if (totalCalls < minimumNumberOfCalls) {
            return false;
        }

        // 计算失败率
        double failureRate = (double) state.failureCount.get() / totalCalls * 100;
        if (failureRate >= failureRateThreshold) {
            return true;
        }

        // 计算慢调用率
        double slowCallRate = (double) state.slowCallCount.get() / totalCalls * 100;
        if (slowCallRate >= slowCallRateThreshold) {
            return true;
        }

        return false;
    }

    /**
     * 检查是否可以尝试重置熔断器
     */
    private boolean shouldAttemptReset(CircuitBreakerState state) {
        if (state.lastFailureTime == null) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();
        return state.lastFailureTime.plusNanos(waitDurationInOpenState * 1_000_000).isBefore(now);
    }

    /**
     * 转换为半开状态
     */
    private void transitionToHalfOpen(CircuitBreakerState state) {
        state.state = State.HALF_OPEN;
        state.stateTransitionTime = LocalDateTime.now();
        state.halfOpenCalls.set(0);
    }

    /**
     * 转换为开启状态
     */
    private void transitionToOpen(CircuitBreakerState state) {
        state.state = State.OPEN;
        state.stateTransitionTime = LocalDateTime.now();
        state.lastFailureTime = LocalDateTime.now();
    }

    /**
     * 转换为关闭状态
     */
    private void transitionToClosed(CircuitBreakerState state) {
        state.state = State.CLOSED;
        state.stateTransitionTime = LocalDateTime.now();
        // 重置计数器
        state.successCount.set(0);
        state.failureCount.set(0);
        state.slowCallCount.set(0);
        state.totalCalls.set(0);
        state.halfOpenCalls.set(0);
    }

    /**
     * 获取熔断器状态
     */
    public State getCircuitBreakerState(String serviceName) {
        CircuitBreakerState state = circuitBreakers.get(serviceName);
        return state != null ? state.state : State.CLOSED;
    }

    /**
     * 手动重置熔断器
     */
    public void resetCircuitBreaker(String serviceName) {
        CircuitBreakerState state = circuitBreakers.get(serviceName);
        if (state != null) {
            transitionToClosed(state);
            log.info("Circuit breaker for {} manually reset to CLOSED", serviceName);
        }
    }

    /**
     * 获取熔断器统计信息
     */
    public CircuitBreakerStats getCircuitBreakerStats(String serviceName) {
        CircuitBreakerState state = circuitBreakers.get(serviceName);
        if (state == null) {
            return new CircuitBreakerStats();
        }

        return CircuitBreakerStats.builder()
                .serviceName(serviceName)
                .state(state.state)
                .successCount(state.successCount.get())
                .failureCount(state.failureCount.get())
                .slowCallCount(state.slowCallCount.get())
                .totalCalls(state.totalCalls.get())
                .halfOpenCalls(state.halfOpenCalls.get())
                .lastFailureTime(state.lastFailureTime)
                .stateTransitionTime(state.stateTransitionTime)
                .build();
    }

    /**
     * 熔断器统计信息
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CircuitBreakerStats {
        private String serviceName;
        private State state;
        private int successCount;
        private int failureCount;
        private int slowCallCount;
        private long totalCalls;
        private int halfOpenCalls;
        private LocalDateTime lastFailureTime;
        private LocalDateTime stateTransitionTime;
    }

}
