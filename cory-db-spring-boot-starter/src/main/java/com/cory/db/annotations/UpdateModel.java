package com.cory.db.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 按ID更新整个model，不用写sql，系统直接处理所有字段
 * 
 * 加在Dao上
 * Created by Cory on 2021/2/9.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UpdateModel {

}
