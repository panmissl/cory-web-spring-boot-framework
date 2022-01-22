package com.cory.db.annotations;

import com.cory.db.enums.CoryDbType;
import com.cory.db.enums.FilterType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 加在Model上
 * <br />
 * 数据库字段名自动生成，规则：java属性名转为下划线风格
 * <br />
 * 数据库定义，相当于DDL。同时兼具页面配置功能
 * Created by Cory on 2021/2/9.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Field {

    /**
     * 页面显示label
     * @return
     */
    String label();

    /**
     * 数据库类型
     * @return
     */
    CoryDbType type();

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
     * 是否可编辑。默认为true。比如一般status状态都不能编辑
     * @return
     */
    boolean editable() default true;

    /**
     * 是否作为列表查询过滤字段，默认不是
     * @return
     */
    boolean filtered() default false;

    /**
     * 过滤类型
     * @return
     */
    FilterType filterType() default FilterType.TEXT;

    /**
     * 当过滤类型是REMOTE_SELECT时，select的url（相对URL，比如：/ajax/base/user/list）
     * @return
     */
    String filterSelectUrl() default "";

    /**
     * 渲染字段：有一些字段渲染时不能直接渲染自己，要渲染加工过的数据，比如枚举类型。
     * <br />
     * 对于继承自BaseEnum的枚举类型、Date型、布尔型，如果指定了此值则用这个名称，否则用默认规则：字段名+Text，比如：showable -> showableText
     * <br />
     * 其他特殊的字段自己在fillOtherFields方法里设置
     *
     * @see com.cory.model.BaseModel#renderFields
     * @return
     */
    String renderName() default "";

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
