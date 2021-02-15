package com.cory.web.security;

import com.cory.constant.Constants;
import com.cory.model.base.Resource;
import com.cory.model.base.Role;
import com.cory.sevice.base.RoleService;
import com.cory.util.systemconfigcache.SystemConfigCacheKey;
import com.cory.util.systemconfigcache.SystemConfigCacheUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * 看当前URL是否允许匿名访问，如果允许，那么直接返回。
 * 如果不允许，那么看用户登录了没，如果没有登录，那么中转到登录页进行登录；
 * 如果登录了，那么看是否允许此用户访问，如果允许，那么返回，
 * 如果不允许，那么跳转到403页面
 * 通过重写isAccessAllowed和onAccessDenied方法实现
 *
 * 对于Save和Delete方法，在方法里判断是不是自己的，如果不是，则抛出异常。
 * @author Cory
 *
 */
public class UserFilter extends org.apache.shiro.web.filter.authc.UserFilter {

	@Autowired
	private RoleService roleService;

	//未登录重定向到登陆页
	protected void redirectToLogin(ServletRequest req, ServletResponse resp) throws IOException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		//设置returnUrl
		String returnUrl = getEncodedReturnUrl(request);
		String loginUrl = getLoginUrl();
		if (!StringUtils.isEmpty(returnUrl)) {
			loginUrl = loginUrl + "?returnUrl=" + returnUrl;
		}

		WebUtils.issueRedirect(request, response, loginUrl);
	}

	private String getEncodedReturnUrl(HttpServletRequest request) throws UnsupportedEncodingException {
		String returnUrl = request.getRequestURI();
		if (returnUrl.startsWith(request.getContextPath())) {
			returnUrl = returnUrl.substring(request.getContextPath().length());
		}
		if (returnUrl.startsWith("/")) {
			returnUrl = returnUrl.substring(1);
		}
		String queryString = request.getQueryString();
		if (!StringUtils.isEmpty(queryString)) {
			returnUrl = returnUrl + "?" + queryString;
		}
		returnUrl = URLEncoder.encode(returnUrl, Constants.UTF8);
		return returnUrl;
	}

	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
		boolean accessAllowed = false;

		String requestUrl = this.getPathWithinApplication(request);
		Subject subject = this.getSubject(request, response);
		if (!UserUtils.isLoginUser(subject)) {
			if (isAnonUrl(requestUrl)) {
				accessAllowed = true;
			}
		} else {
			if (isAnonUrl(requestUrl) || canAccess(request, response, requestUrl)) {
				accessAllowed = true;
			}
		}

		return accessAllowed;
	}

	/**
	 * 判断登录用户有没有访问此URL的权限
	 * @param requestUrl
	 * @return
	 */
	private boolean canAccess(ServletRequest request, ServletResponse response, String requestUrl) {
		Subject subject = this.getSubject(request, response);
		return subject.isPermitted(requestUrl);
	}

	/**
	 * 判断是不是匿名用户可以访问
	 * @param requestUrl
	 * @return
	 */
	private boolean isAnonUrl(String requestUrl) {
		String anonRoleName = SystemConfigCacheUtil.getCache(SystemConfigCacheKey.ANON_ROLE_NAME);
		if (StringUtils.isBlank(anonRoleName)) {
			return false;
		}
		Role anonRole = roleService.getByName(anonRoleName);
		if (null == anonRole) {
			return false;
		}
		List<Resource> resources = anonRole.getResources();
		if (CollectionUtils.isEmpty(resources)) {
			return false;
		}
		for (Resource r : resources) {
			if (this.pathsMatch(r.getValue(), requestUrl)) {
				return true;
			}
		}
		return false;
	}

	private void redirectTo403Page(ServletRequest request, ServletResponse response) {
		try {
			((HttpServletResponse) response).sendRedirect(((HttpServletRequest) request).getContextPath() + "/e/403.html");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		Subject subject = this.getSubject(request, response);
		boolean isAuthenticated = subject.isAuthenticated();
		//登录了但没权限，到403
		if (isAuthenticated) {
			redirectTo403Page(request, response);
			return false;
		} else {
			//没登录，到登录页
			return super.onAccessDenied(request, response);
		}
	}

}
