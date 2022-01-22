package com.cory.cache.etcd;

import com.cory.constant.Constants;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by Cory on 2018/7/22.
 */
public class EtcdCacheHelper {

    private static final String NULL_VALUE_STRING = "__ETCD_NULL_VALUE_STRING__";

    public static String buildCacheKey(String cacheName) {
        assert null == cacheName : "cacheName can't be null.";
        if (cacheName.startsWith(Constants.SPT)) {
            cacheName = cacheName.substring(1);
        }
        return EtcdCacheManager.CACHE_ROOT_KEY + cacheName;
    }

    public static String buildCacheKey(String cacheName, String shortKey) {
        assert null == cacheName : "cacheName can't be null.";
        assert null == shortKey : "shortKey can't be null.";
        if (cacheName.startsWith(Constants.SPT)) {
            cacheName = cacheName.substring(1);
        }
        if (!cacheName.endsWith(Constants.SPT)) {
            cacheName = cacheName + Constants.SPT;
        }
        if (shortKey.startsWith(Constants.SPT)) {
            shortKey = shortKey.substring(1);
        }
        return EtcdCacheManager.CACHE_ROOT_KEY + cacheName + shortKey;
    }

    public static String buildValue(Object value) {
        //Assert.notNull(value, "value can't be null.");
        if (null == value) {
            return NULL_VALUE_STRING;
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(value);
            return new String(baos.toByteArray(), Constants.ISO88591);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object parseValue(String data) {
        //Assert.notNull(data, "data is null.");
        if (StringUtils.isBlank(data) || StringUtils.equals(NULL_VALUE_STRING, data)) {
            return null;
        }
        try {
            return new ObjectInputStream(new ByteArrayInputStream(data.getBytes(Constants.ISO88591))).readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
