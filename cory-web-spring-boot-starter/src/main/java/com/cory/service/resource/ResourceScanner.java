package com.cory.service.resource;

import com.cory.constant.Constants;
import com.cory.context.CorySystemContext;
import com.cory.context.CorySystemContext.FieldMeta;
import com.cory.context.CorySystemContext.ModelMeta;
import com.cory.db.annotations.Model;
import com.cory.db.enums.CoryDbType;
import com.cory.util.datadictcache.DataDictCacheUtil;
import com.cory.vo.DataDictVO;
import com.cory.web.controller.BaseAjaxController;
import com.cory.web.controller.BaseOpenApiController;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.util.AnnotatedTypeScanner;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Cory on 2017/5/21.
 */
@Slf4j
@Repository
public class ResourceScanner {

    private static final String AJAX = "/ajax";
    private static final String SLASH = "/";
    private static final String[] BASE_PACKAGES = new String[] {"com.cory"};

    @Autowired
    private ResourceToDbLoader resourceToDbLoader;

    public void scanAndLoadToDb() {
        log.info("start to scan Controllers ...");

        AnnotatedTypeScanner scanner1 = new AnnotatedTypeScanner(true, Controller.class);
        AnnotatedTypeScanner scanner2 = new AnnotatedTypeScanner(true, RestController.class);

        Set<Class<?>> set1 = scanner1.findTypes(BASE_PACKAGES);
        Set<Class<?>> set2 = scanner2.findTypes(BASE_PACKAGES);

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

        log.info("scan Controllers finish, url count: {}", count);

        doScanModel(true);
    }

    /**
     * 刷新模型元数据。比如：当数据字典有变化的时候，要刷新一下，否则取到的数据还是老的
     */
    public void refreshModelMeta() {
        doScanModel(false);
    }

    private void doScanModel(boolean loadUrl2Db) {
        log.info("start to scan Model ...");

        AnnotatedTypeScanner modelScanner = new AnnotatedTypeScanner(true, Model.class);
        Set<Class<?>> modelSet = modelScanner.findTypes(BASE_PACKAGES);

        if (CollectionUtils.isEmpty(modelSet)) {
            log.info("scan Model finish, no Model found");
            return;
        }

        Set<ModelMeta> modelMetaSet = new HashSet<>();
        Map<String, ModelMeta> modelMetaMap = new HashMap<>();

        Set<String> urls = modelSet.stream().map(model -> {
            // "/" + model.getmodule + "/" + model.getSimpleName().toLowerCase() -> "/base/systemconfig"
            Model anno = model.getAnnotation(Model.class);
            String url = SLASH + anno.module() + SLASH + model.getSimpleName().toLowerCase();

            log.info("scan model: {}", model.getName());

            if (!anno.noTable()) {
                List<FieldMeta> fieldMetaList = new ArrayList<>();
                Field[] fields = model.getDeclaredFields();
                if (null != fields && fields.length > 0) {
                    for (Field field : fields) {
                        com.cory.db.annotations.Field fieldAnno = field.getAnnotation(com.cory.db.annotations.Field.class);
                        if (null == fieldAnno) {
                            continue;
                        }
                        String renderName = fieldAnno.renderName();
                        if (StringUtils.isBlank(renderName) && (CoryDbType.ENUM.equals(fieldAnno.type())) || CoryDbType.BOOLEAN.equals(fieldAnno.type()) || CoryDbType.DATETIME.equals(fieldAnno.type()) || CoryDbType.DATE.equals(fieldAnno.type())) {
                            renderName = field.getName() + "Text";
                        }

                        String datadictTypeValue = fieldAnno.datadictTypeValue();
                        List<DataDictVO> dataDictList = parseDataDictList(datadictTypeValue);

                        FieldMeta fieldMeta = FieldMeta.builder()
                                .name(field.getName())
                                .type(fieldAnno.type().name())
                                .javaType(field.getType())
                                .showable(fieldAnno.showable())
                                .renderName(renderName)
                                .nullable(fieldAnno.nullable())
                                .len(fieldAnno.len())
                                .label(fieldAnno.label())
                                .filterType(fieldAnno.filterType().name())
                                //.filterSelectUrl(fieldAnno.filterSelectUrl())
                                .filtered(fieldAnno.filtered())
                                .desc(fieldAnno.desc())
                                .richText(fieldAnno.richText())
                                .code(fieldAnno.code())
                                .datadictTypeValue(datadictTypeValue)
                                .dataDictList(dataDictList)
                                .updateable(fieldAnno.updateable())
                                .build();
                        fieldMetaList.add(fieldMeta);
                    }
                }
                ModelMeta modelMeta = ModelMeta.builder()
                        .className(model.getName())
                        .fieldList(fieldMetaList)
                        .module(anno.module())
                        .name(anno.name())
                        .createable(anno.createable())
                        .updateable(anno.updateable())
                        .deleteable(anno.deleteable())
                        .pageUrl(url)
                        .build();
                modelMetaSet.add(modelMeta);
                modelMetaMap.put(url, modelMeta);
            }

            return url;
        }).collect(Collectors.toSet());

        if (loadUrl2Db) {
            Set<String> additional = new HashSet<>();
            urls.forEach(u -> {
                if (!u.contains("*")) {
                    additional.add(u + "**");
                }
            });
            urls.addAll(additional);

            resourceToDbLoader.loadToDb(urls);
        }

        CorySystemContext.get().setModelMetaSet(modelMetaSet);
        CorySystemContext.get().setModelMetaMap(modelMetaMap);
        log.info("scan Model finish, model count: {}", modelSet.size());
    }

