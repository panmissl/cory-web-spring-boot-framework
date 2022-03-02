package com.cory.model;

import com.cory.db.annotations.Field;
import com.cory.db.annotations.Model;
import com.cory.db.enums.CoryDbType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 数据字典只支持两级，不支持多级
 * @author cory
 * @date 2022/3/2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Model(name = "数据字典", module = "base")
public class DataDict extends BaseModel {

    /** 存储type的值 */
    @NotEmpty
    @Field(label = "类型", type = CoryDbType.VARCHAR, len = 1024, filtered = true, renderName = "typeDesc", datadictTypeValue = "cory_web_data_dict_root")
    private String type;

    @NotEmpty
    @Field(label = "值", type = CoryDbType.VARCHAR, len = 1024, filtered = true)
    private String value;

    @NotNull
    @Field(label = "排序顺序", type = CoryDbType.INT, defaultValue = "0", nullable = true)
    private Integer sn;

    @NotEmpty
    @Field(label = "描述", type = CoryDbType.VARCHAR, len = 1024)
    private String description;

    @NotNull
    @Field(label = "是否显示", type = CoryDbType.BOOLEAN, defaultValue = "1", renderName = "showableText")
    private Boolean showable;

}
