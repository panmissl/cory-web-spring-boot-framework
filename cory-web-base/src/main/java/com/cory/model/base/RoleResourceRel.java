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
@Model(table = "base_role_resource_rel", name = "角色资源关系", module = "base")
public class RoleResourceRel extends BaseModel {

    @Field(name = "role_id", type = CoryDbType.BIGINT, label = "角色ID")
    private Integer roleId;

    @Field(name = "resource_id", type = CoryDbType.BIGINT, label = "资源ID")
    private Integer resourceId;

}
