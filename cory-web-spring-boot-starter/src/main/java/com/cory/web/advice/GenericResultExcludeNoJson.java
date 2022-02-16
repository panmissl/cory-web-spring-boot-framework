package com.cory.web.advice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 参见{@link GenericResultExclude}，因为它默认输出为JSON了，所以对于不需要输出为JSON的，可以设置{@link GenericResultExclude#renderAsJson()}为false，也可以直接使用此注解。
 * 用法和{@link GenericResultExclude}一样，只是不默认输出JSON了，默认输出toString()
 * @author corypan
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@GenericResultExclude(renderAsJson = false)
public @interface GenericResultExcludeNoJson {}
