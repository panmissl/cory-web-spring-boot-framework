package com.cory.web.security;

import com.alibaba.fastjson.JSON;
import com.cory.constant.Constants;
import com.cory.constant.ErrorCode;
import com.cory.exception.CoryException;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ShiroRedisCacheManager implements CacheManager {

	private RedisConnectionFactory connectionFactory;

	public ShiroRedisCacheManager(RedisConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	@Override
	public <K, V> Cache<K, V> getCache(String s) throws CacheException {
		try {
			byte[] bytes = connectionFactory.getConnection().get(s.getBytes(Constants.UTF8));
			return JSON.parseObject(new String(bytes, Constants.UTF8), ShiroCache.class);
		} catch (UnsupportedEncodingException e) {
			throw new CoryException(ErrorCode.GENERIC_ERROR, "un supported encoding");
		}
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
