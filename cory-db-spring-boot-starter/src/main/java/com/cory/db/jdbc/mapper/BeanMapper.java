package com.cory.db.jdbc.mapper;

import com.cory.constant.ErrorCode;
import com.cory.context.CorySystemContext;
import com.cory.enums.CoryEnum;
import com.cory.exception.CoryException;
import com.cory.model.BaseModel;
import com.cory.util.ClassUtil;
import com.cory.util.DateUtils;
import com.cory.util.MapBuilder;
import com.google.common.base.CaseFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Created by Cory on 2021/2/12.
 */
@Slf4j
public class BeanMapper extends SingleResultMapper {

    private static final String TEXT = "Text";

    private static final BeanUtilsBean UTIL = newBeanUtilsBean();

    @Override
    protected Object doMap(Map<String, Object> map, Class<?> returnType) {
        try {
            MapBuilder builder = MapBuilder.create(String.class, Object.class);
            //下划线转驼峰
            if (MapUtils.isNotEmpty(map)) {
                map.entrySet().forEach(entry -> builder.put(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, entry.getKey()), entry.getValue()));
            }

            Object obj = returnType.newInstance();
            UTIL.populate(obj, builder.build());
            processRenderName(obj, returnType);
            return obj;
        } catch (Exception e) {
            log.error("map to bean error", e);
            throw new CoryException(ErrorCode.DB_ERROR, "map to bean error" + e.getMessage());
        }
    }

    private void processRenderName(Object obj, Class<?> cls) {
        if (!(obj instanceof BaseModel)) {
            return;
        }
        //目前只处理Boolean、枚举（实现CoryEnum接口）及日期型，后续再有通用类型再加

        //"createTime", "modifyTime", "isDeleted" from Constants.BASE_MODEL_COLUMNS;
        BaseModel baseModel = (BaseModel) obj;
        baseModel.getRenderFieldMap().put("createTimeText", DateUtils.formatFull(baseModel.getCreateTime()));
        baseModel.getRenderFieldMap().put("modifyTimeText", DateUtils.formatFull(baseModel.getModifyTime()));
        baseModel.getRenderFieldMap().put("isDeletedText", null != baseModel.getIsDeleted() && baseModel.getIsDeleted() ? "已删除" : "未删除");

        Field[] fields = obj.getClass().getDeclaredFields();
        if (null == fields || fields.length == 0) {
            return;
        }
        Map<String, Object> fieldMap = ClassUtil.fetchProperties(obj, cls, null);

        for (Field field : fields) {
            String name = field.getName();
            Object value = fieldMap.get(name);
            if (null == value) {
                continue;
            }

            com.cory.db.annotations.Field fieldAnno = field.getAnnotation(com.cory.db.annotations.Field.class);
            String renderName = null == fieldAnno || StringUtils.isBlank(fieldAnno.renderName()) ? name + TEXT : fieldAnno.renderName();

            if (field.getType().equals(Boolean.class)) {
                baseModel.getRenderFieldMap().put(renderName, (Boolean) value ? "是" : "否");
            } else if (CoryEnum.class.isAssignableFrom(field.getType())) {
                baseModel.getRenderFieldMap().put(renderName, ((CoryEnum) value).text());
            } else if (Date.class.isAssignableFrom(field.getType())) {
                baseModel.getRenderFieldMap().put(renderName, DateUtils.formatFull((Date) value));
            }
        }
    }

    private static BeanUtilsBean newBeanUtilsBean() {
        ConvertUtilsBean convertUtilsBean = new ConvertUtilsBean();
        convertUtilsBean.deregister(Date.class);
        convertUtilsBean.deregister(Timestamp.class);

        DateTimeConverter converter = new DateTimeConverter();
        convertUtilsBean.register(converter, Date.class);
        convertUtilsBean.register(converter, Timestamp.class);

        CoryEnumConverter enumConverter = new CoryEnumConverter();
        Set<Class<? extends CoryEnum>> enumSet = CorySystemContext.get().getCoryEnumSet();
        if (CollectionUtils.isNotEmpty(enumSet)) {
            enumSet.forEach(cls -> {
                convertUtilsBean.deregister(cls);
                convertUtilsBean.register(enumConverter, cls);
            });
        }

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

    private static class CoryEnumConverter implements Converter {

        @Override
        public <T> T convert(Class<T> type, Object value) {
            if (null == value || !(value instanceof String)) {
                return (T) value;
            }
            String str = (String) value;
            Class cls = type;
            return (T) Enum.valueOf(cls, str);
        }
    }
}
