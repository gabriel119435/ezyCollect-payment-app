package org.example.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@Slf4j
public class AsyncConfig {
    public static final String EXECUTOR_NAME = "paymentExecutor";
    private static final String THREAD_PREFIX = "payment-thread-";
    // low capacity with purpose to generate error logs
    private static final int CORE_POOL_SIZE = 1;
    private static final int MAX_POOL_SIZE = 2;
    private static final int QUEUE_CAPACITY = 3;

    @Bean(name = EXECUTOR_NAME)
    public Executor paymentExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix(THREAD_PREFIX);

        executor.setRejectedExecutionHandler((runnable, exec) ->
                log.error("task rejected by {} because pool is full with {} tasks and queue is full with {} tasks",
                        EXECUTOR_NAME,
                        MAX_POOL_SIZE,
                        QUEUE_CAPACITY
                )
        );

        executor.initialize();
        return executor;
    }
}