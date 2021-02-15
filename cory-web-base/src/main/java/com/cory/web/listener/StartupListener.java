package com.cory.web.listener;

import com.cory.sevice.base.ResourceService;
import com.cory.sevice.base.SystemConfigService;
import com.cory.util.systemconfigcache.SystemConfigCacheKey;
import com.cory.util.systemconfigcache.SystemConfigCacheUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

@Configuration
public class StartupListener implements ServletContextListener {
	
	private WebApplicationContext ctx;
	
    public void contextInitialized(ServletContextEvent event) {
    	ServletContext context = event.getServletContext();
    	this.ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(context);

    	String contextPath = event.getServletContext().getContextPath() + "/";
		SystemConfigCacheUtil.refresh(SystemConfigCacheKey.CONTEXT_PATH, contextPath);

		String staticResourcePath = event.getServletContext().getRealPath("static");
		SystemConfigCacheUtil.refresh(SystemConfigCacheKey.STATIC_RESOURCE_PATH, staticResourcePath);

    	initSystemConfigCache(ctx);
		scanResourceAndLoadToDb(ctx);
    }

	private void scanResourceAndLoadToDb(WebApplicationContext ctx) {
    	ctx.getBean(ResourceService.class).scanResourceAndLoadToDb();
	}

	private void initSystemConfigCache(WebApplicationContext ctx) {
		SystemConfigService systemConfigService = ctx.getBean(SystemConfigService.class);
		systemConfigService.refreshCache();
	}
}
