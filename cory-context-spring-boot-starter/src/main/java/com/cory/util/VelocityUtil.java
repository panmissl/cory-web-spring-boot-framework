package com.cory.util;

import org.apache.commons.collections4.MapUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.StringWriter;
import java.util.Map;

/**
 * 使用：VelocityUtil不能像其它util一样直接静态方法，推荐spring autowire后直接使用。也可以自己创建对象使用(主要是要调用init方法)，举例：
 * <pre>
 * VelocityUtil u = new VelocityUtil();
 * u.init();
 * Map<String, Object> map = new HashMap<>();
 * map.put("name", "cory");
 * System.out.println(u.renderByTpl("hi, ${name}", map));
 * </pre>
 *
 * @author cory
 * @date 2024/1/3
 */
@Component
public class VelocityUtil {

    private VelocityEngine velocityEngine;

    @PostConstruct
    public void init() {
        velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();
    }

    /**
     * 用模板字符串渲染
     * @param tpl 模板字符串
     * @param context 上下文数据
     * @return 渲染好的结果
     */
    public String renderByTpl(String tpl, Map<String, Object> context) {
        VelocityContext velocityContext = newVelocityContext(context);
        StringWriter writer = new StringWriter();
        velocityEngine.evaluate(velocityContext, writer, "logTag", tpl);
        return writer.toString();
    }

    /**
     * 用模板文件渲染
     * @param tplFilePath 模板文件的路径：放在classpath下，一般放resource目录，然后写相对路径。比如放在resource/vm/test.vm，则此参数传：vm/test.vm
     * @param context 上下文数据
     * @return 渲染好的结果
     */
    public String renderByFile(String tplFilePath, Map<String, Object> context) {
        VelocityContext velocityContext = newVelocityContext(context);
        Template template = velocityEngine.getTemplate(tplFilePath, "utf-8");
        StringWriter writer = new StringWriter();
        template.merge(velocityContext, writer);
        return writer.toString();
    }

    private VelocityContext newVelocityContext(Map<String, Object> context) {
        VelocityContext velocityContext = new VelocityContext();
        if (MapUtils.isNotEmpty(context)) {
            context.entrySet().forEach(e -> velocityContext.put(e.getKey(), e.getValue()));
        }
        return velocityContext;
    }
}
