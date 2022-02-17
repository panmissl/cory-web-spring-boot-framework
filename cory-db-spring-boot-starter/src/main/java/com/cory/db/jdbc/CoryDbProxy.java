package com.cory.db.jdbc;

import com.cory.constant.ErrorCode;
import com.cory.context.CurrentUser;
import com.cory.db.annotations.*;
import com.cory.db.enums.CustomSqlType;
import com.cory.db.jdbc.CorySqlBuilder.CoryInsertSqlBuilder;
import com.cory.db.jdbc.CorySqlBuilder.CorySelectSqlBuilder;
import com.cory.db.jdbc.CorySqlBuilder.CorySqlInfo;
import com.cory.db.jdbc.mapper.ResultMapper;
import com.cory.db.jdbc.mapper.ResultMapperFactory;
import com.cory.enums.CoryEnum;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Cory on 2021/2/9.
 */
@Slf4j
public class CoryDbProxy<T> implements InvocationHandler {

    private static final String DAO_MISSING_ANNOTATION_MSG = "Dao方法上，注解Insert、Update、Delete、Select、UpdateModel、Sql必须有且仅有一个";

    private static final String CODE = "code";
    private static final String GET_BY_CODE = "getByCode";
    private static final String GET_BY_CODE_LIST = "getByCodeList";

    private CoryDb coryDb;
    private Class<T> daoClass;
    private Class<? extends BaseModel> modelClass;
    private String table;
    private boolean noTable;
    private boolean logEnable;

