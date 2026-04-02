package org.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置类
 *
 * 为什么需要自定义线程池？
 *   Spring 默认使用 SimpleAsyncTaskExecutor，它为每个任务创建一个新线程，
 *   不会复用线程，在高并发场景下会导致性能问题。
 *   因此，建议自定义线程池来管理异步任务的执行。
 *
 * 线程池工作原理：
 *   1. 当任务提交时，先使用核心线程处理
 *   2. 核心线程满了，将任务放入队列等待
 *   3. 队列满了，创建新线程直到达到最大线程数
 *   4. 最大线程也满了，执行拒绝策略
 *
 * @author Tomatos
 * @date 2025/7/23
 */
@Configuration
@EnableAsync // 开启异步支持，让 @Async 注解生效
public class ThreadPoolConfig {

    /**
     * 创建自定义线程池 Bean
     *
     * 参数说明：
     *   - corePoolSize: 核心线程数，即使空闲也会保留的线程数
     *   - maxPoolSize: 最大线程数，线程池允许创建的最大线程数
     *   - queueCapacity: 队列容量，等待执行的任务队列大小
     *   - threadNamePrefix: 线程名前缀，方便日志追踪和调试
     *   - rejectedExecutionHandler: 拒绝策略，当线程池满了如何处理新任务
     *
     * 常用拒绝策略：
     *   - CallerRunsPolicy: 调用者线程执行任务（推荐，不会丢失任务）
     *   - AbortPolicy: 抛出 RejectedExecutionException 异常（默认）
     *   - DiscardPolicy: 静默丢弃任务
     *   - DiscardOldestPolicy: 丢弃队列最旧的任务，重新提交当前任务
     *
     * @return 配置好的线程池执行器
     */
    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 设置核心线程数：常驻线程数量
        executor.setCorePoolSize(5);

        // 设置最大线程数：峰值时可创建的最大线程数
        executor.setMaxPoolSize(10);

        // 设置队列容量：等待队列大小，超过后触发创建新线程
        executor.setQueueCapacity(100);

        // 设置线程名前缀：方便在日志中区分异步任务线程
        executor.setThreadNamePrefix("async-task-");

        // 设置拒绝策略：CallerRunsPolicy 表示由调用者线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 初始化线程池
        executor.initialize();

        return executor;
    }
}
