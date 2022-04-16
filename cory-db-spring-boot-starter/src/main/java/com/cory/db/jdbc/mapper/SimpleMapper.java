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
            if (returnType.equals(Integer.class) || returnType.getName().equals("int")) {
                return 0;
            }
            if (returnType.equals(Long.class) || returnType.getName().equals("long")) {
                return 0;
            }
            if (returnType.equals(Double.class) || returnType.getName().equals("double")) {
                return 0;
            }
            if (returnType.equals(Float.class) || returnType.getName().equals("float")) {
                return 0;
            }
            if (returnType.equals(Short.class) || returnType.getName().equals("short")) {
                return 0;
            }
            if (returnType.equals(Boolean.class) || returnType.getName().equals("boolean")) {
                return false;
            }
            return null;
        }

        if (object instanceof Number) {
            Number number = (Number) object;
            if (returnType.equals(Integer.class) || returnType.getName().equals("int")) {
                return number.intValue();
            }
            if (returnType.equals(Long.class) || returnType.getName().equals("long")) {
                return number.longValue();
            }
            if (returnType.equals(Double.class) || returnType.getName().equals("double")) {
                return number.doubleValue();
            }
            if (returnType.equals(Float.class) || returnType.getName().equals("float")) {
                return number.floatValue();
            }
            if (returnType.equals(Short.class) || returnType.getName().equals("short")) {
                return number.shortValue();
            }
        }

        if (returnType.equals(Boolean.class) || returnType.getName().equals("boolean")) {
            String objStr = object.toString();
            //true/TRUE -> true, 非0：true，其它false
            return objStr.equalsIgnoreCase("true") || !objStr.equals("0");
        }

        return object;
    }
}
