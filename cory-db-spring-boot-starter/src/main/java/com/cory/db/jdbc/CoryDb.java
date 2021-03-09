package com.cory.db.jdbc;

import com.alibaba.fastjson.JSON;
import com.cory.constant.ErrorCode;
import com.cory.db.jdbc.CorySqlBuilder.CorySqlInfo;
import com.cory.exception.CoryException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * Created by Cory on 2021/2/9.
 */
public class CoryDb {

    private JdbcTemplate jdbcTemplate;

    public CoryDb(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int insert(CorySqlInfo sqlInfo) {
        PreparedStatementCreator creator = conn -> {
            PreparedStatement pstmt = conn.prepareStatement(sqlInfo.getSql(), Statement.RETURN_GENERATED_KEYS);
            Object[] params = buildParams(sqlInfo);
            if (null != params && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    pstmt.setObject(i + 1, params[i]);
                }
            }
            return pstmt;
        };
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int row = jdbcTemplate.update(creator, keyHolder);
        if (row > 0) {
            return keyHolder.getKeyAs(BigInteger.class).intValue();
        }
        throw new CoryException(ErrorCode.DB_ERROR, "插入失败(影响行数：" + row + ")，sqlInfo: " + JSON.toJSONString(sqlInfo));
    }

    public int delete(CorySqlInfo sqlInfo) {
        return jdbcTemplate.update(sqlInfo.getSql(), buildParams(sqlInfo));
    }

    public int update(CorySqlInfo sqlInfo) {
        return jdbcTemplate.update(sqlInfo.getSql(), buildParams(sqlInfo));
    }

    public List<Map<String, Object>> select(CorySqlInfo sqlInfo) {
        return jdbcTemplate.queryForList(sqlInfo.getSql(), buildParams(sqlInfo));
    }

    public int selectCount(CorySqlInfo sqlInfo) {
        return jdbcTemplate.queryForObject(sqlInfo.getSql(), Integer.class, buildParams(sqlInfo));
    }

    private Object[] buildParams(CorySqlInfo sqlInfo) {
        List<Object> params = sqlInfo.getParams();
        return null == params ? new Object[0] : params.toArray(new Object[params.size()]);
    }

    /**
     * 执行一条DDL sql
     * @param sql
     */
    public void executeSql(String sql) {
        jdbcTemplate.execute(sql);
    }

    /**
     * 执行一条insert、update或delete sql
     * @param sql
     * @return
     */
    public int update(String sql) {
        return jdbcTemplate.update(sql);
    }

    /**
     * 执行一条查询sql，返回通用类型
     * @param sql
     * @return
     */
    public List<Map<String, Object>> query(String sql) {
        return jdbcTemplate.queryForList(sql);
    }
}
