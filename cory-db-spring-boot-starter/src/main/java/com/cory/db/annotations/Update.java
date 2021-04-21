package com.cory.db.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 加在Dao上
 * Created by Cory on 2021/2/9.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Update {

    /**
     * 更新字段部分sql。比如：name = #{model.name}, status = {model.status}, modify_time = now(), modifier = #{modifier}
     * @return
     */
    String columnSql();

    /**
     * where 部分sql。不要加前面的where，会自动加上is_deleted = 0条件。比如：
     * code = #{code} and create_time < #{createTimeEnd} and create_time > #{createTimeStart}
     * @return
     */
    String whereSql();
}
