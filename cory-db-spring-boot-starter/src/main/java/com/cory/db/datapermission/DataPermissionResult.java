package com.cory.db.datapermission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据权限结果
 * @author cory
 * @date 2022/6/21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataPermissionResult {

    /** 过滤策略 */
    private DataPermissionStrategy strategy;

    /** 如果过滤策略是{@link DataPermissionStrategy#FILTER}，那么这里装要过滤的sql。这里的sql会添加到查询的where部分，和已有的where部分是and关系 */
    private String filterSql;
}