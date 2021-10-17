package com.cory.cache.config;

import com.cory.cache.etcd.EtcdCacheManager;
import com.cory.cache.manager.CoryCacheManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.lang.Nullable;

/**
 * Created by Cory on 2021/2/9.
 */
@Configuration
@EnableConfigurationProperties(CoryCacheProperties.class)
@EnableCaching(proxyTargetClass = true)
public class CoryCacheAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = Constant.PREFIX, name = "type", havingValue = Constant.CACHE_TYPE_SIMPLE)
    public ConcurrentMapCacheManager simpleCacheManager(CoryCacheProperties coryCacheProperties) {
        if (!Constant.CACHE_TYPE_SIMPLE.equalsIgnoreCase(coryCacheProperties.getType())) {
            return null;
        }
        return new ConcurrentMapCacheManager();
    }

    @Bean
    @ConditionalOnProperty(prefix = Constant.PREFIX, name = "type", havingValue = Constant.CACHE_TYPE_ETCD)
    public EtcdCacheManager etcdCacheManager(CoryCacheProperties coryCacheProperties) {
        if (!Constant.CACHE_TYPE_ETCD.equalsIgnoreCase(coryCacheProperties.getType())) {
            return null;
        }
        return new EtcdCacheManager(coryCacheProperties.getEtcdServers());
    }

    @Bean
    @ConditionalOnProperty(prefix = Constant.PREFIX, name = "type", havingValue = Constant.CACHE_TYPE_REDIS)
    public RedisCacheManager redisCacheManager(CoryCacheProperties coryCacheProperties, RedisCacheWriter redisCacheWriter) {
        if (!Constant.CACHE_TYPE_REDIS.equalsIgnoreCase(coryCacheProperties.getType())) {
            return null;
        }
        return new RedisCacheManager(redisCacheWriter, RedisCacheConfiguration.defaultCacheConfig());
    }

    @Bean
    @Primary
    public CacheManager cacheManager(@Nullable ConcurrentMapCacheManager simpleCacheManager,
                                     @Nullable EtcdCacheManager etcdCacheManager,
                                     @Nullable RedisCacheManager redisCacheManager,
                                         CoryCacheProperties coryCacheProperties) {
        CoryCacheManager manager = new CoryCacheManager();
        manager.setRedisCacheManager(redisCacheManager);
        manager.setSimpleCacheManager(simpleCacheManager);
        manager.setEtcdCacheManager(etcdCacheManager);
        manager.setCacheType(coryCacheProperties.getType());
        return manager;
    }
}
