package com.cory.web.interceptor;

import com.cory.constant.Constants;
import com.cory.context.CoryContext;
import com.cory.context.CurrentUser;
import com.cory.util.systemconfigcache.SystemConfigCacheKey;
import com.cory.util.systemconfigcache.SystemConfigCacheUtil;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Order(120)
public class ContextInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		parseCurrentUser(request);

		CoryContext.CoryContextBuilder builder = CoryContext.builder();

		String contextPath = request.getContextPath();
		String contextPathWithoutPrefix = contextPath.length() > 0 ? contextPath.substring(1) : contextPath;

		builder.ctx(contextPath + "/");
		builder.ctxWithoutSlash(contextPath);
		builder.referer(request.getHeader("referer"));
		builder.requestURI(request.getRequestURI().replace(contextPath, ""));

		builder.domainName(SystemConfigCacheUtil.getCache(SystemConfigCacheKey.DOMAIN_NAME));
		builder.siteName(SystemConfigCacheUtil.getCache(SystemConfigCacheKey.SITE_NAME));
		builder.siteSlogan(SystemConfigCacheUtil.getCache(SystemConfigCacheKey.SITE_SLOGAN));

		builder.jsDir(SystemConfigCacheUtil.getCache(SystemConfigCacheKey.JS_DIR));
		builder.cssDir(SystemConfigCacheUtil.getCache(SystemConfigCacheKey.CSS_DIR));
		builder.imageDir(SystemConfigCacheUtil.getCache(SystemConfigCacheKey.IMAGE_DIR));
		builder.jsFile(SystemConfigCacheUtil.getCache(SystemConfigCacheKey.JS_FILE));
		builder.cssFile(SystemConfigCacheUtil.getCache(SystemConfigCacheKey.CSS_FILE));

		builder.debugMode(SystemConfigCacheUtil.getCache(SystemConfigCacheKey.DEBUG_MODE));

		String adminSkin = SystemConfigCacheUtil.getCache(SystemConfigCacheKey.ADMIN_SKIN);
		if (StringUtils.isEmpty(adminSkin)) {
			adminSkin = "blue";
		}
		builder.adminSkin(adminSkin);

		CoryContext ctx = builder.build();
		CoryContext.set(ctx);

		//全部放到request里
		BeanMap beanMap = new BeanMap(ctx);
		beanMap.entrySet().forEach(entry -> request.setAttribute(entry.getKey().toString(), entry.getValue()));

		return true;
	}

	private void parseCurrentUser(HttpServletRequest request) {
		CurrentUser currentUser = (CurrentUser) request.getSession().getAttribute(Constants.CURRENT_USER);
		if (null == currentUser) {
			currentUser = new CurrentUser();
		}
		CurrentUser.set(currentUser);
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		CoryContext.remove();
		CurrentUser.remove();
	}
}
