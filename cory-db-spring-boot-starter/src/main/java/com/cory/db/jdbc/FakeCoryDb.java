package com.cory.db.jdbc;

import com.cory.db.jdbc.CorySqlBuilder.CorySqlInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by Cory on 2021/2/9.
 */
public class FakeCoryDb implements CoryDb {

    public FakeCoryDb() {
    }

    @Override
    public int insert(CorySqlInfo sqlInfo) {
        throw new UnsupportedOperationException("please enable db");
    }

    @Override
    public int delete(CorySqlInfo sqlInfo) {
        throw new UnsupportedOperationException("please enable db");
    }

    @Override
    public int update(CorySqlInfo sqlInfo) {
        throw new UnsupportedOperationException("please enable db");
    }

    @Override
    public List<Map<String, Object>> select(CorySqlInfo sqlInfo) {
        throw new UnsupportedOperationException("please enable db");
    }

    @Override
    public int selectCount(CorySqlInfo sqlInfo) {
        throw new UnsupportedOperationException("please enable db");
    }

    @Override
    public void executeSql(String sql) {
        throw new UnsupportedOperationException("please enable db");
    }

    @Override
    public int update(String sql) {
        throw new UnsupportedOperationException("please enable db");
    }

    @Override
    public List<Map<String, Object>> query(String sql) {
        throw new UnsupportedOperationException("please enable db");
    }
}
