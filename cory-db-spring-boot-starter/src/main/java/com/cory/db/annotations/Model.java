package com.cory.db.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Cory on 2021/2/9.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Model {

    /**
     * 表名
     * @return
     */
    String table();

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
}
