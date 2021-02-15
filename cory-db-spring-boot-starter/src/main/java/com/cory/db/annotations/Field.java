package com.cory.db.annotations;

import com.cory.db.enums.CoryDbType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 加在Model上
 * <br />
 * 数据库定义，相当于DDL。同时兼具页面配置功能
 * Created by Cory on 2021/2/9.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Field {

    /**
     * 数据库字段名
     * @return
     */
    String name();

    /**
     * 数据库类型
     * @return
     */
    CoryDbType type();

    /**
     * 页面显示label
     * @return
     */
    String label();

    /**
     * 页面显示说明。比如一些字段的备注。默认为空
     * @return
     */
    String desc() default "";

    /**
     * 页面上显示。默认为true，即默认显示。不需要显示的字段配置成false
     * @return
     */
    boolean showable() default true;

    /**
     * 数据库长度，只对varchar生效，默认为254
     * @return
     */
    int len() default 254;

    /**
     * 数据库是否可空。默认不可空，需要可空的自行设置
     * @return
     */
    boolean nullable() default false;

    /**
     * 数据库默认值
     * @return
     */
    String defaultValue() default "";

    /**
     * 数据库字段描述(不显示再页面上)。默认无，需要时设置即可
     * @return
     */
    String comment() default "";

}
