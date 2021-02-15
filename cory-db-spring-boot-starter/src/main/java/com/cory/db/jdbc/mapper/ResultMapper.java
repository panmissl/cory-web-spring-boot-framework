package com.cory.db.jdbc.mapper;

import java.util.List;
import java.util.Map;

/**
 * Created by Cory on 2021/2/12.
 */
public interface ResultMapper {

    /**
     * @param listData
     * @param returnType 对于list，returnType指的是泛型的类型
     * @return
     */
    Object map(List<Map<String, Object>> listData, Class<?> returnType);
}
