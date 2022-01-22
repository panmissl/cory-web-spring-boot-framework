package com.cory.util.systemconfigcache;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.CacheManager;

/**
 * Created by Cory on 2017/5/14.
 */
public class SystemConfigCacheUtil {

    private static final String CACHE_NAME = "SystemConfigCacheUtil";

    private static CacheManager cacheManager;

    private SystemConfigCacheUtil() {}

    public static String getCache(String key) {
        return cacheManager.getCache(CACHE_NAME).get(key, String.class);
    }

    /**
     * default: 0
     * @param key
     * @return
     */
    public static int getIntCache(String key) {
        return getIntCache(key, 0);
    }

    public static int getIntCache(String key, int defaultVal) {
        String val = cacheManager.getCache(CACHE_NAME).get(key, String.class);
        if (StringUtils.isEmpty(val)) {
            return defaultVal;
        }
        try {
            return Integer.valueOf(val);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    public static void refresh(String key, String value) {
        cacheManager.getCache(CACHE_NAME).put(key, value);
    }

    public static void setCacheManager(CacheManager cacheManager) {
        SystemConfigCacheUtil.cacheManager = cacheManager;
    }
}
