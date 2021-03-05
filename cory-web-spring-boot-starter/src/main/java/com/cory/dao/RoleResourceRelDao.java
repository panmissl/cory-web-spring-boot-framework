package com.cory.dao;

import com.cory.dao.BaseDao;
import com.cory.db.annotations.Dao;
import com.cory.db.annotations.Delete;
import com.cory.db.annotations.Param;
import com.cory.model.RoleResourceRel;

/**
 * generated by CodeGenerator on 2017/5/10.
 */
@Dao(model = RoleResourceRel.class)
public interface RoleResourceRelDao extends BaseDao<RoleResourceRel> {

    @Delete(whereSql = "role_id = #{roleId}")
    void deleteByRole(@Param("roleId") Integer roleId);
}