package com.cory.db.jdbc.mapper;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by Cory on 2021/2/12.
 */
public abstract class SingleResultMapper extends BaseResultMapper {

    @Override
    public Object map(List<Map<String, Object>> listData, Class<?> returnType) {
        if (CollectionUtils.isEmpty(listData)) {
            return null;
        }
        Map<String, Object> map = listData.get(0);
        if (MapUtils.isEmpty(map)) {
            return null;
        }
        return doMap(map, returnType);
    }

    protected abstract Object doMap(Map<String, Object> map, Class<?> returnType);
}
