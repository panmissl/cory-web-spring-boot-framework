package com.cory.model;

import com.cory.db.annotations.Field;
import com.cory.db.annotations.Model;
import com.cory.db.enums.CoryDbType;
import com.cory.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * generated by CodeGenerator on 2017/5/10.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Model(name = "资源", module = "base", deleteable = false, createable = false, updateable = false)
public class Resource extends BaseModel {

    @NotNull
    @Field(label = "类型", type = CoryDbType.ENUM, len = 50, filtered = true)
    private ResourceType type;

    @NotEmpty
    @Field(label = "值", type = CoryDbType.VARCHAR, len = 200, filtered = true)
    private String value;

    @NotEmpty
    @Field(label = "说明", type = CoryDbType.VARCHAR, len = 200)
    private String description;
    
}
