package com.cory.dao;

import com.cory.db.annotations.Dao;
import com.cory.db.annotations.Param;
import com.cory.db.annotations.Select;
import com.cory.dto.AccessCountStatDTO;
import com.cory.model.AccessCount;

import java.util.List;

/**
 * generated by CodeGenerator on 2017/5/10.
 */
@Dao(model = AccessCount.class)
public interface AccessCountDao extends BaseDao<AccessCount> {

    @Select(customSql = "select uri, sum(access_count) as count from base_access_count where is_deleted = 0 #![ and day = #{da} ] group by uri order by count desc limit 20", returnType = AccessCountStatDTO.class)
    List<AccessCountStatDTO> stat(@Param("day") String day);
}
