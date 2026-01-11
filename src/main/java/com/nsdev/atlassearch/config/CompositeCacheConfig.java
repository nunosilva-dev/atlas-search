package com.nsdev.atlassearch.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class CompositeCacheConfig {

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