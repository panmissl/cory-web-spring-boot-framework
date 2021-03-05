package com.cory.dao;

import com.cory.dao.BaseDao;
import com.cory.db.annotations.Dao;
import com.cory.db.annotations.Delete;
import com.cory.db.annotations.Param;
import com.cory.model.UserRoleRel;

/**
 * generated by CodeGenerator on 2017/5/10.
 */
@Dao(model = UserRoleRel.class)
public interface UserRoleRelDao extends BaseDao<UserRoleRel> {

    @Delete(whereSql = "user_id = #{userId}")
    int deleteByUser(@Param("userId") Integer userId);
}