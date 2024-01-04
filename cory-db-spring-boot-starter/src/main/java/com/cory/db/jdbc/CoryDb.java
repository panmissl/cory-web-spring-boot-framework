package com.cory.db.jdbc;

import com.cory.db.jdbc.CorySqlBuilder.CorySqlInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by Cory on 2021/2/9.
 */
public interface CoryDb {

    int insert(CorySqlInfo sqlInfo);

    int delete(CorySqlInfo sqlInfo);

    int update(CorySqlInfo sqlInfo);

    List<Map<String, Object>> select(CorySqlInfo sqlInfo);

    int selectCount(CorySqlInfo sqlInfo);

    /**
     * 执行一条DDL sql
     * @param sql
     */
    void executeSql(String sql);

    /**
     * 执行一条insert、update或delete sql
     * @param sql
     * @return
     */
    int update(String sql);

    /**
     * 执行一条查询sql，返回通用类型
     * @param sql
     * @return
     */
    List<Map<String, Object>> query(String sql);
}
