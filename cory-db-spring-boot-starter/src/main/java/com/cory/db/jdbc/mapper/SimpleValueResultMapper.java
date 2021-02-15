package com.cory.db.jdbc.mapper;

import java.util.Map;

/**
 * Created by Cory on 2021/2/12.
 */
public abstract class SimpleValueResultMapper extends SingleResultMapper {

    @Override
    protected Object doMap(Map<String, Object> map, Class<?> returnType) {
        return doSimpleMap(map.values().iterator().next(), returnType);
    }

    protected abstract Object doSimpleMap(Object object, Class<?> returnType);
}
