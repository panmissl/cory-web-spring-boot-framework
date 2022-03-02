package com.cory.model;

import com.cory.db.annotations.Field;
import com.cory.db.annotations.Model;
import com.cory.db.enums.CoryDbType;
import com.cory.model.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

/**
 * generated by CodeGenerator on 2017/5/10.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Model(name = "反馈", module = "base")
public class Feedback extends BaseModel {

    @NotEmpty
    @Field(label = "内容", type = CoryDbType.VARCHAR, len = 1024)
    private String content;
    
}
