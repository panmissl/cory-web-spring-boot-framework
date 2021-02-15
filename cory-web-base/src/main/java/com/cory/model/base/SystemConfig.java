package com.cory.model.base;

import com.cory.db.annotations.Field;
import com.cory.db.annotations.Model;
import com.cory.db.enums.CoryDbType;
import com.cory.model.BaseModel;
import lombok.Data;

/**
 * Created by Cory on 2017/5/10.
 */
@Data
@Model(table = "base_system_config", name = "系统配置", module = "base")
public class SystemConfig extends BaseModel {

    @Field(name = "code", type = CoryDbType.VARCHAR, label = "编码")
    private String code;

    @Field(name = "val", type = CoryDbType.VARCHAR, label = "值")
    private String val;

    @Field(name = "description", type = CoryDbType.VARCHAR, label = "描述")
    private String description;
}
