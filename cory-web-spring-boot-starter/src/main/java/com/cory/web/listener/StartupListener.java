package com.cory.web.listener;

import com.cory.context.CorySystemContext;
import com.cory.enums.CoryEnum;
import com.cory.service.ResourceService;
import com.cory.service.SystemConfigService;
import com.cory.util.systemconfigcache.SystemConfigCacheKey;
import com.cory.util.systemconfigcache.SystemConfigCacheUtil;
import org.reflections.Reflections;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Set;

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

		initForEnum();
    	initSystemConfigCache(ctx);
		scanResourceAndLoadToDb(ctx);
    }

	private void initForEnum() {
		Reflections reflections = new Reflections("com.cory");
		Set<Class<? extends CoryEnum>> coryEnumSet = reflections.getSubTypesOf(CoryEnum.class);
		CorySystemContext.get().setCoryEnumSet(coryEnumSet);
	}

	private void scanResourceAndLoadToDb(WebApplicationContext ctx) {
    	ctx.getBean(ResourceService.class).scanResourceAndLoadToDb();
	}

	private void initSystemConfigCache(WebApplicationContext ctx) {
		SystemConfigService systemConfigService = ctx.getBean(SystemConfigService.class);
		systemConfigService.refreshCache();
	}
}
