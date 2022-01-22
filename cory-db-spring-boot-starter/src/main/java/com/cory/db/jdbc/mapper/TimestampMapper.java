package com.cory.db.jdbc.mapper;

import com.cory.constant.ErrorCode;
import com.cory.exception.CoryException;
import com.cory.util.DateUtils;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.text.ParseException;

/**
 * Created by Cory on 2021/2/12.
 */
@Slf4j
public class TimestampMapper extends SimpleValueResultMapper {

    @Override
    protected Object doSimpleMap(Object object, Class<?> returnType) {
        try {
            return new Timestamp(DateUtils.parseDate(object.toString()).getTime());
        } catch (ParseException e) {
            log.error("parse to timestamp fail", e);
            throw new CoryException(ErrorCode.DB_ERROR, "parse to timestamp fail: " + e.getMessage());
        }
    }
}
