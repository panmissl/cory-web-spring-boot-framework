package com.cory.db.jdbc.mapper;

import com.cory.constant.Constants;
import com.cory.constant.ErrorCode;
import com.cory.exception.CoryException;
import com.cory.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.*;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

/**
 * Created by Cory on 2021/2/12.
 */
@Slf4j
public class BeanMapper extends SingleResultMapper {

    static {
        DateTimeConverter converter = new DateTimeConverter();

        ConvertUtilsBean2 convertUtilsBean2 = new ConvertUtilsBean2();
        convertUtilsBean2.deregister(Date.class);
        convertUtilsBean2.deregister(Timestamp.class);

        convertUtilsBean2.register(converter, Date.class);
        convertUtilsBean2.register(converter, Timestamp.class);

        BeanUtilsBean beanUtilsBean = new BeanUtilsBean(convertUtilsBean2, new PropertyUtilsBean());

        BeanUtilsBean2.setInstance(beanUtilsBean);
    }

    @Override
    protected Object doMap(Map<String, Object> map, Class<?> returnType) {
        try {
            Object obj = returnType.newInstance();
            BeanUtilsBean2.getInstance().populate(obj, map);
            return obj;
        } catch (Exception e) {
            log.error("map to bean error", e);
            throw new CoryException(ErrorCode.DB_ERROR, "map to bean error" + e.getMessage());
        }
    }

    private static class DateTimeConverter implements Converter {

        @Override
        public <T> T convert(Class<T> type, Object value) {
            if (null == value || !(value instanceof String)) {
                return (T) value;
            }
            String str = (String) value;
            try {
                if (type.equals(Date.class)) {
                    return (T) DateUtils.parseDate(str, Constants.ALL_DATE_FORMAT);
                } else if (type.equals(Timestamp.class)) {
                    return (T) new Timestamp(DateUtils.parseDate(str, Constants.ALL_DATE_FORMAT).getTime());
                }
            } catch (ParseException e) {
            }
            return (T) value;
        }
    }
}
