package com.cory.db.jdbc.mapper;

import com.cory.constant.ErrorCode;
import com.cory.exception.CoryException;
import com.cory.util.DateUtils;
import com.cory.util.MapBuilder;
import com.google.common.base.CaseFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.collections4.MapUtils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

/**
 * Created by Cory on 2021/2/12.
 */
@Slf4j
public class BeanMapper extends SingleResultMapper {

    @Override
    protected Object doMap(Map<String, Object> map, Class<?> returnType) {
        try {
            MapBuilder builder = MapBuilder.create(String.class, Object.class);
            //下划线转驼峰
            if (MapUtils.isNotEmpty(map)) {
                map.entrySet().forEach(entry -> builder.put(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, entry.getKey()), entry.getValue()));
            }

            BeanUtilsBean util = newBeanUtilsBean();

            Object obj = returnType.newInstance();
            util.populate(obj, builder.build());
            return obj;
        } catch (Exception e) {
            log.error("map to bean error", e);
            throw new CoryException(ErrorCode.DB_ERROR, "map to bean error" + e.getMessage());
        }
    }

    private BeanUtilsBean newBeanUtilsBean() {
        DateTimeConverter converter = new DateTimeConverter();

        ConvertUtilsBean convertUtilsBean = new ConvertUtilsBean();
        convertUtilsBean.deregister(Date.class);
        convertUtilsBean.deregister(Timestamp.class);

        convertUtilsBean.register(converter, Date.class);
        convertUtilsBean.register(converter, Timestamp.class);

        return new BeanUtilsBean(convertUtilsBean, new PropertyUtilsBean());
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
                    return (T) DateUtils.parseDate(str);
                } else if (type.equals(Timestamp.class)) {
                    return (T) new Timestamp(DateUtils.parseDate(str).getTime());
                }
            } catch (ParseException e) {
            }
            return (T) value;
        }
    }
}
