package com.cory.db.jdbc.mapper;

import java.util.List;
import java.util.Map;

/**
 * Created by Cory on 2021/2/12.
 */
public class MapListMapper extends BaseResultMapper {

    public Object map(List<Map<String, Object>> listData, Class<?> returnType) {
        return listData;
    }
}
