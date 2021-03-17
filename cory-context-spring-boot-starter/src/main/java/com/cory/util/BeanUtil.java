package com.cory.util;

import org.springframework.cglib.beans.BeanCopier;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author cory
 */
public class BeanUtil {

	private static final ConcurrentHashMap<String, BeanCopier> BEAN_COPIER_CACHE = new ConcurrentHashMap<>();

	public static <T> T copy(Object source, Class<T> cls) {
		if (null == source) {
			return null;
		}
		try {
			T t = cls.newInstance();
			copy(source, t);
			return t;
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	public static void copy(Object source, Object target) {
		String key = genKey(source.getClass(), target.getClass());
		BeanCopier beanCopier;
		if (BEAN_COPIER_CACHE.containsKey(key)) {
			beanCopier = BEAN_COPIER_CACHE.get(key);
		} else {
			beanCopier = BeanCopier.create(source.getClass(), target.getClass(), false);
			BEAN_COPIER_CACHE.put(key, beanCopier);
		}
		beanCopier.copy(source, target, null);
	}

	private static String genKey(Class<?> srcClazz, Class<?> tgtClazz) {
		return srcClazz.getName() + tgtClazz.getName();
	}
}
