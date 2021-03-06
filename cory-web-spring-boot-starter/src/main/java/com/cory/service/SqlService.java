package com.cory.service;

import com.cory.dao.SqlDao;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * generated by CodeGenerator on 2017/5/10.
 */
@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
@Transactional
public class SqlService {

    @Autowired
    private SqlDao sqlDao;

    public boolean customDDLSql(String sql) {
        batchExecute(sql, s -> sqlDao.customDDLSql(s));
        return true;
    }

    public int customExecuteSql(String sql) {
        return batchExecute(sql, s -> sqlDao.customExecuteSql(s));
    }

    public List<Map<String, Object>> customQuerySql(String sql) {
        return sqlDao.customQuerySql(sql);
    }

    private int batchExecute(String sqlListStr, Consumer<String> consumer) {
        String[] arr = sqlListStr.split(";");
        int num = 0;
        for (String sql : arr) {
            if (StringUtils.isNotBlank(sql)) {
                consumer.accept(sql);
                num ++;
            }
        }
        return num;
    }
}
