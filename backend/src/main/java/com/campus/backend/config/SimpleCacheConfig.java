package com.campus.backend.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Arrays;

@Configuration
@EnableCaching
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
    name = "spring.data.redis.enabled",
    havingValue = "false",
    matchIfMissing = true
)
public class SimpleCacheConfig extends CachingConfigurerSupport {
    
    @Bean
    @Primary
    @Override
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        // 配置缓存名称
        cacheManager.setCacheNames(Arrays.asList(
            "products",
            "categories",
            "users",
            "messages",
            "orders"
        ));
        // 设置允许动态创建缓存
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }
}