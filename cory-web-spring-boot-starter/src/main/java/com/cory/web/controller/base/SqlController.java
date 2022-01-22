package com.cory.web.controller.base;

import com.cory.service.SqlService;
import com.cory.util.AssertUtils;
import com.cory.util.systemconfigcache.SystemConfigCacheUtil;
import com.cory.web.controller.BaseController;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * generated by CodeGenerator on 2017/5/10.
 */
@RestController
@RequestMapping("/ajax/base/sql/")
public class SqlController extends BaseController {

    private static final String PASSWORD_KEY = "sql_password";

    @Autowired
    private SqlService sqlService;

    @PostMapping(value = "execute", params = "type=ddl")
    public boolean customDDLSql(String password, String sql) {
        checkSql(sql, true);
        checkPassword(password);
        return sqlService.customDDLSql(sql);
    }

    @PostMapping(value = "execute", params = "type=execute")
    public int customExecuteSql(String password, String sql) {
        checkSql(sql, true);
        checkPassword(password);
        return sqlService.customExecuteSql(sql);
    }

    @PostMapping(value = "execute", params = "type=query")
    public List<Map<String, Object>> customQuerySql(String password, String sql) {
        checkSql(sql, false);
        checkPassword(password);
        return sqlService.customQuerySql(sql);
    }

    private void checkSql(String sql, boolean multiEnable) {
        AssertUtils.hasText(sql, "sql不能为空");
        if (!multiEnable) {
            AssertUtils.isTrue(!sql.contains(";"), "sql只能为单条，不能为多条");
        }
    }

    private void checkPassword(String password) {
        AssertUtils.hasText(password, "密码不正确");
        String db = SystemConfigCacheUtil.getCache(PASSWORD_KEY);
        AssertUtils.isTrue(StringUtils.equals(password, db), "密码不正确");
    }

}
