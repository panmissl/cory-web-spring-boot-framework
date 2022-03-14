package com.cory.web.advice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Controller类或方法加此注解(加在类上对所有方法生效，加在方法上只对该方法生效)，则返回值(GenericResult)里的object被加密，isEncrypt字段设置为true，其它几个字段不加密。
 * <br />
 * 1、object字段为null，不加密
 * <br />
 * 2、使用了GenericResultExclude，不加密
 * <br />
 * 3、加密方法自己提供。提供一个bean，实现{@link GenericResultEncryptor}接口
 * <br />
 * <br />
 * 注意：如果使用了此注解，又没有提供GenericResultEncryptor的实现Bean，那么会报错
 * <br />
 * <br />
 *
 *
 * @author corypan
 * @see GenericResultEncryptor
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GenericResultEncrypt {}
