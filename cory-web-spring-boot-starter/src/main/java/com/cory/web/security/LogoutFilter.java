package com.cory.web.security;

import com.cory.web.util.URLHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class LogoutFilter extends org.apache.shiro.web.filter.authc.LogoutFilter {
	@Override
	protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
		Subject subject = SecurityUtils.getSubject();
		String username = null == subject ? null : null == subject.getPrincipal() ? null : subject.getPrincipal().toString();
		boolean result = super.preHandle(request, response);
		if (null == username) {
			ShiroCacheUtils.clearCache(username);
		}
		return result;
	}

	protected String getRedirectUrl(ServletRequest req, ServletResponse resp, Subject subject) {
		HttpServletRequest request = (HttpServletRequest) req;
		String redirectUrl = URLHelper.parseReturnUrl(request);
		if (StringUtils.isBlank(redirectUrl)) {
			redirectUrl = getRedirectUrl();
		}
		return redirectUrl;
	}

	@Override
	protected void issueRedirect(ServletRequest request,
								 ServletResponse response, String redirectUrl) throws Exception {
		redirectUrl = URLHelper.fixReturnUrlForLogout((HttpServletRequest) request, redirectUrl);

		super.issueRedirect(request, response, redirectUrl);
	}
}
