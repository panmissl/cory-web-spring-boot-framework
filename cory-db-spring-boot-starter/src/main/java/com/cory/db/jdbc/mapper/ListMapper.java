package com.cory.db.jdbc.mapper;

import com.cory.constant.ErrorCode;
import com.cory.util.AssertUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Cory on 2021/2/12.
 */
@Slf4j
public class ListMapper extends BaseResultMapper {

    public Object map(List<Map<String, Object>> listData, Class<?> returnType) {
        //返回类型为空，不知道返回啥，直接抛错
        AssertUtils.notNull(returnType, "return type is null", ErrorCode.DB_ERROR);

        if (CollectionUtils.isEmpty(listData)) {
            return new ArrayList<>();
        }
        return listData.stream().map(map -> new BeanMapper().doMap(map, returnType)).collect(Collectors.toList());
    }
}
