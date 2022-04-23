package com.cory.model;

import com.cory.db.annotations.Field;
import com.cory.db.annotations.Model;
import com.cory.db.enums.CoryDbType;
import com.cory.enums.ClusterStatus;
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
@Model(name = "集群", module = "base", createable = false, updateable = false, deleteable = false)
public class Cluster extends BaseModel {

    private static final long serialVersionUID = 5986818325581884185L;

    @NotEmpty
    @Field(label = "IP&端口", type = CoryDbType.VARCHAR, filtered = true, desc = "IP&端口：192.168.0.1:8080")
    private String ip;

    @NotNull
    @Field(label = "状态", type = CoryDbType.ENUM)
    private ClusterStatus status;

}