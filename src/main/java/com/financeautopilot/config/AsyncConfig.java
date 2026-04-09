package com.financeautopilot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

// Without this — Spring creates a new thread for every @Async call with no limit.
// Under load your server crashes.
// With this — controlled, bounded thread pool. Safe under load.
// creates a pool of background threads -
// like hiring 3–10 background kitchen workers.
// When multiple users send transactions at the same time,
// each categorization gets its own background thread.
// They all run in parallel.
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "categorizationExecutor")
    public Executor categorizationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3); // 3 threads always ready
        executor.setMaxPoolSize(10); // can scale to 10 if busy
        executor.setQueueCapacity(50); // if all 10 busy, queue upto 50 more
        executor.setThreadNamePrefix("categorization -");
        executor.initialize();
        return executor;
    }
}
