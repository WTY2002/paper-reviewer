package com.paper.reviewer.ai.config;

import com.paper.reviewer.ai.parser.AiOutputParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.Executor;

@Configuration
public class AiConfiguration {
    @Bean AiOutputParser aiOutputParser(ObjectMapper objectMapper) { return new AiOutputParser(objectMapper); }

    @Bean(name = "reviewExecutor")
    Executor reviewExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("review-ai-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
