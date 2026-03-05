package com.devh.payment_sim.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration
public class PaginationConfig {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    @Bean
    public PageableHandlerMethodArgumentResolverCustomizer pageableCustomizer() {
        return resolver -> {
            resolver.setOneIndexedParameters(false); // 0-based page
            resolver.setMaxPageSize(MAX_SIZE);
            // No global sort here; we want explicit per endpoint control.
            resolver.setFallbackPageable(PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE));
        };
    }
}
