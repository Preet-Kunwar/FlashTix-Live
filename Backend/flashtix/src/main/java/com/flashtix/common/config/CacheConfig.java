package com.flashtix.common.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    // We will configure Caffeine specifics later, default works perfectly for now!
}