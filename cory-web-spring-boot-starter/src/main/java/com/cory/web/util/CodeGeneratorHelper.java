package com.cory.web.util;

import com.cory.context.CoryEnv;
import com.cory.db.annotations.Model;
import com.cory.util.AssertUtils;

public class CodeGeneratorHelper {

	public static class GenerateCodeController {
		public boolean controller;
		public boolean service;
		public boolean dao;
		public boolean js;

		private GenerateCodeController(boolean controller, boolean service, boolean dao, boolean js) {
			this.controller = controller;
			this.service = service;
			this.dao = dao;
			this.js = js;
		}

		public static GenerateCodeController all() {
			return new GenerateCodeController(true, true, true, true);
		}

		public static GenerateCodeController serviceAndDaoOnly() {
			return new GenerateCodeController(false, true, true, false);
		}

		public static GenerateCodeController controllerOnly() {
			return new GenerateCodeController(true, false, false, false);
		}

		public static GenerateCodeController serviceOnly() {
			return new GenerateCodeController(false, true, false, false);
		}

		public static GenerateCodeController daoOnly() {
			return new GenerateCodeController(false, false, true, false);
		}

		public static GenerateCodeController jsOnly() {
			return new GenerateCodeController(false, false, false, true);
		}
	}

	/**
	 * 为某个Model类生成代码
	 * @param modelClass model类，带有{@link @Model}注解的类
	 * @param controller 生成控制器
	 * @return 0：未生成，1：生成（可能是部分代码）
	 */
	public static int generateCode(Class<?> modelClass, GenerateCodeController controller) {
		AssertUtils.isTrue(CoryEnv.IS_DEV, "CODE_GENERATOR_ERROR", "非开发环境不能生成代码");
		Model model = modelClass.getAnnotation(Model.class);
		AssertUtils.notNull(model, "CODE_GENERATOR_ERROR", "model类必须带有@Model注解");

		if (!controller.controller && !controller.service && !controller.dao && !controller.js) {
			return 0;
		}

		String result = "";
		CodeGenerator codeGenerator = new CodeGenerator(modelClass);
		if (controller.controller) {
			codeGenerator.generateController();
			result += "Controller、";
		}
		if (controller.service) {
			codeGenerator.generateService();
			result += "Service、";
		}
		if (controller.dao) {
			codeGenerator.generateDao();
			result += "Dao、";
		}
		if (controller.js) {
			codeGenerator.generateJs();
			result += "JS、";
		}
		if (result.length() > 0) {
			result = result.substring(0, result.length() - 1);
		}
		System.out.println("已经为：" + modelClass.getSimpleName() + "生成" + result);
		System.out.println();
		return 1;
	}

}
