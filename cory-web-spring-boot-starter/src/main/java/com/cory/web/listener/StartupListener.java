package com.cory.web.listener;

import com.cory.cache.manager.CoryCacheManager;
import com.cory.context.CoryContext;
import com.cory.context.CoryEnv;
import com.cory.context.CorySystemContext;
import com.cory.db.annotations.Dao;
import com.cory.db.annotations.Model;
import com.cory.enums.CoryEnum;
import com.cory.service.ResourceService;
import com.cory.service.SystemConfigService;
import com.cory.util.systemconfigcache.SystemConfigCacheKey;
import com.cory.util.systemconfigcache.SystemConfigCacheUtil;
import com.cory.web.util.CodeGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.reflections.Reflections;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.util.AnnotatedTypeScanner;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class StartupListener implements ServletContextListener {

	private static final String[] BASE_PACKAGES = new String[] {"com.cory"};

	private WebApplicationContext ctx;
	
    public void contextInitialized(ServletContextEvent event) {
    	ServletContext context = event.getServletContext();
    	this.ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(context);

		SystemConfigCacheUtil.setCacheManager(ctx.getBean("coryCacheManager", CoryCacheManager.class));

    	String contextPath = event.getServletContext().getContextPath() + "/";
		SystemConfigCacheUtil.refresh(SystemConfigCacheKey.CONTEXT_PATH, contextPath);

		String staticResourcePath = event.getServletContext().getRealPath("static");
		SystemConfigCacheUtil.refresh(SystemConfigCacheKey.STATIC_RESOURCE_PATH, staticResourcePath);

		//先生成代码，否则加载系统参数出错
		generateCode(ctx);
		initForEnum();
    	initSystemConfigCache(ctx);
		scanResourceAndLoadToDb(ctx);
    }

	private void generateCode(WebApplicationContext ctx) {
    	/*
    	生成代码
    	1、开发环境才生成
    	2、生成规则：有Model，但是没有Dao
    	3、生成文件：Controller、Service、
    	*/
    	if (!CoryEnv.IS_DEV) {
    		return;
		}

		AnnotatedTypeScanner modelScanner = new AnnotatedTypeScanner(true, Model.class);
		Set<Class<?>> modelSet = modelScanner.findTypes(BASE_PACKAGES);
		AnnotatedTypeScanner daoScanner = new AnnotatedTypeScanner(true, Dao.class);
		Set<Class<?>> daoSet = daoScanner.findTypes(BASE_PACKAGES);

		if (CollectionUtils.isEmpty(modelSet)) {
			return;
		}
		modelSet = modelSet.stream().filter(m -> !hasDao(m, daoSet)).collect(Collectors.toSet());
		if (CollectionUtils.isEmpty(modelSet)) {
			return;
		}
		int count = 0;
		for (Class<?> modelClass : modelSet) {
			System.out.println();
			System.out.println("代码生成模块：检测到：" + modelClass.getSimpleName() + "没有Dao类，是否生成代码？");
			System.out.println("1、生成所有代码（Controller、Service、Dao、JS）（默认）");
			System.out.println("2、生成Service代码（Service、Dao）");
			System.out.println("3、不生成");
			System.out.println("请选择：1/2/3。输入选择的数字后回车。");
			try {
				String line = new BufferedReader(new InputStreamReader(System.in)).readLine();
				boolean generatePart = null != line && "2".equals(line.trim());
				boolean generateNone = null != line && "3".equals(line.trim());
				boolean generateAll = !generatePart && !generateNone;

				System.out.println("代码生成模块，您选择了：" + (generateAll ? "生成所有代码" : generatePart ? "生成Service代码" : "不生成"));

				count += doGenerate(modelClass, generateAll, generatePart);
			} catch (Throwable t) {
				t.printStackTrace();
				System.exit(0);
			}
		}
		if (count > 0) {
			System.out.println("代码生成模块，代码生成完成，请重新build工程然后启动(如果有初始化数据，请在启动前将数据初始化)。");
			System.exit(0);
		}
	}

	private int doGenerate(Class<?> modelClass, boolean generateAll, boolean generatePart) {
    	if (!generateAll && !generatePart) {
			return 0;
		}
		CodeGenerator codeGenerator = new CodeGenerator(modelClass);
		//service, dao
		codeGenerator.generateDao();
		codeGenerator.generateService();
		//controller, js
		if (generateAll) {
			codeGenerator.generateController();
			codeGenerator.generateJs();
		}
		System.out.println("已经为：" + modelClass.getSimpleName() + "生成" + (generateAll ? "Controller、Service、Dao及JS" : "Service及Dao"));
		System.out.println();
		return 1;
	}

	private boolean hasDao(Class<?> m, Set<Class<?>> daoSet) {
    	if (CollectionUtils.isEmpty(daoSet)) {
    		return false;
		}
		for (Class<?> dao : daoSet) {
    		Dao daoAnno = dao.getAnnotation(Dao.class);
			if (null != daoAnno && daoAnno.model().equals(m)) {
				return true;
			}
		}
		return false;
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
