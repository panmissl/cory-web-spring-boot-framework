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
@Model(name = "系统配置", module = "base")
public class SystemConfig extends BaseModel {

    @Field(label = "编码", type = CoryDbType.VARCHAR, len = 50)
    private String code;

    @Field(label = "值", type = CoryDbType.VARCHAR, len = 1024)
    private String val;

    @Field(label = "描述", type = CoryDbType.VARCHAR, len = 1024)
    private String description;
}
