package com.cory.cache.manager;

import com.cory.cache.config.Constant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;

import java.util.Collection;

/**
 * Created by Cory on 2017/5/15.
 */
public class CoryCacheManager implements CacheManager {

    private static final Logger log = LoggerFactory.getLogger(CoryCacheManager.class);

    //simple/redis/etcd
    private String cacheType;
    private CacheManager etcdCacheManager;
    private CacheManager redisCacheManager;
    private CacheManager simpleCacheManager;
    private CacheManager noOpCacheManager = new NoOpCacheManager();

    public Cache getCache(String s) {
        return determinCacheManager().getCache(s);
    }

    public Collection<String> getCacheNames() {
        return determinCacheManager().getCacheNames();
    }

    private CacheManager determinCacheManager() {
        if (StringUtils.isEmpty(cacheType)) {
            log.info("CacheManager, cacheType=NULL");
            return noOpCacheManager;
        }
        log.debug("CacheManager, cacheType={}", cacheType);

        if (Constant.CACHE_TYPE_REDIS.equalsIgnoreCase(cacheType)) {
            return redisCacheManager;
        } else if (Constant.CACHE_TYPE_ETCD.equalsIgnoreCase(cacheType)) {
            return etcdCacheManager;
        } else if (Constant.CACHE_TYPE_SIMPLE.equalsIgnoreCase(cacheType)) {
            return simpleCacheManager;
        } else {
            throw new UnsupportedOperationException("unsupported cache type");
        }
    }

    public void setRedisCacheManager(CacheManager redisCacheManager) {
        this.redisCacheManager = redisCacheManager;
    }

    public void setSimpleCacheManager(CacheManager simpleCacheManager) {
        this.simpleCacheManager = simpleCacheManager;
    }

    public void setEtcdCacheManager(CacheManager etcdCacheManager) {
        this.etcdCacheManager = etcdCacheManager;
    }

    public void setCacheType(String cacheType) {
        this.cacheType = cacheType;
    }
}
