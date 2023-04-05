package com.cory.web.interceptor;

import com.cory.constant.Constants;
import com.cory.context.CoryContext;
import com.cory.context.CurrentUser;
import com.cory.util.systemconfigcache.SystemConfigCacheKey;
import com.cory.util.systemconfigcache.SystemConfigCacheUtil;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Order(120)
public class ContextInterceptor implements HandlerInterceptor {

	@Value("${cory.shiro.success-url}")
	private String successUrl;

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
		builder.successUrl(successUrl);

		builder.domainName(SystemConfigCacheUtil.getCache(SystemConfigCacheKey.DOMAIN_NAME));
		builder.site(SystemConfigCacheUtil.getCache(SystemConfigCacheKey.SITE));
		builder.siteName(SystemConfigCacheUtil.getCache(SystemConfigCacheKey.SITE_NAME));
		builder.siteSlogan(SystemConfigCacheUtil.getCache(SystemConfigCacheKey.SITE_SLOGAN));
		builder.siteKeywords(SystemConfigCacheUtil.getCache(SystemConfigCacheKey.SITE_KEYWORDS));
		builder.siteDescription(SystemConfigCacheUtil.getCache(SystemConfigCacheKey.SITE_DESCRIPTION));
		builder.siteDescriptionBody(SystemConfigCacheUtil.getCache(SystemConfigCacheKey.SITE_DESCRIPTION_BODY));

		builder.staticDir(buildStaticDir());
		builder.jsFile(SystemConfigCacheUtil.getCache(SystemConfigCacheKey.JS_FILE));
		builder.cssFile(SystemConfigCacheUtil.getCache(SystemConfigCacheKey.CSS_FILE));
		builder.faviconFile(SystemConfigCacheUtil.getCache(SystemConfigCacheKey.FAVICON_FILE));

		builder.debugMode(SystemConfigCacheUtil.getCache(SystemConfigCacheKey.DEBUG_MODE));

		String adminSkin = SystemConfigCacheUtil.getCache(SystemConfigCacheKey.ADMIN_SKIN);
		if (StringUtils.isEmpty(adminSkin)) {
			adminSkin = "blue";
		}
		builder.adminSkin(adminSkin);
		builder.registerEnable(SystemConfigCacheUtil.getCache(SystemConfigCacheKey.REGISTER_ENABLE));

		CoryContext ctx = builder.build();
		CoryContext.set(ctx);

		//全部放到request里
		BeanMap beanMap = new BeanMap(ctx);
		beanMap.entrySet().forEach(entry -> request.setAttribute(entry.getKey().toString(), entry.getValue()));

		return true;
	}

	private String buildStaticDir() {
		String staticDir = SystemConfigCacheUtil.getCache(SystemConfigCacheKey.STATIC_DIR);
		if (null == staticDir) {
			staticDir = "";
		} else if (!staticDir.endsWith("/")) {
			staticDir += "/";
		}
		String staticVersion = SystemConfigCacheUtil.getCache(SystemConfigCacheKey.STATIC_VERSION);
		if (StringUtils.isNotBlank(staticVersion)) {
			if (staticVersion.startsWith("/")) {
				staticVersion = staticVersion.substring(1);
			}
			if (!staticVersion.endsWith("/")) {
				staticVersion += "/";
			}
			staticDir += staticVersion;
		}
		return staticDir;
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
