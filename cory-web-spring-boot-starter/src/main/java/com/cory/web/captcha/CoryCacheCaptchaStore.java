package com.cory.web.captcha;

import com.octo.captcha.Captcha;
import com.octo.captcha.service.CaptchaServiceException;
import com.octo.captcha.service.captchastore.CaptchaStore;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CoryCacheCaptchaStore implements CaptchaStore {

	private static final String CACHE_NAME = "CORY_CAPTCHA_STORE_CACHE";
	private static final String MAP_KEY = "MAP_KEY";

	private CacheManager cacheManager;

	public CoryCacheCaptchaStore(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	@Override
	public boolean hasCaptcha(String id) {
		return getFromCache().containsKey(id);
	}

	@Override
	public void storeCaptcha(String id, Captcha captcha) throws CaptchaServiceException {
		this.storeCaptcha(id, captcha, null);
	}

	@Override
	public void storeCaptcha(String id, Captcha captcha, Locale locale) throws CaptchaServiceException {
		saveToCache(id, captcha);
	}

	@Override
	public boolean removeCaptcha(String id) {
		saveToCache(id, null);
		return true;
	}

	@Override
	public Captcha getCaptcha(String id) throws CaptchaServiceException {
		return getFromCache().get(id);
	}

	@Override
	public Locale getLocale(String id) throws CaptchaServiceException {
		return Locale.CHINA;
	}

	@Override
	public int getSize() {
		return getFromCache().size();
	}

	@Override
	public Collection getKeys() {
		return getFromCache().keySet();
	}

	@Override
	public void empty() {
		cacheManager.getCache(CACHE_NAME).invalidate();
	}

	@Override
	public void initAndStart() {
	}

	@Override
	public void cleanAndShutdown() {
		cacheManager.getCache(CACHE_NAME).clear();
	}

	private Map<String, Captcha> getFromCache() {
		Map cache = cacheManager.getCache(CACHE_NAME).get(MAP_KEY, Map.class);
		if (null == cache) {
			return new HashMap<>();
		}
		return cache;
	}

	private void saveToCache(String id, Captcha captcha) {
		Map<String, Captcha> map = getFromCache();
		if (null == captcha) {
			map.remove(id);
		} else {
			map.put(id, captcha);
		}
		cacheManager.getCache(CACHE_NAME).put(MAP_KEY, map);
	}
}
