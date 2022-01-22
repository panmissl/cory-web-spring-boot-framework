package com.cory.dao;

import com.cory.db.annotations.*;
import com.cory.model.BaseModel;
import com.cory.page.Pagination;

import java.util.List;

/**
 * Created by Cory on 2017/5/10.
 */
public interface BaseDao<T extends BaseModel> {

    @Insert
    void add(@Param("model") T model);

    @Delete(whereSql = "ID = #{model.id}")
    void delete(@Param("model") T model);

    @Delete(whereSql = "ID = #{id}")
    void deleteById(@Param("id") int id);

    @UpdateModel
    void updateModel(@Param("model") T model);

    @Select(whereSql = "ID = #{id}")
    T get(@Param("id") int id);

    /**
     * 如果没有code字段，则此方法抛错。如果有则自动执行查询
     * @param code
     * @return
     */
    @Select(whereSql = "CODE = #{code}")
    T getByCode(@Param("code") String code);

    /**
     * 如果没有code字段，则此方法抛错。如果有则自动执行查询
     * @param codeList
     * @return
     */
    @Select(whereSql = "CODE IN #{codeList}")
    List<T> getByCodeList(@Param("codeList") List<String> codeList);

    @Select(whereByModel = true, orderBy = true, limit = true)
    Pagination<T> pagination(@Param("model") T model, @Param("pageStart") int pageStart, @Param("pageSize") int pageSize, @Param("sort") String sort);
}
