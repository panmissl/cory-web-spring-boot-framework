package com.cory.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Cory on 2021/2/12.
 */
public class MapBuilder<K, V> {

    private Map<K, V> map = new HashMap<>();

    public static <K, V> MapBuilder<K, V> create(Class<K> kClass, Class<V> vClass) {
        return new MapBuilder<>();
    }

    public MapBuilder<K, V> put(K key, V value) {
        map.put(key, value);
        return this;
    }

    public Map<K, V> build() {
        return map;
    }
}
