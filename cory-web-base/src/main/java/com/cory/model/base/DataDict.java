package com.cory.model.base;

import com.cory.db.annotations.Field;
import com.cory.db.annotations.Model;
import com.cory.db.enums.CoryDbType;
import com.cory.db.enums.FilterType;
import com.cory.model.BaseModel;
import lombok.Data;

/**
 * generated by CodeGenerator on 2017/5/10.
 */
@Data
@Model(name = "数据字典", module = "base")
public class DataDict extends BaseModel {

    @Field(label = "类型", type = CoryDbType.BIGINT, filtered = true, filterType = FilterType.REMOTE_SELECT, filterSelectUrl = "/ajax/base/datadict/allTypes", renderName = "typeDesc")
    private Integer type;

    @Field(label = "值", type = CoryDbType.VARCHAR, len = 1024)
    private String value;

    @Field(label = "排序顺序", type = CoryDbType.INT, defaultValue = "0", nullable = true)
    private Integer sn;

    @Field(label = "描述", type = CoryDbType.VARCHAR, len = 1024)
    private String description;

    @Field(label = "是否显示", type = CoryDbType.BOOLEAN, defaultValue = "1")
    private Boolean showable;

}
