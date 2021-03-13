package com.cory.web.util;

import com.cory.constant.Constants;
import com.cory.db.annotations.Model;
import com.cory.model.Feedback;
import com.cory.util.DateUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CodeGenerator {

	/** Model */
	private static final String CONTEXT_BIG_MODEL = "#MODEL#";
	/** model */
	private static final String CONTEXT_SMALL_MODEL = "#model#";
	/** 2020/05/10 */
	private static final String CONTEXT_DATE = "#NOW#";
	private static final String CONTEXT_MODULE = "#module#";

	private static final String DATE_FORMAT = "yyyy/MM/dd";

	private static final String DAO_TPL =
			"package com.cory.dao;\n" +
			"\n" +
			"import com.cory.db.annotations.Dao;\n" +
			"import com.cory.model.#MODEL#;\n" +
			"\n" +
			"/**\n" +
			" * generated by CodeGenerator on #NOW#.\n" +
			" */\n" +
			"@Dao(model = #MODEL#.class)\n" +
			"public interface #MODEL#Dao extends BaseDao<#MODEL#> {}\n";

	private static final String SERVICE_TPL =
			"package com.cory.service;\n" +
			"\n" +
			"import com.cory.dao.#MODEL#Dao;\n" +
			"import com.cory.model.#MODEL#;\n" +
			"import org.springframework.beans.factory.annotation.Autowired;\n" +
			"import org.springframework.context.annotation.Scope;\n" +
			"import org.springframework.context.annotation.ScopedProxyMode;\n" +
			"import org.springframework.stereotype.Service;\n" +
			"import org.springframework.transaction.annotation.Transactional;\n" +
			"\n" +
			"/**\n" +
			" * generated by CodeGenerator on #NOW#.\n" +
			" */\n" +
			"@Service\n" +
			"@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)\n" +
			"@Transactional\n" +
			"public class #MODEL#Service extends BaseService<#MODEL#> {\n" +
			"\n" +
			"\t@Autowired\n" +
			"\tprivate #MODEL#Dao #model#Dao;\n" +
			"\n" +
			"\tpublic #MODEL#Dao getDao() {\n" +
			"\t\treturn #model#Dao;\n" +
			"\t}\n" +
			"}\n";

	private static final String CONTROLLER_TPL =
			"package com.cory.controller;\n" +
			"\n" +
			"import com.cory.model.#MODEL#;\n" +
			"import com.cory.service.#MODEL#Service;\n" +
			"import com.cory.web.controller.BaseAjaxController;\n" +
			"import org.springframework.beans.factory.annotation.Autowired;\n" +
			"import org.springframework.web.bind.annotation.RequestMapping;\n" +
			"import org.springframework.web.bind.annotation.RestController;\n" +
			"\n" +
			"/**\n" +
			" * generated by CodeGenerator on #NOW#.\n" +
			" */\n" +
			"@RestController\n" +
			"@RequestMapping(\"/ajax/#module#/#model#/\")\n" +
			"public class #MODEL#Controller extends BaseAjaxController<#MODEL#> {\n" +
			"\n" +
			"\t@Autowired\n" +
			"\tprivate #MODEL#Service #model#Service;\n" +
			"\n" +
			"\tpublic #MODEL#Service getService() {\n" +
			"\t\treturn #model#Service;\n" +
			"\t}\n" +
			"}\n";

	private static final String JS_TPL =
			"import TableList from '@/components/TableList';\n" +
			"import { PageContainer } from '@ant-design/pro-layout';\n" +
			"import React from 'react';\n" +
			"\n" +
			"const Page = () => {\n" +
			"  return (\n" +
			"    <PageContainer>\n" +
			"      <TableList model=\"com.cory.model.#MODEL#\" showId={true} />\n" +
			"    </PageContainer>\n" +
			"  );\n" +
			"};\n" +
			"\n" +
			"export default Page;\n";

	private String modelName;
	private Map<String, String> context = new HashMap<>();

	private File javaRoot;
	private File jsRoot;

	private File controllerRoot;
	private File serviceRoot;
	private File daoRoot;

	public CodeGenerator(Class<?> modelClass) {
		try {
			this.modelName = modelClass.getSimpleName();
			Model model = modelClass.getAnnotation(Model.class);

			context.put(CONTEXT_BIG_MODEL, modelName);
			context.put(CONTEXT_SMALL_MODEL, modelName.substring(0, 1).toLowerCase() + modelName.substring(1));
			context.put(CONTEXT_DATE, DateUtils.format(new Date(), DATE_FORMAT));
			context.put(CONTEXT_MODULE, model.module());

			javaRoot = new File(modelClass.getClassLoader().getResource("").toURI());
			//target\classes
			if (javaRoot.getName().equals("classes")) {
				javaRoot = javaRoot.getParentFile();
			}
			if (javaRoot.getName().equals("target")) {
				javaRoot = javaRoot.getParentFile();
			}

			System.out.println("自动检测到工程根目录为：" + javaRoot.getAbsolutePath() + "，如果正确请按回车，如果不正确请输入正确的根目录(绝对路径)然后回车。");
			try {
				String line = new BufferedReader(new InputStreamReader(System.in)).readLine();
				if (StringUtils.isNotBlank(line)) {
					javaRoot = new File(line);
				}
				System.out.println("工程根目录为：" + javaRoot.getAbsolutePath());
			} catch (Throwable t) {
				t.printStackTrace();
				System.exit(0);
			}

			jsRoot = new File(javaRoot.getAbsolutePath() + "-cdn");
			jsRoot = new File(jsRoot, "src/pages/" + model.module());
			jsRoot.mkdirs();

			controllerRoot = new File(javaRoot, "src/main/java/com/cory/controller");
			serviceRoot = new File(javaRoot, "src/main/java/com/cory/service");
			daoRoot = new File(javaRoot, "src/main/java/com/cory/dao");
			controllerRoot.mkdirs();
			serviceRoot.mkdirs();
			daoRoot.mkdirs();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void generateDao() {
		generateFile(daoRoot, modelName + "Dao.java", DAO_TPL);
	}

	public void generateService() {
		generateFile(serviceRoot, modelName + "Service.java", SERVICE_TPL);
	}

	public void generateController() {
		generateFile(controllerRoot, modelName + "Controller.java", CONTROLLER_TPL);
	}

	public void generateJs() {
		generateFile(jsRoot, modelName + ".js", JS_TPL);
	}

	private void generateFile(File root, String fileName, String tpl) {
		try {
			for (Map.Entry<String, String> entry : context.entrySet()) {
				tpl = tpl.replaceAll(entry.getKey(), entry.getValue());
			}

			File file = new File(root, fileName);
			file.createNewFile();

			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), Constants.UTF8));
			writer.write(tpl);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}
