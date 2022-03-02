package com.cory.model;

import com.cory.db.annotations.Field;
import com.cory.db.annotations.Model;
import com.cory.db.enums.CoryDbType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

/**
 * Created by Cory on 2017/5/10.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Model(name = "系统配置", module = "base")
public class SystemConfig extends BaseModel {

    @NotEmpty
    @Field(label = "编码", type = CoryDbType.VARCHAR, len = 50, filtered = true)
    private String code;

    @NotEmpty
    @Field(label = "值", type = CoryDbType.TEXT)
    private String val;

    @NotEmpty
    @Field(label = "描述", type = CoryDbType.TEXT)
    private String description;
}
