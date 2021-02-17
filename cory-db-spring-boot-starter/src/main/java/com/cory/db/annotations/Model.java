package com.cory.db.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表名自动生成，生成规则：module + '_' + Model类名转下划线格式
 *
 * Created by Cory on 2021/2/9.
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
}
