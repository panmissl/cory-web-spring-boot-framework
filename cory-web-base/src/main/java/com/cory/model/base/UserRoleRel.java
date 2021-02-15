package com.cory.model.base;

import com.cory.db.annotations.Field;
import com.cory.db.annotations.Model;
import com.cory.db.enums.CoryDbType;
import com.cory.model.BaseModel;
import lombok.Data;

/**
 * generated by CodeGenerator on 2017/5/10.
 */
@Data
@Model(table = "base_user_role_rel", name = "用户角色关系", module = "base")
public class UserRoleRel extends BaseModel {

    @Field(name = "user_id", type = CoryDbType.BIGINT, label = "用户ID")
    private Integer userId;

    @Field(name = "role_id", type = CoryDbType.BIGINT, label = "角色ID")
    private Integer roleId;
}
