package com.cory.web.security;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.RealmSecurityManager;

public class ShiroCacheUtils {

	/**
	 * 清除缓存
	 */
	public static void clearAllCache() {
		clearCache(true, true);
	}

	/**
	 * 清除登录信息缓存
	 */
	public static void clearAuthenticationCache() {
		clearCache(true, false);
	}

    /**
     * 清除登录信息缓存
	 */
	public static void clearCache(String username) {
		if (StringUtils.isBlank(username)) {
			return;
		}
		clearCache(false, false, username);
	}

	/**
	 * 清除授权信息缓存
	 */
	public static void clearAuthorizationCache() {
		clearCache(false, true);
	}

	private static void clearCache(boolean clearAuthenticationCache, boolean clearAuthorizationCache) {
		clearCache(clearAuthenticationCache, clearAuthorizationCache, null);
	}

	private static void clearCache(boolean clearAuthenticationCache, boolean clearAuthorizationCache, String key) {
		org.apache.shiro.mgt.SecurityManager securityManager = SecurityUtils.getSecurityManager();
		if (securityManager instanceof RealmSecurityManager) {
			RealmSecurityManager realmSecurityManager = (RealmSecurityManager) securityManager;
			if (CollectionUtils.isNotEmpty(realmSecurityManager.getRealms())) {
				realmSecurityManager.getRealms().forEach(realm -> {
					if (realm instanceof AuthorizingRealm) {
						AuthorizingRealm authorizingRealm = (AuthorizingRealm) realm;
						if (clearAuthenticationCache) {
							authorizingRealm.getAuthenticationCache().clear();
						}
						if (clearAuthorizationCache) {
							authorizingRealm.getAuthorizationCache().clear();
						}
						if (StringUtils.isNotBlank(key)) {
							authorizingRealm.getAuthenticationCache().remove(key);
							authorizingRealm.getAuthorizationCache().remove(key);
						}
					}
				});
			}
		}
	}
}
