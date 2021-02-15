package com.cory.util;

import com.cory.constant.ErrorCode;
import com.cory.exception.CoryException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Map;

/**
 * @author Cory Pan
 */
public class AssertUtils {

    public static void isTrue(boolean expression, String message) {
        isTrue(expression, message, ErrorCode.GENERIC_ERROR);
    }

    public static void isTrue(boolean expression, String message, ErrorCode errorCode) {
        if (!expression) {
            error(message, errorCode);
        }
    }

    public static void isNull(Object object, String message) {
        isNull(object, message, ErrorCode.GENERIC_ERROR);
    }

    public static void isNull(Object object, String message, ErrorCode errorCode) {
        isTrue(null == object, message, errorCode);
    }

    public static void notNull(Object object, String message) {
        notNull(object, message, ErrorCode.GENERIC_ERROR);
    }

    public static void notNull(Object object, String message, ErrorCode errorCode) {
        isTrue(null != object, message, errorCode);
    }

    public static void hasText(String text, String message) {
        hasText(text, message, ErrorCode.GENERIC_ERROR);
    }

    public static void hasText(String text, String message, ErrorCode errorCode) {
        isTrue(StringUtils.isNoneBlank(text), message, errorCode);
    }

    public static void notEmpty(Object[] array, String message) {
        notEmpty(array, message, ErrorCode.GENERIC_ERROR);
    }

    public static void notEmpty(Object[] array, String message, ErrorCode errorCode) {
        isTrue(ObjectUtils.isNotEmpty(array), message, errorCode);
    }

    public static void notEmpty(Collection<?> collection, String message) {
        notEmpty(collection, message, ErrorCode.GENERIC_ERROR);
    }

    public static void notEmpty(Collection<?> collection, String message, ErrorCode errorCode) {
        isTrue(CollectionUtils.isNotEmpty(collection), message, errorCode);
    }

    public static void notEmpty(Map<?, ?> map, String message) {
        notEmpty(map, message, ErrorCode.GENERIC_ERROR);
    }

    public static void notEmpty(Map<?, ?> map, String message, ErrorCode errorCode) {
        isTrue(MapUtils.isNotEmpty(map), message, errorCode);
    }

    private static void error(String message, ErrorCode errorCode) {
        throw new CoryException(errorCode, message);
    }
}
