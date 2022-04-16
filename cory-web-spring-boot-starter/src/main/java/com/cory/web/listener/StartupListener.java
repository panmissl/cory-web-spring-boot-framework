package com.cory.web.listener;

import com.cory.context.CoryEnv;
import com.cory.context.CorySystemContext;
import com.cory.db.annotations.Dao;
import com.cory.db.annotations.Model;
import com.cory.db.processor.CoryDbChecker;
import com.cory.enums.CoryEnum;
import com.cory.service.DatadictService;
import com.cory.service.ResourceService;
import com.cory.service.SystemConfigService;
import com.cory.util.systemconfigcache.SystemConfigCacheKey;
import com.cory.util.systemconfigcache.SystemConfigCacheUtil;
import com.cory.web.util.CodeGeneratorHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.reflections.Reflections;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.util.AnnotatedTypeScanner;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class StartupListener implements ServletContextListener {

	private static final String[] BASE_PACKAGES = new String[] {"com.cory"};

	private WebApplicationContext ctx;

	@Override
    public void contextInitialized(ServletContextEvent event) {
    	ServletContext context = event.getServletContext();
    	this.ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(context);

    	String contextPath = event.getServletContext().getContextPath() + "/";
		SystemConfigCacheUtil.refresh(SystemConfigCacheKey.CONTEXT_PATH, contextPath);

		String staticResourcePath = event.getServletContext().getRealPath("static");
		SystemConfigCacheUtil.refresh(SystemConfigCacheKey.STATIC_RESOURCE_PATH, staticResourcePath);

		//先生成代码，否则加载系统参数出错
		generateCode(ctx);
		initForEnum();

		//在使用db前先同步一下表，否则开发环境一开始启动时会有问题：直接取CoryDbChecker即可，初始化Bean会自动检查
		ctx.getBean(CoryDbChecker.class);

    	initSystemConfigCache(ctx);
    	initDataDictCache(ctx);
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

				System.out.println("代码生成模块，您选择了：" + (generateAll ? "生成所有代码" : generatePart ? "生成Service&Dao代码" : "不生成"));

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
		return CodeGeneratorHelper.generateCode(modelClass, generateAll ? CodeGeneratorHelper.GenerateCodeController.all() : CodeGeneratorHelper.GenerateCodeController.serviceAndDaoOnly());
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

	private void initDataDictCache(WebApplicationContext ctx) {
		DatadictService dataDictService = ctx.getBean(DatadictService.class);
		dataDictService.refreshCache();
	}
}
