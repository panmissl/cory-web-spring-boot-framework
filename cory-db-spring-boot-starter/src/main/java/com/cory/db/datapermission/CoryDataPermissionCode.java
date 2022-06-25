package com.cory.db.datapermission;

/**
 * Created by Cory on 2021/2/13.
 * @see DataPermission
 */
public interface CoryDataPermissionCode {

    /** admin角色才能查询，否则返回空 */
    String ADMIN_ROLE = "_CORY_DATA_PERMISSION_CODE_ADMIN_ROLE";

}
