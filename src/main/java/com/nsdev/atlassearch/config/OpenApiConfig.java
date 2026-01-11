package com.nsdev.atlassearch.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI atlasOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Atlas Search API")
                        .description("High-performance hotel search API with Multi-Level Caching (L1 Caffeine + L2 Redis).")
                        .version("1.0.0")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")));
    }
}