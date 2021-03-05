package com.cory.dao;

import com.cory.db.annotations.Dao;
import com.cory.db.annotations.Param;
import com.cory.db.annotations.Select;
import com.cory.model.User;

/**
 * generated by CodeGenerator on 2017/5/10.
 */
@Dao(model = User.class)
public interface UserDao extends BaseDao<User> {

    /**
     * @param logonId 可能是phone、email、第三方id
     * @return
     */
    @Select(whereSql = "(PHONE = #{logonId} OR EMAIL = #{logonId} OR THIRDPARTY_ID = #{logonId})")
    User findByLogonId(@Param("logonId") String logonId);
}