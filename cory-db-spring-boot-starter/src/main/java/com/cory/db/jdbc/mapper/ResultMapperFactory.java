package com.cory.db.jdbc.mapper;

import com.cory.util.ClassUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Cory on 2021/2/12.
 */
public class ResultMapperFactory {

    /**
     * @param returnType 返回值类型
     * @param modelClass 对list类型，因为取不到泛型，所以默认用Dao对应的Model类
     * @param selectReturnType 对list类型，支持自定义返回类型，如果指定了自定义类型优先使用，否则使用Model类
     * @return key: mapper, value: 返回类型，如果是list，返回list的泛型(有可能为空)
     */
    public static Pair<ResultMapper, Class<?>> parseMapper(Class<?> returnType, Class<?> modelClass, Class selectReturnType) {
        if (isMapListType(returnType)) {
            return Pair.of(new MapListMapper(), returnType);
        }
        if (returnType.equals(List.class)) {
            //解析不到用modelClass
            Class<?> cls = ClassUtil.parseGenericType(returnType);
            if (null == cls || cls.equals(Object.class)) {
                cls = null != selectReturnType && !Void.class.equals(selectReturnType) ? selectReturnType : modelClass;
            }
            return Pair.of(new ListMapper(), cls);
        }
        if (isSimpleType(returnType)) {
            return Pair.of(new SimpleMapper(), returnType);
        }
        if (isDateType(returnType)) {
            return Pair.of(new DateMapper(), returnType);
        }
        if (isTimestampType(returnType)) {
            return Pair.of(new TimestampMapper(), returnType);
        }
        return Pair.of(new BeanMapper(), returnType);
    }

    private static boolean isMapListType(Class<?> returnType) {
        if (!returnType.equals(List.class)) {
            return false;
        }
        Class genericType = ClassUtil.parseGenericType(returnType);
        return null != genericType && genericType.equals(Map.class);
    }

    private static boolean isTimestampType(Class<?> returnType) {
        return returnType.equals(Timestamp.class);
    }

    private static boolean isDateType(Class<?> returnType) {
        return returnType.equals(Date.class);
    }

    private static boolean isSimpleType(Class<?> returnType) {
        return returnType.isPrimitive() ||
                returnType.equals(Integer.class) ||
                returnType.equals(Long.class) ||
                returnType.equals(Double.class) ||
                returnType.equals(Float.class) ||
                returnType.equals(Short.class) ||
                returnType.equals(Boolean.class) ||
                returnType.equals(String.class);
    }

}
