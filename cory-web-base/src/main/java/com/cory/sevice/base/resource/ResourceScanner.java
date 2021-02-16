package com.cory.sevice.base.resource;

import com.cory.web.controller.BaseAjaxController;
import com.cory.web.controller.BaseOpenApiController;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.util.AnnotatedTypeScanner;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Cory on 2017/5/21.
 */
@Slf4j
@Repository
public class ResourceScanner {

    private static final String AJAX = "/ajax";

    @Autowired
    private ResourceToDbLoader resourceToDbLoader;

    public void scanAndLoadToDb() {
        log.info("start to scan Controllers ...");

        AnnotatedTypeScanner scanner1 = new AnnotatedTypeScanner(true, Controller.class);
        AnnotatedTypeScanner scanner2 = new AnnotatedTypeScanner(true, RestController.class);

        Set<Class<?>> set1 = scanner1.findTypes("com.cory");
        Set<Class<?>> set2 = scanner2.findTypes("com.cory");

        Set<Class<?>> set = new HashSet<>();
        if (CollectionUtils.isNotEmpty(set1)) {
            set.addAll(set1);
        }
        if (CollectionUtils.isNotEmpty(set2)) {
            set.addAll(set2);
        }
        int count = 0;
        if (CollectionUtils.isNotEmpty(set)) {
            count = set.stream().filter(cls -> !BaseOpenApiController.class.isAssignableFrom(cls)).map(cls -> doScan(cls)).reduce(0, (i1, i2) -> i1 + i2);
        }

        log.info("scan Controllers finish, url count: {} ...", count);
    }

    private int doScan(Class<?> cls) {
        if (cls.equals(BaseAjaxController.class)) {
            return 0;
        }
        boolean isAjax = BaseAjaxController.class.isAssignableFrom(cls);
        String clsRequestMapping = getClassLevelRequestMapping(cls);
        return resourceToDbLoader.loadToDb(scanClass(isAjax, cls, clsRequestMapping));
    }

    private Set<String> scanClass(boolean isAjax, Class<?> cls, String clsRequestMapping) {
        Set<String> urls = new HashSet<>();

        Method[] methods = cls.getMethods();
        if (null == methods || methods.length == 0) {
            return urls;
        }
        for (Method method : methods) {
            Arrays.asList(RequestMapping.class, GetMapping.class, PostMapping.class).forEach(anno -> {
                String[] requestMapping = getMethodLevelRequestMapping(isAjax, clsRequestMapping, method, anno);
                if (null != requestMapping && requestMapping.length > 0) {
                    for (String m : requestMapping) {
                        m = m.replaceAll("\\{.*}", "*");
                        urls.add(m);
                    }
                }
            });
        }
        return urls;
    }

    private String[] getMethodLevelRequestMapping(boolean isAjax, String clsRequestMapping, Method method, Class annotationClass) {
        Annotation requestMapping = AnnotationUtils.getAnnotation(method, annotationClass);
        if (null == requestMapping) {
            return null;
        }

        Object value = AnnotationUtils.getValue(requestMapping, "value");
        if (null == value) {
            return null;
        }
        String[] methodMapping;
        if (value instanceof String) {
            methodMapping = new String[] {(String) value};
        } else {
            methodMapping = (String[]) value;
        }
        if (null == methodMapping || methodMapping.length == 0) {
            return null;
        }
        for (int i = 0; i < methodMapping.length; i++) {
            methodMapping[i] = (isAjax ? AJAX : "") + (null == clsRequestMapping ? "" : clsRequestMapping) + methodMapping[i];
        }
        return methodMapping;
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

}
