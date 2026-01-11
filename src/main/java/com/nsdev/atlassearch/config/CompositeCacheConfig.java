package com.nsdev.atlassearch.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration responsible for wiring the Multi-Level Caching strategy.
 * <p>
 * It aggregates the individual cache providers (Caffeine and Redis) into a single
 * {@link LayeredCacheManager} that orchestrates the data flow.
 * </p>
 */
@Configuration
public class CompositeCacheConfig {

    /**
     * Creates the primary CacheManager for the application.
     * <p>
     * This bean is marked as {@code @Primary} to ensure Spring uses this implementation
     * whenever the {@code @Cacheable} annotation is encountered.
     * </p>
     *
     * @param l1Cache       The L1 in-memory cache manager (Caffeine).
     * @param l2Cache       The L2 distributed cache manager (Redis).
     * @param meterRegistry The Micrometer registry for manual metric recording.
     * @return The orchestrated {@link LayeredCacheManager}.
     */
    @Bean
    @Primary
    public CacheManager compositeCacheManager(
            @Qualifier("caffeineCacheManager") CacheManager l1Cache,
            @Qualifier("redisCacheManager") CacheManager l2Cache,
            MeterRegistry meterRegistry
    ) {
        return new LayeredCacheManager(l1Cache, l2Cache, meterRegistry);
    }
}