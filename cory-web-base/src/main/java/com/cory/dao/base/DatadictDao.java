package com.cory.dao.base;

import com.cory.dao.BaseDao;
import com.cory.db.annotations.Dao;
import com.cory.db.annotations.Param;
import com.cory.db.annotations.Select;
import com.cory.db.annotations.Update;
import com.cory.model.base.DataDict;

import java.util.List;

/**
 * generated by CodeGenerator on 2017/5/10.
 */
@Dao(model = DataDict.class)
public interface DatadictDao extends BaseDao<DataDict> {

    @Select(orderBy = true)
    List<DataDict> getAllTypes(@Param("sort") String sort);

    @Select(whereSql = "value = #{value}")
    DataDict getByValue(@Param("value") String value);

    @Select(whereSql = "type = #{type}", orderBy = true)
    List<DataDict> getByType(@Param("type") Integer type, @Param("sort") String sort);

    @Update(columnSql = "MODIFIER = #{modifier}, MODIFY_TIME = now(), SHOWABLE = #{showable}", whereSql = "id = #{id}")
    int updateShowable(@Param("id") Integer id, @Param("showable") boolean showable, @Param("modifier") Integer modifier);
}
