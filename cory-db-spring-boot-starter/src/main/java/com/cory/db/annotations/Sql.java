package com.cory.db.annotations;

import com.cory.db.enums.CustomSqlType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 加在Dao上
 * <br />
 * 执行自定义sql。使用此注解的方法，有且只有一个String类型的参数。
 * <br />
 * 返回值类型：type为DDL时，返回值类型为void；type为EXECUTE时，返回值类型为int；type为QUERY时，返回值类型为List&lt;Map&lt;String, Object&gt;&gt;
 *
 * Created by Cory on 2021/2/9.
 * @author corypan
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Sql {

    CustomSqlType type();
}
