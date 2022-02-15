package com.cory.db.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 加在Dao上
 * Created by Cory on 2021/2/9.
 * @author corypan
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Delete {

    /**
     * where 部分sql。不要加前面的where，会自动加上is_deleted = 0条件。比如：
     * code = #{code} and create_time < #{createTimeEnd} and create_time > #{createTimeStart}
     * @return
     */
    String whereSql();

    /**
     * 是否逻辑删除，默认物理删除，否则索引不好建
     * @return
     */
    boolean logicDelete() default false;
}
