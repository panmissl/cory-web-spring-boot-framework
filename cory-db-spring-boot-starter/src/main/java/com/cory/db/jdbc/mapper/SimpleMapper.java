package com.cory.db.jdbc.mapper;

/**
 * Created by Cory on 2021/2/12.
 */
public class SimpleMapper extends SimpleValueResultMapper {

    @Override
    protected Object doSimpleMap(Object object, Class<?> returnType) {
        return object;
    }
}