    public CoryDbProxy(Class<T> daoClass, CoryDb coryDb, boolean logEnable) {
        this.daoClass = daoClass;
        this.coryDb = coryDb;
        this.logEnable = logEnable;

        Dao dao = daoClass.getAnnotation(Dao.class);
        this.modelClass = dao.model();

        Model model = modelClass.getAnnotation(Model.class);
        noTable = null == model ? false : model.noTable();

        this.table = CoryModelUtil.buildTableName(modelClass);
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
        Sql sql = method.getAnnotation(Sql.class);

        int opCount = (null == insert ? 0 : 1) + (null == update ? 0 : 1) + (null == delete ? 0 : 1) + (null == select ? 0 : 1) + (null == updateModel ? 0 : 1) + (null == sql ? 0 : 1);
        AssertUtils.isTrue(opCount == 1, DAO_MISSING_ANNOTATION_MSG, ErrorCode.DB_ERROR);

        if (noTable && null == sql) {
            AssertUtils.isTrue(false, "noTable时只能有用Sql注解的方法.", ErrorCode.DB_ERROR);
        }

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
        } else if (null != sql) {
            return executeCustomSql(method, args, sql);
        }
        throw new CoryException(ErrorCode.DB_ERROR, DAO_MISSING_ANNOTATION_MSG);
    }

    private Object executeCustomSql(Method method, Object[] args, Sql sql) {
        AssertUtils.isTrue(null != args && args.length == 1 && args[0].getClass().equals(String.class), "Sql时有且只能有一个类型是String的参数", ErrorCode.DB_ERROR);

        String sqlValue = (String) args[0];

        if (logEnable) {
            log.info("type: {}, sql: {}", sql.type().name(), sqlValue);
        }

        if (CustomSqlType.DDL.equals(sql.type())) {
            AssertUtils.isTrue(method.getReturnType().equals(Void.TYPE), "DDL Sql返回值类型必须是void", ErrorCode.DB_ERROR);
            coryDb.executeSql(sqlValue);
            return Void.TYPE;
        } else if (CustomSqlType.EXECUTE.equals(sql.type())) {
            AssertUtils.isTrue(method.getReturnType().equals(Integer.TYPE), "Sql时返回值类型必须是int", ErrorCode.DB_ERROR);
            return coryDb.update(sqlValue);
        } else if (CustomSqlType.QUERY.equals(sql.type())) {
            AssertUtils.isTrue(method.getReturnType().equals(List.class), "Sql时返回值类型必须是List<Map<String, Object>>", ErrorCode.DB_ERROR);
            return coryDb.query(sqlValue);
        } else {
            throw new CoryException(ErrorCode.DB_ERROR, "sql类型错误!");
        }
    }

    private int updateModel(Method method, Object[] args, UpdateModel updateModel) {
        AssertUtils.isTrue(null != args && args.length == 1 && args[0].getClass().equals(modelClass), "UpdateModel时有且只能有一个类型是：" + modelClass + "的参数", ErrorCode.DB_ERROR);

        Object model = args[0];
        if (model instanceof BaseModel) {
            resetDateAndOperator((BaseModel) model);
        }
        Map<String, Object> columns = CoryModelUtil.parseModelFieldsValueWithBaseModel(model, modelClass);

        CorySqlBuilder.CoryUpdateModelSqlBuilder builder = CorySqlBuilder.createUpdateModelBuilder(table);
        if (MapUtils.isNotEmpty(columns)) {
            columns.entrySet().forEach(entry -> builder.column(entry.getKey(), toValue(entry.getValue())));
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
        checkForSelectCount(method, select.count());

        Map<String, Object> paramMap = buildNotNullParamMap(method, args);

        CorySelectSqlBuilder builder = CorySqlBuilder.createSelectBuilder(table, select.whereSql(), select.whereByModel(), select.orderBy(), select.limit(), select.customSql(), paramMap);

        if (select.whereByModel()) {
            Object model = paramMap.get("model");
            //model可能为空
            //AssertUtils.isTrue(null != model && (model instanceof BaseModel), "whereByModel为true时，必须有名为model、类型为T extends BaseModel的参数", ErrorCode.DB_ERROR);
            if (null != model) {
                Map<String, Object> columns = ClassUtil.fetchProperties(model, modelClass, Field.class);
                if (MapUtils.isNotEmpty(columns)) {
                    columns.entrySet().forEach(entry -> builder.column(entry.getKey(), toValue(entry.getValue())));
                }
            }
        }

        CorySqlInfo dataSqlInfo = builder.buildDataSql();

        if (logEnable) {
            log.info(dataSqlInfo.toString());
        }

        Class selectReturnType = select.returnType();
        List<Map<String, Object>> listData = coryDb.select(dataSqlInfo);

        if (select.count()) {
            CorySqlInfo countSqlInfo = builder.buildCountSql();

            if (logEnable) {
                log.info(countSqlInfo.toString());
            }

            return coryDb.selectCount(countSqlInfo);
        }

        Class<?> returnType = method.getReturnType();
        if (returnType.equals(Pagination.class)) {
            CorySqlInfo countSqlInfo = builder.buildCountSql();

            if (logEnable) {
                log.info(countSqlInfo.toString());
            }

            int count = coryDb.selectCount(countSqlInfo);
            Pagination p = new Pagination<>();
            //pagination直接用modelClass，因为解析不到泛型
            Class<?> cls = ClassUtil.parseGenericType(returnType);
            if (null == cls || cls.equals(Object.class)) {
                //支持自定义返回类型
                cls = null != selectReturnType && !Void.class.equals(selectReturnType) ? selectReturnType : modelClass;
            }
            p.setList((List) ResultMapperFactory.parseMapper(List.class, modelClass, selectReturnType).getLeft().map(listData, cls));
            p.setTotalCount(count);

            return p;
        } else {
            Pair<ResultMapper, Class<?>> pair = ResultMapperFactory.parseMapper(returnType, modelClass, selectReturnType);
            return pair.getLeft().map(listData, pair.getRight());
        }
    }

    private void checkForSelectCount(Method method, boolean count) {
        if (!count) {
            return;
        }
        AssertUtils.isTrue(Integer.class.equals(method.getReturnType()), "查询记录数(count = true)时，返回值必须是Integer", ErrorCode.DB_ERROR);
    }

    private void checkForSelectByCodeMethod(Method method) {
        if (!method.getName().equals(GET_BY_CODE_LIST) && !method.getName().equals(GET_BY_CODE)) {
            return;
        }
        CoryException ex = new CoryException(ErrorCode.DB_ERROR, "model类" + modelClass + "没有code字段(并且需要加上Field注解)，不能调用" + method.getName() + "方法");
        try {
            java.lang.reflect.Field javaField = modelClass.getDeclaredField(CODE);
            if (null == javaField || !javaField.isAnnotationPresent(Field.class)) {
                throw ex;
            }
        } catch (NoSuchFieldException e) {
            throw ex;
        }
    }

    private int delete(Method method, Object[] args, Delete delete) {
        Map<String, Object> paramMap = buildNotNullParamMap(method, args);
        CorySqlInfo sqlInfo = CorySqlBuilder.createDeleteBuilder(table, delete.whereSql(), delete.logicDelete(), paramMap).build();

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
        if (model instanceof BaseModel) {
            resetDateAndOperator((BaseModel) model);
        }
        Map<String, Object> columns = CoryModelUtil.parseModelFieldsValueWithBaseModel(model, modelClass);

        CoryInsertSqlBuilder builder = CorySqlBuilder.createInsertBuilder(table);
        if (MapUtils.isNotEmpty(columns)) {
            columns.entrySet().forEach(entry -> builder.column(entry.getKey(), toValue(entry.getValue())));
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

    private void resetDateAndOperator(BaseModel model) {
        CurrentUser user = CurrentUser.get();

        if (null == model.getCreator()) {
            model.setCreator(null == user ? 1 : user.getId());
        }
        if (null == model.getModifier()) {
            model.setModifier(null == user ? 1 : user.getId());
        }
        if (null == model.getCreateTime()) {
            model.setCreateTime(new Date());
        }
        if (null == model.getModifyTime()) {
            model.setModifyTime(new Date());
        }
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

            paramMap.put(param.value(), toValue(args[i]));
        }
        return paramMap;
    }

    private Object toValue(Object o) {
        if (null == o) {
            return o;
        }
        if (o instanceof CoryEnum) {
            return ((CoryEnum)o).name();
        }
        return o;
    }
}
