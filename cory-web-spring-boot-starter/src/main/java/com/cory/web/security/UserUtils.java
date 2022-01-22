package com.cory.web.security;

import com.cory.util.StrUtils;
import com.cory.util.systemconfigcache.SystemConfigCacheKey;
import com.cory.util.systemconfigcache.SystemConfigCacheUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class UserUtils {
	
	/**
	 * 获取当前登录用户的logonId
	 * @return
	 */
	public static String getCurrentUserLogonId() {
		return (String) SecurityUtils.getSubject().getPrincipal();
	}

	/**
	 * 判断当前用户是不是超级管理员：root, admin
	 * @return
	 */
	public static boolean isAdmin() {
		if (isRoot()) {
			return true;
		}
		try {
			List<String> adminRoles = StrUtils.toList(SystemConfigCacheUtil.getCache(SystemConfigCacheKey.ADMIN_ROLE_NAMES));
			if (CollectionUtils.isEmpty(adminRoles)) {
				return false;
			}
			boolean isAdmin = false;
			for (String adminRole : adminRoles) {
				if (SecurityUtils.getSubject().hasRole(adminRole)) {
					isAdmin = true;
					break;
				}
			}
			if (isAdmin) {
				return true;
			}
			String msg = "User does not have roles [" + adminRoles + "]";
			throw new UnauthorizedException(msg);
		} catch (AuthorizationException e) {
			return false;
		}
	}

	/**
	 * 判断当前用户是不是超级管理员：root
	 * @return
	 */
	public static boolean isRoot() {
		try {
			String rootRole = SystemConfigCacheUtil.getCache(SystemConfigCacheKey.ROOT_ROLE_NAME);
			if (StringUtils.isEmpty(rootRole)) {
				return false;
			}
			SecurityUtils.getSubject().checkRole(rootRole);
			return true;
		} catch (AuthorizationException e) {
			return false;
		}
	}

	/**
	 * 判断用户是否登录
	 * @return
	 */
	public static boolean isLoginUser() {
		return isLoginUser(SecurityUtils.getSubject());
	}
	
	/**
	 * 判断用户是否登录
	 * @param subject
	 * @return
	 */
	public static boolean isLoginUser(Subject subject) {
		if (null == subject) {
			return false;
		}
		//加上rememberMe功能
		boolean isRemembered = subject.isRemembered();
		return isRemembered || subject.isAuthenticated();
	}
	
	/**
	 * 判断用户是否有权限访问指定的url
	 * @param url
	 * @return
	 */
	public static boolean canAccess(String url) {
		try {
			SecurityUtils.getSubject().checkPermission(url);
			return true;
		} catch (AuthorizationException e) {
			return false;
		}
	}
}
