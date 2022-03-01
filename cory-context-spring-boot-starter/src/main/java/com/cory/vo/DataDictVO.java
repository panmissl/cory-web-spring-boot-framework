package com.cory.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author cory
 * @date 2022/3/1
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataDictVO extends BaseVO {

    /** 值 */
    private String value;
    /** 描述 */
    private String description;
    /** 排序 */
    private Integer sn;
}
