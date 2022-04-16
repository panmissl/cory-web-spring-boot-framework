package com.cory.util.datadictcache;

import com.cory.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cache.CacheManager;

/**
 * Created by Cory on 2017/5/14.
 */
public class DataDictCacheUtil {

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataDict extends BaseDTO {

        private static final long serialVersionUID = 1602508531529313312L;

        private Integer id;

        /** 数据字典分类 */
        private String type;

        /** 数据字典分类名称 */
        private String typeDesc;

        /** 数据字典值 */
        private String value;

        /** 数据字典排序 */
        private Integer sn;

        /** 数据字典描述 */
        private String description;

        /** 是否显示 */
        private Boolean showable;

    }

    private static final String CACHE_NAME_BY_ID = "DataDictCacheUtil_BY_ID";
    private static final String CACHE_NAME_BY_VALUE = "DataDictCacheUtil_BY_VALUE";

    private static CacheManager cacheManager;

    private DataDictCacheUtil() {}

    public static DataDict get(int id) {
        return cacheManager.getCache(CACHE_NAME_BY_ID).get(id, DataDict.class);
    }

    public static DataDict getByValue(String type, String value) {
        return cacheManager.getCache(CACHE_NAME_BY_VALUE).get(buildByValueKey(type, value), DataDict.class);
    }

    public static void refresh(DataDict dd) {
        if (null == dd) {
            return;
        }
        cacheManager.getCache(CACHE_NAME_BY_ID).put(dd.getId(), dd);
        cacheManager.getCache(CACHE_NAME_BY_VALUE).put(buildByValueKey(dd.getType(), dd.getValue()), dd);
    }

    public static void setCacheManager(CacheManager cacheManager) {
        DataDictCacheUtil.cacheManager = cacheManager;
    }

    private static String buildByValueKey(String type, String value) {
        return type + "__" + value;
    }
}