    private List<DataDictVO> parseDataDictList(String datadictTypeValue) {
        if (StringUtils.isBlank(datadictTypeValue)) {
            return Lists.newArrayList();
        }
        List<DataDictCacheUtil.DataDict> ddList = DataDictCacheUtil.getByType(datadictTypeValue);
        if (CollectionUtils.isEmpty(ddList)) {
            ddList = Lists.newArrayList();
        }
        //对于根类型，将ROOT也添加一下
        if (Constants.DATA_DICT_ROOT_VALUE.equals(datadictTypeValue)) {
            ddList.add(0, DataDictCacheUtil.getByValue(Constants.DATA_DICT_ROOT_PARENT_VALUE, Constants.DATA_DICT_ROOT_VALUE));
        }
        return ddList.stream()
                .map(dd -> DataDictVO.builder().value(dd.getValue()).description(dd.getDescription()).sn(dd.getSn()).build())
                .collect(Collectors.toList());
    }

    private int doScan(Class<?> cls) {
        if (cls.equals(BaseAjaxController.class)) {
            return 0;
        }
        boolean isAjax = BaseAjaxController.class.isAssignableFrom(cls);
        String clsRequestMapping = getClassLevelRequestMapping(cls);

        log.info("san controller: {}", cls.getName());

        return resourceToDbLoader.loadToDb(scanClass(isAjax, cls, clsRequestMapping));
    }

    private Set<String> scanClass(boolean isAjax, Class<?> cls, String clsRequestMapping) {
        Set<String> urls = new HashSet<>();

        Method[] methods = cls.getMethods();
        if (null == methods || methods.length == 0) {
            return urls;
        }
        for (Method method : methods) {
            Arrays.asList(RequestMapping.class, GetMapping.class, PostMapping.class, DeleteMapping.class, PatchMapping.class, PutMapping.class).forEach(anno -> {
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
            //不能加ajax的前缀了，因为现在是controller里自己写全了
            //methodMapping[i] = (isAjax ? AJAX : "") + (null == clsRequestMapping ? "" : clsRequestMapping) + methodMapping[i];
            methodMapping[i] = (null == clsRequestMapping ? "" : clsRequestMapping) + methodMapping[i];
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
