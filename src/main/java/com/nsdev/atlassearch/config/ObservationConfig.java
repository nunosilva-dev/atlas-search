package com.nsdev.atlassearch.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Micrometer Observation AOP.
 * <p>
 * This configuration enables the usage of the {@code @Observed} annotation on methods
 * and classes. It registers the {@link ObservedAspect} which intercepts annotated methods
 * to generate traces and metrics automatically.
 * </p>
 */
@Configuration
public class ObservationConfig {

    /**
     * Registers the aspect required for @Observed annotations to work.
     *
     * @param observationRegistry the registry where observations are recorded.
     * @return the configured aspect.
     */
    @Bean
    public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        return new ObservedAspect(observationRegistry);
    }
}