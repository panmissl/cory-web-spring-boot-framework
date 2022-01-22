package com.cory.db.jdbc;

import com.cory.db.annotations.Field;
import com.cory.db.annotations.Model;
import com.cory.model.BaseModel;
import com.cory.util.ClassUtil;
import com.google.common.base.CaseFormat;

import java.util.Map;

import static com.cory.constant.Constants.BASE_MODEL_COLUMNS;

/**
 * Created by Cory on 2021/2/17.
 */
public class CoryModelUtil {

    public static String buildTableName(Class<? extends BaseModel> modelClass) {
        Model model = modelClass.getAnnotation(Model.class);
        String modelName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, modelClass.getSimpleName());
        return model.module() + "_" + modelName;
    }

    public static String buildColumnName(String javaFieldName) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, javaFieldName);
    }

    /**
     * 解析model类里带Field注解的字段，且带上baseModel里的字段
     * @param object
     * @param modelClass
     * @return
     */
    public static Map<String, Object> parseModelFieldsValueWithBaseModel(Object object, Class modelClass) {
        Map<String, Object> columns = ClassUtil.fetchProperties(object, modelClass, Field.class);
        Map<String, Object> baseColumns = ClassUtil.fetchProperties(object, modelClass, null);

        for (String c : BASE_MODEL_COLUMNS) {
            Object value = baseColumns.get(c);
            if (null != value) {
                columns.put(c, value);
            }
        }
        return columns;
    }
}
