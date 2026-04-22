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
// 当Redis不可用时启用此配置
@org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass("org.springframework.data.redis.connection.RedisConnectionFactory")
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