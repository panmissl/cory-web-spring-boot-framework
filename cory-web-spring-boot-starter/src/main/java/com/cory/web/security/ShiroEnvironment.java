package com.cory.web.security;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.env.DefaultWebEnvironment;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

public class ShiroEnvironment extends DefaultWebEnvironment {
	
	private WebApplicationContext ctx;
	
	public ShiroEnvironment(ServletContext sc, WebApplicationContext ctx) {
		this.setServletContext(sc);
		this.ctx = ctx;
		init();
	}

	private void init() {
		DefaultWebSecurityManager securityManager = this.ctx.getBean(DefaultWebSecurityManager.class);
		setWebSecurityManager(securityManager);
		//给SecurityUtils初始化securityManager
		SecurityUtils.setSecurityManager(securityManager);
		
		FilterChainResolver resolver = createFilterChainResolver();
		if (resolver != null) {
			setFilterChainResolver(resolver);
		}
	}

	private FilterChainResolver createFilterChainResolver() {
		return new PathMatchingFilterChainResolver();
	}

}
