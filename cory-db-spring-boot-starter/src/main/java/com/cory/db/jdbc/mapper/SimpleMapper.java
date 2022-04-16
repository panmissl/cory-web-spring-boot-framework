package com.cory.db.jdbc.mapper;

/**
 * Created by Cory on 2021/2/12.
 */
public class SimpleMapper extends SimpleValueResultMapper {

    @Override
    protected Object doSimpleMap(Object object, Class<?> returnType) {
        /*
        returnType.isPrimitive() ||
        returnType.equals(Integer.class) ||
        returnType.equals(Long.class) ||
        returnType.equals(Double.class) ||
        returnType.equals(Float.class) ||
        returnType.equals(Short.class) ||
        returnType.equals(Boolean.class) ||
        returnType.equals(String.class);
        */
        if (null == object) {
            return null;
        }
        if (object instanceof Number) {
            Number number = (Number) object;
            if (returnType.equals(Integer.class)) {
                return number.intValue();
            }
            if (returnType.equals(Long.class)) {
                return number.longValue();
            }
            if (returnType.equals(Double.class)) {
                return number.doubleValue();
            }
            if (returnType.equals(Float.class)) {
                return number.floatValue();
            }
            if (returnType.equals(Short.class)) {
                return number.shortValue();
            }
        }
        return object;
    }
}
