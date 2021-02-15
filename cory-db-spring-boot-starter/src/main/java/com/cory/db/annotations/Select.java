package com.cory.db.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 加在Dao上
 * <br />
 * DAO返回值类型，支持Pagination(会自动查询总数和数据列表)，BaseModel的子类，简单类型（int、long、String、Date等）、Map、List<T>、其他对象类型（自动填充数据）
 *
 * Created by Cory on 2021/2/9.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Select {

    /**
     * where 部分sql。不要加前面的where，会自动加上is_deleted = 0条件。比如：
     * code = #{code} and create_time < #!{createTimeEnd} and create_time > #!{createTimeStart}
     * @return
     */
    String whereSql() default "";

    /**
     * 是否根据model对象过滤，如果为true，则必须有名为model、类型为T extends BaseModel的参数。
     * <br />
     * 可以和whereSql叠加使用（实现这样的功能：列表过滤，按类型等于过滤[用whereByModel实现]，同时按创建时间段过滤[用whereSql实现]）
     * <br />
     * 如果有whereSql和whereByModel里的参数重叠，则会添加两个条件，请避免此情况
     * @return
     */
    boolean whereByModel() default false;

    /**
     * 是否需要order by语句，默认不需要。如果为true，则需要参数：sort，如果没有则使用ORDER BY MODIFY_TIME DESC。会拼接成：order by #{sort}，所以sort是：id desc, create_time desc
     * @return
     */
    boolean orderBy() default false;

    /**
     * 是否需要limit语句，默认不需要。如果为true，则需要参数：pageStart, pageSize，如果没有则报错
     * @return
     */
    boolean limit() default false;

    /**
     * 自定义查询语句。默认为空。如果有自定义查询语句，则用此sql进行查询。比如：select * from base_user where is_deleted = 0 order by id desc
     * <br />
     * 自定义查询语句不支持分页查询。分页时勿用
     *
     * @return
     */
    String customSql() default "";
}
