package com.cory.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.cory.enums.CoryEnum;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

/**
 * Created by Cory on 2017/5/13.
 */
@Data
public class CorySystemContext implements Serializable {

    private static final TransmittableThreadLocal<CorySystemContext> THREAD_LOCAL = new TransmittableThreadLocal<>();

    private Set<Class<? extends CoryEnum>> coryEnumSet = new HashSet<>();
    private Set<ModelMeta> modelMetaSet = new HashSet<>();
    /**
     * key: ModelMeta.pageUrl
     * value: ModelMeta
     */
    private Map<String, ModelMeta> modelMetaMap = new HashMap<>();

    private CorySystemContext() {}

    public static CorySystemContext get() {
        CorySystemContext ctx = THREAD_LOCAL.get();
        if (null == ctx) {
            ctx = new CorySystemContext();
            THREAD_LOCAL.set(ctx);
        }
        return ctx;
    }

    /**
     * @see com.cory.db.annotations.Model
     */
    @Data
    @Builder
    public static class ModelMeta implements Serializable {
        private String module;
        private String className;
        private String name;
        //页面的url，规则："/" + module + "/" + simpleClassName.toLowerCase -> /base/systemconfig
        private String pageUrl;
        private List<FieldMeta> fieldList;
    }

    /**
     * @see com.cory.db.annotations.Field
     */
    @Data
    @Builder
    public static class FieldMeta implements Serializable {
        private String label;

        //default TEXT
        //@see com.cory.db.enums.CoryDbType
        private String type;

        //default ""
        private String desc;
        //default true
        private boolean showable;
        //default true
        private boolean editable;
        //default false
        private boolean filtered;

        //default TEXT
        //@see com.cory.db.enums.FilterType
        private String filterType;

        //default ""
        private String filterSelectUrl;
        //default ""
        private String renderName;
        //default 254
        private int len;
        //default false
        private boolean nullable;
    }
}
