package com.cory.db.datapermission;

/**
 * Created by Cory on 2021/2/13.
 * @see DataPermission
 */
public enum DataPermissionStrategy {

    /** 直接通过，不进行特殊过滤 */
    PASS,

    /** 直接拒绝，不返回任何数据 */
    DENY,

    /** 根据返回的filter语句进行过滤 */
    FILTER,

}
