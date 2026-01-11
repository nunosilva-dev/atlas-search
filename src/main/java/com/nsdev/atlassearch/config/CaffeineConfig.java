package com.nsdev.atlassearch.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for the Level 1 (L1) In-Memory Cache using Caffeine.
 * <p>
 * This cache acts as the first line of defense to reduce network calls to Redis (L2)
 * and database hits. It is configured for high performance with a short Time-To-Live (TTL)
 * and a limited size to prevent heap memory exhaustion.
 * </p>
 */
@Configuration
public class CaffeineConfig {

    /**
     * Creates the Caffeine CacheManager bean.
     * <p>
     * Configuration highlights:
     * <ul>
     * <li><b>TTL:</b> 1 Minute (Short-lived data to ensure eventual consistency).</li>
     * <li><b>Max Size:</b> 500 entries (Prevents OutOfMemory errors).</li>
     * <li><b>Stats:</b> Enabled for Prometheus monitoring.</li>
     * </ul>
     *
     * @return a configured {@link CaffeineCacheManager} instance.
     */
    @Bean("caffeineCacheManager")
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .maximumSize(500)
                .recordStats()); // Micrometer/Prometheus exposure
        return cacheManager;
    }
}