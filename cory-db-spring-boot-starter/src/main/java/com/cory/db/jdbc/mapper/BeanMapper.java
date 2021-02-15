package com.cory.db.jdbc.mapper;

import com.cory.constant.ErrorCode;
import com.cory.exception.CoryException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.Map;

/**
 * Created by Cory on 2021/2/12.
 */
@Slf4j
public class BeanMapper extends SingleResultMapper {

    @Override
    protected Object doMap(Map<String, Object> map, Class<?> returnType) {
        try {
            Object obj = returnType.newInstance();
            BeanUtils.copyProperties(map, obj);
            return obj;
        } catch (Exception e) {
            log.error("map to bean error", e);
            throw new CoryException(ErrorCode.DB_ERROR, "map to bean error" + e.getMessage());
        }
    }
}
