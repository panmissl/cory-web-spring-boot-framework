package com.cory.web.converter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONValidator;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.cory.constant.Constants;
import com.cory.util.DateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class CoryWebFastJsonHttpMessageConverter extends FastJsonHttpMessageConverter {

    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return readType(getType(type, contextClass), inputMessage);
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return readType(getType(clazz, null), inputMessage);
    }

    private Object readType(Type type, HttpInputMessage inputMessage) {
        try {
            List<String> lines = IOUtils.readLines(inputMessage.getBody(), Constants.UTF8);
            if (CollectionUtils.isEmpty(lines)) {
                return null;
            }
            String body = String.join("", lines);
            if (JSONValidator.from(body).validate()) {
                return JSON.parseObject(body, type, JSON.DEFAULT_PARSER_FEATURE,
                        getFastJsonConfig().getFeatures());
            }
            return parseOriginal(type, body);
        } catch (JSONException ex) {
            throw new HttpMessageNotReadableException("JSON parse error: " + ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new HttpMessageNotReadableException("I/O error while reading input message", ex);
        } catch (ParseException e) {
            throw new HttpMessageNotReadableException("I/O error while reading input message", e);
        }
    }

    private Object parseOriginal(Type type, String body) throws ParseException {
        //TODO FIXME 对于数组类型，比如byte[]之类的，目前无法转换
        if (null == type) {
            return body;
        }
        if (String.class.equals(type)) {
            return body;
        }
        if (Long.class.equals(type)) {
            return Long.valueOf(body);
        }
        if (Integer.class.equals(type)) {
            return Integer.valueOf(body);
        }
        if (Double.class.equals(type)) {
            return Double.valueOf(body);
        }
        if (Short.class.equals(type)) {
            return Short.valueOf(body);
        }
        if (Boolean.class.equals(type)) {
            return Boolean.valueOf(body);
        }
        if (Float.class.equals(type)) {
            return Float.valueOf(body);
        }
        if (Byte.class.equals(type)) {
            return Byte.valueOf(body);
        }
        if (Date.class.equals(type)) {
            return DateUtils.parseDate(body);
        }
        if (Timestamp.class.equals(type)) {
            return new Timestamp(DateUtils.parseDate(body).getTime());
        }
        return body;
    }
}
