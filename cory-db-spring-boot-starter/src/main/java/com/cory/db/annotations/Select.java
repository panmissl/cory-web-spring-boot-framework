package com.cory.db.annotations;

import com.cory.db.datapermission.CoryDataPermissionCode;
import com.cory.db.datapermission.DataPermission;

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
 * @author corypan
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Select {

    /**
     * where 部分sql。不要加前面的where，会自动加上is_deleted = 0条件。比如：
     * code = #{code} and create_time < #{createTimeEnd} and create_time > #{createTimeStart}
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
     * 是否查询总记录数，如果为true，则查询记录数返回。此时要求返回值是Integer
     * @return
     */
    boolean count() default false;

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

    /**
     * 返回类型，如果返回类型是List或其他带泛型的，因为取不到泛型类型，所以默认用Dao关联的Model类，如果是其他类型，请使用此属性指定。
     * <br />
     * 比如：UserDao里，返回方法定义是这样：List<User> listAllUsers()，因为取不到泛型类型User，所以默认List里装的是User，如果是装其他对象，则需要指定。比如需要返回一个自定义的UserVO：List<UserVO> listUserVo，则使用此属性指定。
     * <br />
     * 注意：对于非泛型的，不用指定此属性，会自动解析返回值类型
     *
     * @return
     */
    Class returnType() default Void.class;

    /**
     * 数据权限配置，这里配置的是{@link DataPermission}里的code(系统默认提供的数据权限已经定义在了{@link CoryDataPermissionCode}里，直接能用)，进行数据权限的过滤
     * <br />
     * <br />
     * 指定customSql时，数据权限不生效
     * <br />
     * <br />
     * @return
     * @see DataPermission
     * @see CoryDataPermissionCode
     */
    String[] dataPermission() default {};
}
