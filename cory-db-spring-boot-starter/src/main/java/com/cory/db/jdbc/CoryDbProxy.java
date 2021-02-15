package com.cory.db.jdbc;

import com.cory.constant.ErrorCode;
import com.cory.db.annotations.*;
import com.cory.db.jdbc.CorySqlBuilder.CoryInsertSqlBuilder;
import com.cory.db.jdbc.CorySqlBuilder.CorySelectSqlBuilder;
import com.cory.db.jdbc.CorySqlBuilder.CorySqlInfo;
import com.cory.db.jdbc.mapper.ResultMapper;
import com.cory.db.jdbc.mapper.ResultMapperFactory;
import com.cory.exception.CoryException;
import com.cory.model.BaseModel;
import com.cory.page.Pagination;
import com.cory.util.AssertUtils;
import com.cory.util.ClassUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Cory on 2021/2/9.
 */
@Slf4j
public class CoryDbProxy<T> implements InvocationHandler {

    private static final String DAO_MISSING_ANNOTATION_MSG = "Dao方法上，注解Insert、Update、Delete、Select、UpdateModel必须有且仅有一个";

    private static final String CODE = "code";
    private static final String GET_BY_CODE = "getByCode";
    private static final String GET_BY_CODE_LIST = "getByCodeList";

    private CoryDb coryDb;
    private Class<T> daoClass;
    private Class<? extends BaseModel> modelClass;
    private String table;
    private boolean logEnable;

    public CoryDbProxy(Class<T> daoClass, CoryDb coryDb, boolean logEnable) {
        this.daoClass = daoClass;
        this.coryDb = coryDb;
        this.logEnable = logEnable;

        Dao dao = daoClass.getAnnotation(Dao.class);
        this.modelClass = dao.model();

        Model model = modelClass.getAnnotation(Model.class);
        this.table = model.table();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }

        Insert insert = method.getAnnotation(Insert.class);
        Update update = method.getAnnotation(Update.class);
        Delete delete = method.getAnnotation(Delete.class);
        Select select = method.getAnnotation(Select.class);
        UpdateModel updateModel = method.getAnnotation(UpdateModel.class);

        int opCount = (null == insert ? 0 : 1) + (null == update ? 0 : 1) + (null == delete ? 0 : 1) + (null == select ? 0 : 1) + (null == updateModel ? 0 : 1);
        AssertUtils.isTrue(opCount == 1, DAO_MISSING_ANNOTATION_MSG, ErrorCode.DB_ERROR);

