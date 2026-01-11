package com.nsdev.atlassearch.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

/**
 * Configuration for the Level 2 (L2) Distributed Cache using Redis.
 * <p>
 * This configuration handles the serialization of Java objects to JSON for storage in Redis,
 * enabling human-readable data and interoperability. It is configured with a longer TTL
 * than L1.
 * </p>
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * Creates the Redis CacheManager.
     * <p>
     * <b>Serialization Note:</b> Uses {@link GenericJackson2JsonRedisSerializer} with default typing
     * to allow polymorphic deserialization. This enables storing complex objects without
     * manual mapping, although care should be taken with class allow-lists in production.
     * </p>
     *
     * @param connectionFactory the Redis connection factory (configured by Spring Boot).
     * @return a configured {@link RedisCacheManager}.
     */
    @Bean("redisCacheManager")
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .enableStatistics() // Prometheus visibility
                .build();
    }
}