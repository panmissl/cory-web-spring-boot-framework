package com.cory.util.systemconfigcache;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Cory on 2017/5/14.
 */
public class SystemConfigCacheUtil {

    private static final ConcurrentMap<String, String> CACHE = new ConcurrentHashMap<>(1024);

    private SystemConfigCacheUtil() {}

    public static String getCache(String key) {
        return CACHE.get(key);
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
        String val = getCache(key);
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
        CACHE.put(key, value);
    }
}
