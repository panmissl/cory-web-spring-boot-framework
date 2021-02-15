package com.cory.util.systemconfigcache;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Cory on 2017/5/14.
 */
public class SystemConfigCacheUtil {

    private static Map<String, String> cacheMap = new ConcurrentHashMap<String, String>();

    private SystemConfigCacheUtil() {}

    public static String getCache(String key) {
        return cacheMap.get(key);
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
        String val = cacheMap.get(key);
        if (StringUtils.isEmpty(val)) {
            return defaultVal;
        }
        try {
            return Integer.valueOf(val);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    public static Map<String, String> getAllCache() {
        return Collections.unmodifiableMap(cacheMap);
    }

    public static void refresh(String key, String value) {
        cacheMap.put(key, value);
    }
}