        if (null != insert) {
            return insert(method, args, insert);
        } else if (null != update) {
            return update(method, args, update);
        } else if (null != delete) {
            return delete(method, args, delete);
        } else if (null != select) {
            return select(method, args, select);
        } else if (null != updateModel) {
            return updateModel(method, args, updateModel);
        }
        throw new CoryException(ErrorCode.DB_ERROR, DAO_MISSING_ANNOTATION_MSG);
    }

    private int updateModel(Method method, Object[] args, UpdateModel updateModel) {
        AssertUtils.isTrue(null != args && args.length == 1 && args[0].getClass().equals(modelClass), "UpdateModel时有且只能有一个类型是：" + modelClass + "的参数", ErrorCode.DB_ERROR);

        Object model = args[0];
        Map<String, Object> columns = parseModelColumnsWithBaseModel(model);

        CorySqlBuilder.CoryUpdateModelSqlBuilder builder = CorySqlBuilder.createUpdateModelBuilder(table);
        if (MapUtils.isNotEmpty(columns)) {
            columns.entrySet().forEach(entry -> builder.column(entry.getKey(), entry.getValue()));
        }
        CorySqlBuilder.CorySqlInfo sqlInfo = builder.build();

        if (logEnable) {
            log.info(sqlInfo.toString());
        }

        return coryDb.update(sqlInfo);
    }

    private Object select(Method method, Object[] args, Select select) {
        //check for byCode
        checkForSelectByCodeMethod(method);

        Map<String, Object> paramMap = buildNotNullParamMap(method, args);

        CorySelectSqlBuilder builder = CorySqlBuilder.createSelectBuilder(table, select.whereSql(), select.whereByModel(), select.orderBy(), select.limit(), select.customSql(), paramMap);

        if (select.whereByModel()) {
            Object model = paramMap.get("model");
            AssertUtils.isTrue(null != model && (model instanceof BaseModel), "whereByModel为true时，必须有名为model、类型为T extends BaseModel的参数", ErrorCode.DB_ERROR);

            Map<String, Object> columns = ClassUtil.fetchProperties(model, modelClass, Field.class);
            if (MapUtils.isNotEmpty(columns)) {
                columns.entrySet().forEach(entry -> builder.column(entry.getKey(), entry.getValue()));
            }
        }

        CorySqlInfo dataSqlInfo = builder.buildDataSql();

        if (logEnable) {
            log.info(dataSqlInfo.toString());
        }

        List<Map<String, Object>> listData = coryDb.select(dataSqlInfo);

        Class<?> returnType = method.getReturnType();
        if (returnType.equals(Pagination.class)) {
            CorySqlInfo countSqlInfo = builder.buildCountSql();

            if (logEnable) {
                log.info(countSqlInfo.toString());
            }

            int count = coryDb.selectCount(countSqlInfo);
            Pagination p = new Pagination<>();
            p.setList((List) ResultMapperFactory.parseMapper(List.class).getLeft().map(listData, ClassUtil.parseGenericType(returnType)));
            p.setTotalCount(count);

            return p;
        } else {
            Pair<ResultMapper, Class<?>> pair = ResultMapperFactory.parseMapper(returnType);
            return pair.getLeft().map(listData, pair.getRight());
        }
    }

    private void checkForSelectByCodeMethod(Method method) {
        if (!method.getName().equals(GET_BY_CODE_LIST) && !method.getName().equals(GET_BY_CODE)) {
            return;
        }
        CoryException ex = new CoryException(ErrorCode.DB_ERROR, "model类" + modelClass + "没有code字段(并且需要加上Field注解)，不能调用" + method.getName() + "方法");
        try {
            java.lang.reflect.Field javaField = modelClass.getField(CODE);
            if (null == javaField || !javaField.isAnnotationPresent(Field.class)) {
                throw ex;
            }
        } catch (NoSuchFieldException e) {
            throw ex;
        }
    }

    private int delete(Method method, Object[] args, Delete delete) {
        Map<String, Object> paramMap = buildNotNullParamMap(method, args);
        CorySqlInfo sqlInfo = CorySqlBuilder.createDeleteBuilder(table, delete.whereSql(), paramMap).build();

        if (logEnable) {
            log.info(sqlInfo.toString());
        }

        return coryDb.delete(sqlInfo);
    }

    private int update(Method method, Object[] args, Update update) {
        Map<String, Object> paramMap = buildNotNullParamMap(method, args);
        CorySqlInfo sqlInfo = CorySqlBuilder.createUpdateBuilder(table, update.columnSql(), update.whereSql(), paramMap).build();

        if (logEnable) {
            log.info(sqlInfo.toString());
        }

        return coryDb.update(sqlInfo);
    }

    private Object insert(Method method, Object[] args, Insert insert) {
        AssertUtils.isTrue(null != args && args.length == 1 && args[0].getClass().equals(modelClass), "插入时有且只能有一个类型是：" + modelClass + "的参数", ErrorCode.DB_ERROR);

        Object model = args[0];
        Map<String, Object> columns = parseModelColumnsWithBaseModel(model);

        CoryInsertSqlBuilder builder = CorySqlBuilder.createInsertBuilder(table);
        if (MapUtils.isNotEmpty(columns)) {
            columns.entrySet().forEach(entry -> builder.column(entry.getKey(), entry.getValue()));
        }

        CorySqlBuilder.CorySqlInfo sqlInfo = builder.build();

        if (logEnable) {
            log.info(sqlInfo.toString());
        }

        int id = coryDb.insert(sqlInfo);

        //生成的ID设置到model里
        if (model instanceof BaseModel) {
            ((BaseModel) model).setId(id);
        }

        return Void.TYPE;
    }

    public static <T> T newMapperProxy(Class<T> daoClass, CoryDb coryDb, boolean logEnable) {
        ClassLoader classLoader = daoClass.getClassLoader();
        Class<?>[] interfaces = new Class[]{daoClass};
        CoryDbProxy proxy = new CoryDbProxy(daoClass, coryDb, logEnable);
        return (T) Proxy.newProxyInstance(classLoader, interfaces, proxy);
    }

    private Map<String,Object> buildNotNullParamMap(Method method, Object[] args) {
        Map<String, Object> paramMap = new HashMap<>();

        if (null == args || args.length == 0) {
            return paramMap;
        }
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < args.length; i++) {
            Parameter parameter = parameters[i];
            Param param = parameter.getAnnotation(Param.class);
            AssertUtils.notNull(param, "参数(" + parameter.getName() + ")必须写Param注解", ErrorCode.DB_ERROR);

            paramMap.put(param.value(), args[i]);
        }
        return paramMap;
    }

    private Map<String, Object> parseModelColumnsWithBaseModel(Object object) {
        Map<String, Object> columns = ClassUtil.fetchProperties(object, modelClass, Field.class);
        Map<String, Object> baseColumns = ClassUtil.fetchProperties(object, modelClass, null);

        for (String c : new String[] {"id", "creator", "modifier", "createTime", "modifyTime", "isDeleted"}) {
            Object value = baseColumns.get(c);
            if (null != value) {
                columns.put(c, value);
            }
        }
        return columns;
    }

}
