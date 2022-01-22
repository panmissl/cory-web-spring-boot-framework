package com.cory.web.security;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CoryShiroCacheManager implements CacheManager {

	private static final String SHIRO_CACHE_NAME = "CORY_SHIRO_CACHE";

	private org.springframework.cache.CacheManager cacheManager;

	public CoryShiroCacheManager(org.springframework.cache.CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	@Override
	public <K, V> Cache<K, V> getCache(String s) throws CacheException {
		org.springframework.cache.Cache springCache = cacheManager.getCache(SHIRO_CACHE_NAME);
		if (null == springCache) {
			return new ShiroCache<>();
		}
		String content = springCache.get(SHIRO_CACHE_NAME, String.class);
		if (StringUtils.isBlank(content)) {
			return new ShiroCache<>();
		}
		return JSON.parseObject(content, ShiroCache.class);
	}

	public static class ShiroCache<K, V> implements Cache<K, V> {

		private Map<K, V> cache = new ConcurrentHashMap<>();

		@Override
		public V get(K k) throws CacheException {
			return cache.get(k);
		}

		@Override
		public V put(K k, V v) throws CacheException {
			return cache.put(k, v);
		}

		@Override
		public V remove(K k) throws CacheException {
			return cache.remove(k);
		}

		@Override
		public void clear() throws CacheException {
			cache.clear();
		}

		@Override
		public int size() {
			return cache.size();
		}

		@Override
		public Set<K> keys() {
			return cache.keySet();
		}

		@Override
		public Collection<V> values() {
			return cache.values();
		}
	}
}
