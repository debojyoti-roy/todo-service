package com.tradebyte.todo_service.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Spring Cache using in-memory ConcurrentMapCacheManager.
 * Defines caches used in the Todo application.
 */
@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        // Register all caches used in the application
        return new ConcurrentMapCacheManager(
                "todoById",
                "todoList"
        );
    }
}