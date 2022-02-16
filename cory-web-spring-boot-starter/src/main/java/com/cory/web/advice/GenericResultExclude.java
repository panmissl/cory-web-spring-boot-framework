package com.cory.web.advice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Controller类或方法加此注解(加在类上对所有方法生效，加在方法上只对该方法生效)，则返回值不被GenericResult包裹，直接原样输出。规则如下：
 * <br />
 * 1、返回null，不输出，返回空
 * <br />
 * 2、返回字符串，直接输出此字符串
 * <br />
 * 3、返回其它类型(比如boolean, double ,VO, DTO等)，根据renderAsJson()的配置，输出为JSON.toJSONString(xxx)或xxx.toString()
 * <br />
 * <br />
 * @author corypan
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GenericResultExclude {

    /**
     * 如果返回值不是String，是否输出为JSON，默认输出为JSON，如果选否则，则输出为返回值的toString()
     * @return
     */
    boolean renderAsJson() default true;
}
