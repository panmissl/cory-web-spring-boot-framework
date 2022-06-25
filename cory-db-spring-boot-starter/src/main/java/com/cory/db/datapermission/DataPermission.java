package com.cory.db.datapermission;

import java.util.Map;

/**
 * Created by Cory on 2021/2/12.
 * @see CoryDataPermissionCode
 */
public interface DataPermission {

    /**
     * 权限判断
     * <br />
     * <br />
     * 注意：此方法必须是pure的，也就是多次调用返回一样的结果，而且不会造成其它副作用（比如每调用一次，就更新一次数据库）。因为在查询过程中可能会被多次调用
     * <br />
     * <br />
     * @param ognlParamMap DAO方法上的参数map。那些加了@Param注解的参数
     * @return
     */
    DataPermissionResult permission(Map<String, Object> ognlParamMap);

    /**
     * 编码
     * @return
     * @see CoryDataPermissionCode
     */
    String code();
}
