package com.cory.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.cory.enums.CoryEnum;
import com.cory.vo.DataDictVO;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.*;

/**
 * Created by Cory on 2017/5/13.
 */
@Data
public class CorySystemContext implements Serializable {

    private static final TransmittableThreadLocal<CorySystemContext> THREAD_LOCAL = new TransmittableThreadLocal<>();

    private static final long serialVersionUID = 5331922271790377155L;

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
        private boolean createable;
        private boolean updateable;
        private boolean deleteable;
        @Builder.Default
        private List<FieldMeta> fieldList = new ArrayList<>();

        @Override
        public int hashCode() {
            return className.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || !(o instanceof ModelMeta)) {
                return false;
            }
            return className.equals(((ModelMeta)o).getClassName());
        }
    }

    /**
     * @see com.cory.db.annotations.Field
     */
    @Data
    @Builder
    public static class FieldMeta implements Serializable {
        // id, code, ...
        private String name;
        private String label;

        //default TEXT
        //@see com.cory.db.enums.CoryDbType
        private String type;

        //java field type
        private Class<?> javaType;

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
        //private String filterSelectUrl;

        //default ""
        private String renderName;
        //default 254
        private int len;
        //default false
        private boolean nullable;
        //default false
        private boolean richText;
        //default false
        private boolean code;
        //default null
        private String datadictTypeValue;
        //default null：如果有datadictTypeValue，则根据类型取出列表，放到此字段里，前端直接渲染
        private List<DataDictVO> dataDictList;
        //default false
        private boolean updateable;

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || !(o instanceof FieldMeta)) {
                return false;
            }
            return name.equals(((FieldMeta)o).getName());
        }
    }

    @Data
    @Builder
    public static class EnumMeta implements Serializable {
        private String className;
        //[{value: {label: xx, order: 1}]
        @Builder.Default
        private Map<String, Pair<String, Integer>> valueLabelOrderMap = new HashMap<>();

        @Override
        public int hashCode() {
            return className.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || !(o instanceof EnumMeta)) {
                return false;
            }
            return className.equals(((EnumMeta)o).getClassName());
        }
    }
}
