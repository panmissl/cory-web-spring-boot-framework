package com.cory.db.jdbc.mapper;

import com.cory.constant.ErrorCode;
import com.cory.exception.CoryException;
import com.cory.util.DateUtils;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;

/**
 * Created by Cory on 2021/2/12.
 */
@Slf4j
public class DateMapper extends SimpleValueResultMapper {

    @Override
    protected Object doSimpleMap(Object object, Class<?> returnType) {
        try {
            return DateUtils.parseDate(object.toString());
        } catch (ParseException e) {
            log.error("parse to timestamp fail", e);
            throw new CoryException(ErrorCode.DB_ERROR, "parse to timestamp fail: " + e.getMessage());
        }
    }
}
