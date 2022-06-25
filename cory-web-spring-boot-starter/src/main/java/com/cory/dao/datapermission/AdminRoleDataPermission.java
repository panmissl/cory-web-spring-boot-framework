package com.cory.dao.datapermission;

import com.cory.db.datapermission.CoryDataPermissionCode;
import com.cory.db.datapermission.DataPermission;
import com.cory.db.datapermission.DataPermissionResult;
import com.cory.db.datapermission.DataPermissionStrategy;
import com.cory.web.security.UserUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AdminRoleDataPermission implements DataPermission {

    @Override
    public DataPermissionResult permission(Map<String, Object> ognlParamMap) {
        if (UserUtils.isAdmin()) {
            return DataPermissionResult.builder().strategy(DataPermissionStrategy.PASS).build();
        } else {
            return DataPermissionResult.builder().strategy(DataPermissionStrategy.DENY).build();
        }
    }

    @Override
    public String code() {
        return CoryDataPermissionCode.ADMIN_ROLE;
    }
}
