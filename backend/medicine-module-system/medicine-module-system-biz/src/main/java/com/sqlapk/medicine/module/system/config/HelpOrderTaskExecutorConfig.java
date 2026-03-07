package com.sqlapk.medicine.module.system.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 工单任务执行器配置
 * 为工单相关的异步任务提供专用的线程池
 *
 * @author sqlapk
 */
@Configuration
@EnableAsync
@Slf4j
public class HelpOrderTaskExecutorConfig {

    /**
     * 工单任务专用线程池
     * 用于处理工单相关的异步任务，如批量更新、导出等
     */
    @Bean("helpOrderTaskExecutor")
    public ThreadPoolExecutor helpOrderTaskExecutor() {
        // 核心线程数：CPU核心数
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        
        // 最大线程数：核心线程数的2倍
        int maximumPoolSize = corePoolSize * 2;
        
        // 线程空闲时间：60秒
        long keepAliveTime = 60L;
        
        // 工作队列：有界队列，避免内存溢出
        LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(1000);
        
        // 线程工厂：自定义线程名称
        AtomicInteger threadNumber = new AtomicInteger(1);
        ThreadFactory threadFactory = r -> {
            Thread thread = new Thread(r, "help-order-task-" + threadNumber.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };
        
        // 拒绝策略：调用者运行，确保任务不丢失
        ThreadPoolExecutor.CallerRunsPolicy rejectedExecutionHandler = 
            new ThreadPoolExecutor.CallerRunsPolicy();
        
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            corePoolSize,
            maximumPoolSize,
            keepAliveTime,
            TimeUnit.SECONDS,
            workQueue,
            threadFactory,
            rejectedExecutionHandler
        );
        
        // 允许核心线程超时
        executor.allowCoreThreadTimeOut(true);
        
        log.info("工单任务线程池初始化完成 - 核心线程数: {}, 最大线程数: {}, 队列大小: {}", 
            corePoolSize, maximumPoolSize, workQueue.remainingCapacity());
        
        return executor;
    }

    /**
     * 工单导出专用线程池
     * 用于处理大量数据的导出任务
     */
    @Bean("helpOrderExportExecutor")
    public ThreadPoolExecutor helpOrderExportExecutor() {
        // 导出任务通常需要更多线程
        int corePoolSize = 4;
        int maximumPoolSize = 8;
        
        LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(500);
        
        AtomicInteger threadNumber = new AtomicInteger(1);
        ThreadFactory threadFactory = r -> {
            Thread thread = new Thread(r, "help-order-export-" + threadNumber.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };
        
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            corePoolSize,
            maximumPoolSize,
            120L, // 导出任务可能需要更长时间
            TimeUnit.SECONDS,
            workQueue,
            threadFactory,
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        executor.allowCoreThreadTimeOut(true);
        
        log.info("工单导出线程池初始化完成 - 核心线程数: {}, 最大线程数: {}", 
            corePoolSize, maximumPoolSize);
        
        return executor;
    }
}
