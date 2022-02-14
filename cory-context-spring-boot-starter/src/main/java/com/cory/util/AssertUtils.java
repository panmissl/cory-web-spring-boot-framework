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
 * 条件判断，不符合则抛出CoryException
 * @author Cory Pan
 */
public class AssertUtils {

    public static void isTrue(boolean expression, String errorCode, String message) {
        if (!expression) {
            error(errorCode, message);
        }
    }

    public static void isNull(Object object, String errorCode, String message) {
        isTrue(null == object, errorCode, message);
    }

    public static void notNull(Object object, String errorCode, String message) {
        isTrue(null != object, errorCode, message);
    }

    public static void hasText(String text, String errorCode, String message) {
        isTrue(StringUtils.isNoneBlank(text), errorCode, message);
    }

    public static void notEmpty(Object[] array, String errorCode, String message) {
        isTrue(ObjectUtils.isNotEmpty(array), errorCode, message);
    }

    public static void notEmpty(Collection<?> collection, String errorCode, String message) {
        isTrue(CollectionUtils.isNotEmpty(collection), errorCode, message);
    }

    public static void notEmpty(Map<?, ?> map, String errorCode, String message) {
        isTrue(MapUtils.isNotEmpty(map), errorCode, message);
    }

    /**
     * 请使用带字符型errorCode的参数代替
     * @deprecated 请使用带字符型errorCode的参数代替
     */
    @Deprecated
    public static void isTrue(boolean expression, String message) {
        isTrue(expression, message, ErrorCode.GENERIC_ERROR);
    }

    /**
     * 请使用带字符型errorCode的参数代替
     * @deprecated 请使用带字符型errorCode的参数代替
     */
    @Deprecated
    public static void isTrue(boolean expression, String message, ErrorCode errorCode) {
        if (!expression) {
            error(message, errorCode);
        }
    }

    /**
     * 请使用带字符型errorCode的参数代替
     * @deprecated 请使用带字符型errorCode的参数代替
     */
    @Deprecated
    public static void isNull(Object object, String message) {
        isNull(object, message, ErrorCode.GENERIC_ERROR);
    }

    /**
     * 请使用带字符型errorCode的参数代替
     * @deprecated 请使用带字符型errorCode的参数代替
     */
    @Deprecated
    public static void isNull(Object object, String message, ErrorCode errorCode) {
        isTrue(null == object, message, errorCode);
    }

    /**
     * 请使用带字符型errorCode的参数代替
     * @deprecated 请使用带字符型errorCode的参数代替
     */
    @Deprecated
    public static void notNull(Object object, String message) {
        notNull(object, message, ErrorCode.GENERIC_ERROR);
    }

    /**
     * 请使用带字符型errorCode的参数代替
     * @deprecated 请使用带字符型errorCode的参数代替
     */
    @Deprecated
    public static void notNull(Object object, String message, ErrorCode errorCode) {
        isTrue(null != object, message, errorCode);
    }

    /**
     * 请使用带字符型errorCode的参数代替
     * @deprecated 请使用带字符型errorCode的参数代替
     */
    @Deprecated
    public static void hasText(String text, String message) {
        hasText(text, message, ErrorCode.GENERIC_ERROR);
    }

    /**
     * 请使用带字符型errorCode的参数代替
     * @deprecated 请使用带字符型errorCode的参数代替
     */
    @Deprecated
    public static void hasText(String text, String message, ErrorCode errorCode) {
        isTrue(StringUtils.isNoneBlank(text), message, errorCode);
    }

    /**
     * 请使用带字符型errorCode的参数代替
     * @deprecated 请使用带字符型errorCode的参数代替
     */
    @Deprecated
    public static void notEmpty(Object[] array, String message) {
        notEmpty(array, message, ErrorCode.GENERIC_ERROR);
    }

    /**
     * 请使用带字符型errorCode的参数代替
     * @deprecated 请使用带字符型errorCode的参数代替
     */
    @Deprecated
    public static void notEmpty(Object[] array, String message, ErrorCode errorCode) {
        isTrue(ObjectUtils.isNotEmpty(array), message, errorCode);
    }

    /**
     * 请使用带字符型errorCode的参数代替
     * @deprecated 请使用带字符型errorCode的参数代替
     */
    @Deprecated
    public static void notEmpty(Collection<?> collection, String message) {
        notEmpty(collection, message, ErrorCode.GENERIC_ERROR);
    }

    /**
     * 请使用带字符型errorCode的参数代替
     * @deprecated 请使用带字符型errorCode的参数代替
     */
    @Deprecated
    public static void notEmpty(Collection<?> collection, String message, ErrorCode errorCode) {
        isTrue(CollectionUtils.isNotEmpty(collection), message, errorCode);
    }

    /**
     * 请使用带字符型errorCode的参数代替
     * @deprecated 请使用带字符型errorCode的参数代替
     */
    @Deprecated
    public static void notEmpty(Map<?, ?> map, String message) {
        notEmpty(map, message, ErrorCode.GENERIC_ERROR);
    }

    /**
     * 请使用带字符型errorCode的参数代替
     * @deprecated 请使用带字符型errorCode的参数代替
     */
    @Deprecated
    public static void notEmpty(Map<?, ?> map, String message, ErrorCode errorCode) {
        isTrue(MapUtils.isNotEmpty(map), message, errorCode);
    }

    /**
     * 请使用带字符型errorCode的参数代替
     * @deprecated 请使用带字符型errorCode的参数代替
     */
    @Deprecated
    private static void error(String message, ErrorCode errorCode) {
        throw new CoryException(errorCode, message);
    }

    private static void error(String errorCode, String message) {
        throw new CoryException(errorCode, message);
    }
}
