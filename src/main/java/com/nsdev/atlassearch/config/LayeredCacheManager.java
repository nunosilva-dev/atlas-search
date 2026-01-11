package com.nsdev.atlassearch.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Custom CacheManager implementation that coordinates a Multi-Level Caching strategy.
 * <p>
 * <b>Strategy:</b>
 * <ol>
 * <li><b>L1 (Local Heap):</b> Fastest access. Checked first.</li>
 * <li><b>L2 (Distributed Redis):</b> Fallback if L1 misses. Checked second.</li>
 * <li><b>Synchronization:</b> If data is found in L2, it is backfilled into L1.</li>
 * </ol>
 * <p>
 * This class also manually registers metrics with Micrometer to ensure visibility
 * into cache hits/misses for both layers, overcoming limitations of proxy-based monitoring.
 */
@RequiredArgsConstructor
public class LayeredCacheManager implements CacheManager {

    private final CacheManager l1CacheManager;
    private final CacheManager l2CacheManager;
    private final MeterRegistry meterRegistry;

    @Override
    @Nullable
    public Cache getCache(@NonNull String name) {
        Cache l1 = l1CacheManager.getCache(name);
        Cache l2 = l2CacheManager.getCache(name);

        if (l1 != null && l2 != null) {
            return new LayeredCache(name, l1, l2, meterRegistry);
        }
        return l1 != null ? l1 : l2;
    }

    @Override
    @NonNull
    public Collection<String> getCacheNames() {
        return Collections.emptyList();
    }

    /**
     * Inner class wrapper that implements the actual Layered Cache logic.
     */
    static class LayeredCache extends AbstractValueAdaptingCache {
        private final String name;
        private final Cache l1;
        private final Cache l2;
        private final MeterRegistry meterRegistry;

        LayeredCache(String name, Cache l1, Cache l2, MeterRegistry meterRegistry) {
            super(true);
            this.name = name;
            this.l1 = l1;
            this.l2 = l2;
            this.meterRegistry = meterRegistry;
        }

        /**
         * Records cache access metrics explicitly.
         * <p>
         * This bypasses Spring's AOP metrics, which fail to see inside composite managers.
         *
         * @param result       The outcome ("hit" or "miss").
         * @param cacheManager The specific layer being accessed.
         */
        private void recordMetric(String result, String cacheManager) {
            meterRegistry.counter("cache.gets",
                    List.of(
                            Tag.of("result", result),
                            Tag.of("cache", name),
                            Tag.of("cache_manager", cacheManager),
                            Tag.of("name", name)
                    )
            ).increment();
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public Object getNativeCache() {
            return this;
        }

        @Override
        @Nullable
        protected Object lookup(Object key) {
            Object value = l1.get(key, Object.class);
            if (value != null) {
                recordMetric("hit", "caffeineCacheManager");
                return value;
            }
            recordMetric("miss", "caffeineCacheManager");

            value = l2.get(key, Object.class);
            if (value != null) {
                recordMetric("hit", "redisCacheManager");
                l1.put(key, value);
                return value;
            }
            recordMetric("miss", "redisCacheManager");

            return null;
        }

        @Override
        public <T> T get(Object key, Callable<T> valueLoader) {
            Object value = lookup(key);
            if (value != null) {
                return (T) value;
            }
            try {
                T loadedValue = valueLoader.call();
                put(key, loadedValue);
                return loadedValue;
            } catch (Exception e) {
                throw new ValueRetrievalException(key, valueLoader, e);
            }
        }

        @Override
        public void put(Object key, Object value) {
            l2.put(key, value);
            l1.put(key, value);
        }

        @Override
        public void evict(Object key) {
            l1.evict(key);
            l2.evict(key);
        }

        @Override
        public void clear() {
            l1.clear();
            l2.clear();
        }
    }
}