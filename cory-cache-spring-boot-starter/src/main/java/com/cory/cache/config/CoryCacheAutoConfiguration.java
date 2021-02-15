package com.cory.cache.config;

import com.cory.cache.etcd.EtcdCacheManager;
import com.cory.cache.manager.CoryCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 * 使用前，在application.properties文件里配置数据库信息：spring.datasource.username、spring.datasource.password、spring.datasource.name
 * <br />
 * Created by Cory on 2021/2/9.
 */
@Configuration
@EnableConfigurationProperties(CoryCacheProperties.class)
public class CoryCacheAutoConfiguration {

    @Autowired
    private CoryCacheProperties coryCacheProperties;

    @Autowired
    private RedisCacheWriter redisCacheWriter;

    @Bean
    @ConditionalOnMissingBean
    public LettuceConnectionFactory connectionFactory() {
        return new LettuceConnectionFactory();
    }

    @Bean
    @ConditionalOnProperty(prefix = Constant.PREFIX, name = "type", havingValue = Constant.CACHE_TYPE_SIMPLE)
    public ConcurrentMapCacheManager simpleCacheManager() {
        if (!Constant.CACHE_TYPE_SIMPLE.equalsIgnoreCase(coryCacheProperties.getType())) {
            return null;
        }
        return new ConcurrentMapCacheManager();
    }

    @Bean
    @ConditionalOnProperty(prefix = Constant.PREFIX, name = "type", havingValue = Constant.CACHE_TYPE_ETCD)
    public EtcdCacheManager etcdCacheManager() {
        if (!Constant.CACHE_TYPE_ETCD.equalsIgnoreCase(coryCacheProperties.getType())) {
            return null;
        }
        return new EtcdCacheManager(coryCacheProperties.getEtcdServers());
    }

    @Bean
    @ConditionalOnProperty(prefix = Constant.PREFIX, name = "type", havingValue = Constant.CACHE_TYPE_REDIS)
    public RedisCacheManager redisCacheManager() {
        if (!Constant.CACHE_TYPE_REDIS.equalsIgnoreCase(coryCacheProperties.getType())) {
            return null;
        }
        return new RedisCacheManager(redisCacheWriter, RedisCacheConfiguration.defaultCacheConfig());
    }

    @Bean
    public CoryCacheManager coryCacheManager(ConcurrentMapCacheManager simpleCacheManager,
                                             EtcdCacheManager etcdCacheManager,
                                             RedisCacheManager redisCacheManager) {
        CoryCacheManager manager = new CoryCacheManager();
        manager.setRedisCacheManager(redisCacheManager);
        manager.setSimpleCacheManager(simpleCacheManager);
        manager.setEtcdCacheManager(etcdCacheManager);
        manager.setCacheType(coryCacheProperties.getType());
        return manager;
    }
}
