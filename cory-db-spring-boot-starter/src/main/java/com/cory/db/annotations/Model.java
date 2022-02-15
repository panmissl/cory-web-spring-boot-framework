package com.cory.db.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表名自动生成，生成规则：module + '_' + Model类名转下划线格式
 *
 * Created by Cory on 2021/2/9.
 * @author corypan
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Model {

    /**
     * model名称
     * @return
     */
    String name();

    /**
     * 模块，比如：base（基础模块）
     * @return
     */
    String module();

    /** 是否可添加 */
    boolean createable() default true;

    /** 是否可修改 */
    boolean updateable() default true;

    /** 是否可删除 */
    boolean deleteable() default true;

    /** 是否不建表：仅解析DAO里面的方法，且每个方法都只能有 {@link Sql} 的注解，不能有其他的 */
    boolean noTable() default false;
}
