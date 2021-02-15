package com.cory.sevice.base.resource;

import com.cory.util.ClassUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Cory on 2017/5/21.
 */
@Slf4j
@Repository
public class ResourceScanner {

    private static final String DISPATCHER_SERVLET_FILE = "dispatcher-servlet.xml";
    private static final String NODE_NAME = "context:component-scan";
    private static final String ATTR_NAME = "base-package";
    private static final String BASE_CONTROLLER_NAME = "com.cory.web.controller.BaseController";
    private static final String BASE_AJAX_CONTROLLER_NAME = "com.cory.web.controller.BaseAjaxController";

    @Autowired
    private ResourceToDbLoader resourceToDbLoader;

    public void scanAndLoadToDb() {
        log.info("开始扫描并加载资源到数据库...");

        int count = 0;
        String basePkgName = parseControllerBasePkgName();
        String[] pkgs = basePkgName.split(",");
        for (String pkg : pkgs) {
            count += scanPackage(pkg);
        }

        log.info("本次扫描包：{}，共加载{}条数据到数据库.", basePkgName, count);
    }

    private int scanPackage(String pkg) {
        Set<Class<?>> classses = ClassUtil.getClasses(pkg);
        if (CollectionUtils.isEmpty(classses)) {
            return 0;
        }
        //用Set，防止类继承时重复
        Set<String> urls = new HashSet<String>();

        for (Class<?> cls : classses) {
            //处理BaseController
            if (BASE_CONTROLLER_NAME.equals(cls.getName())) {
                continue;
            }
            if (BASE_AJAX_CONTROLLER_NAME.equals(cls.getName())) {
                continue;
            }
            String clsRequestMapping = getClassLevelRequestMapping(cls);
            //继承自BaseController的，默认加BaseController的几个方法
            addBaseControllerMethodIfNeed(cls, urls);

            scanClass(cls, clsRequestMapping, urls);
        }

        return resourceToDbLoader.loadToDb(urls);
    }

    private void scanClass(Class<?> cls, String clsRequestMapping, Set<String> urls) {
        Method[] methods = cls.getMethods();
        if (null == methods || methods.length == 0) {
            return;
        }
        for (Method method : methods) {
            String requestMapping = getMethodLevelRequestMapping(clsRequestMapping, method);
            if (null != requestMapping) {
                requestMapping = requestMapping.replaceAll("\\{.*}", "*");
                urls.add(requestMapping);
            }
        }
    }

    private void addBaseControllerMethodIfNeed(Class<?> cls, Set<String> urls) {
        Class<?> parent = cls.getSuperclass();
        if (null == parent) {
            return;
        }
        if (BASE_CONTROLLER_NAME.equals(parent.getName())) {
            doAddBaseControllerMethod(cls, urls);
        }
        //递归找父类
        addBaseControllerMethodIfNeed(parent, urls);
    }

    private void doAddBaseControllerMethod(Class<?> cls, Set<String> urls) {
        String clsRequestMapping = getClassLevelRequestMapping(cls);
        if (null == clsRequestMapping) {
            clsRequestMapping = "";
        }
        urls.add(clsRequestMapping + "list");
        urls.add(clsRequestMapping + "edit");
        urls.add(clsRequestMapping + "detail/*");
        urls.add(clsRequestMapping + "listData");
        urls.add(clsRequestMapping + "delete/*");
        urls.add(clsRequestMapping + "save");
    }

    private String getMethodLevelRequestMapping(String clsRequestMapping, Method method) {
        RequestMapping requestMapping = AnnotationUtils.getAnnotation(method, RequestMapping.class);
        if (null == requestMapping) {
            return null;
        }

        Object value = AnnotationUtils.getValue(requestMapping, "value");
        if (null == value) {
            return null;
        }
        String methodMapping = null;
        if (value instanceof String) {
            methodMapping = (String) value;
        } else {
            String[] arr = (String[]) value;
            methodMapping = arr[0];
        }

        if (null == methodMapping) {
            return null;
        }
        return (null == clsRequestMapping ? "" : clsRequestMapping) + methodMapping;
    }

    private String getClassLevelRequestMapping(Class<?> cls) {
        RequestMapping clsRequestMapping = cls.getAnnotation(RequestMapping.class);
        if (null == clsRequestMapping) {
            return null;
        }

        Object value = AnnotationUtils.getValue(clsRequestMapping, "value");
        if (null == value) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        } else {
            String[] arr = (String[]) value;
            return arr[0];
        }
    }

    private String parseControllerBasePkgName() {
        //扫描的包，从spring配置文件里取得
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(ResourceScanner.class.getClassLoader().getResourceAsStream(DISPATCHER_SERVLET_FILE));
            Node beans = document.getChildNodes().item(0);
            NodeList list = beans.getChildNodes();
            int len = list.getLength();
            for (int i=0; i<len; i++) {
                Node node = list.item(i);
                if (NODE_NAME.equals(node.getNodeName())) {
                    NamedNodeMap map = node.getAttributes();
                    Node attr = map.getNamedItem(ATTR_NAME);
                    return attr.getNodeValue();
                }
            }
            log.warn("加载{}文件失败，没有找到{}节点，不能扫描Controller并加载resource，请检查.", DISPATCHER_SERVLET_FILE, NODE_NAME);
            return null;
        } catch (Exception e) {
            log.error("加载{}文件失败，请检查.", DISPATCHER_SERVLET_FILE, e);
            throw new RuntimeException(e);
        }
    }
}
