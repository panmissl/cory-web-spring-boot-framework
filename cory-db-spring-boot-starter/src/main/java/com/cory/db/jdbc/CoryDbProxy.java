package com.cory.db.jdbc;

import com.cory.constant.ErrorCode;
import com.cory.context.CurrentUser;
import com.cory.db.annotations.*;
import com.cory.db.datapermission.DataPermission;
import com.cory.db.datapermission.DataPermissionResult;
import com.cory.db.datapermission.DataPermissionStrategy;
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
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Collectors;

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
    private boolean logicDelete;
    private Map<String, DataPermission> dataPermissionCodeMap = new HashMap<>();

    public CoryDbProxy(Class<T> daoClass, CoryDb coryDb, boolean logEnable, List<DataPermission> dataPermissionList) {
        this.daoClass = daoClass;
        this.coryDb = coryDb;
        this.logEnable = logEnable;

        if (CollectionUtils.isNotEmpty(dataPermissionList)) {
            dataPermissionList.forEach(dp -> dataPermissionCodeMap.put(dp.code(), dp));
        }

        Dao dao = daoClass.getAnnotation(Dao.class);
        this.modelClass = dao.model();

        Model model = modelClass.getAnnotation(Model.class);
        noTable = null == model ? false : model.noTable();

        this.table = CoryModelUtil.buildTableName(modelClass);
        this.logicDelete = model.logicDelete();
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

        if (method.getName().equals(GET_BY_CODE) && (null == args || args.length == 0 || null == args[0])) {
            return null;
        }
        if (method.getName().equals(GET_BY_CODE_LIST) && (null == args || args.length == 0)) {
            return new ArrayList<>();
        }

        checkForSelectCount(method, select.count());

        Map<String, Object> paramMap = buildNotNullParamMap(method, args);
        DataPermissionResult dataPermissionResult = calculateDataPermissionResult(select, paramMap);
        CorySelectSqlBuilder builder = CorySqlBuilder.createSelectBuilder(table, select.whereSql(), select.whereByModel(), select.orderBy(), select.limit(), select.customSql(), paramMap, dataPermissionResult);

        if (select.whereByModel()) {
            Object model = paramMap.get("model");
            //model可能为空
            //AssertUtils.isTrue(null != model && (model instanceof BaseModel), "whereByModel为true时，必须有名为model、类型为T extends BaseModel的参数", ErrorCode.DB_ERROR);
            if (null != model) {
                Map<String, Object> columns = ClassUtil.fetchProperties(model, modelClass, Field.class);
                if (MapUtils.isNotEmpty(columns)) {
                    columns.entrySet().stream().filter(entry -> null != entry.getValue()).forEach(entry -> builder.column(entry.getKey(), toValue(entry.getValue())));
                }
                //filter里的那些值，也要处理一下：toValue
                if (model instanceof BaseModel) {
                    BaseModel baseModel = (BaseModel) model;
                    if (MapUtils.isNotEmpty(baseModel.getFilterFieldMap())) {
                        baseModel.getFilterFieldMap().keySet().forEach(key -> baseModel.getFilterFieldMap().put(key, toValue(baseModel.getFilterFieldMap().get(key))));
                    }
                    //支持按ID搜索，但ID要大于0才算，小于或等于0的不算
                    if (null != baseModel.getId() && baseModel.getId() > 0) {
                        builder.column("id", baseModel.getId());
                    }
                }
            }
        }

        CorySqlInfo dataSqlInfo = builder.buildDataSql();

        if (logEnable) {
            log.info(dataSqlInfo.toString());
        }

        if (select.count()) {
            CorySqlInfo countSqlInfo = builder.buildCountSql();

            if (logEnable) {
                log.info(countSqlInfo.toString());
            }

            return countSqlInfo.getSelectDenied() ? 0 : coryDb.selectCount(countSqlInfo);
        }

        Class selectReturnType = select.returnType();
        Class<?> returnType = method.getReturnType();
        if (returnType.equals(Pagination.class)) {
            CorySqlInfo countSqlInfo = builder.buildCountSql();

            if (logEnable) {
                log.info(countSqlInfo.toString());
            }

            int count = countSqlInfo.getSelectDenied() ? 0 : coryDb.selectCount(countSqlInfo);
            Pagination p = new Pagination<>();
            //pagination直接用modelClass，因为解析不到泛型
            Class<?> cls = ClassUtil.parseGenericType(returnType);
            if (null == cls || cls.equals(Object.class)) {
                //支持自定义返回类型
                cls = null != selectReturnType && !Void.class.equals(selectReturnType) ? selectReturnType : modelClass;
            }
            p.setTotalCount(count);

            if (count > 0) {
                List<Map<String, Object>> listData = coryDb.select(dataSqlInfo);
                p.setList((List) ResultMapperFactory.parseMapper(List.class, modelClass, selectReturnType).getLeft().map(listData, cls));
            }

            return p;
        } else {
            List<Map<String, Object>> listData = dataSqlInfo.getSelectDenied() ? Lists.newArrayList() : coryDb.select(dataSqlInfo);
            Pair<ResultMapper, Class<?>> pair = ResultMapperFactory.parseMapper(returnType, modelClass, selectReturnType);
            return pair.getLeft().map(listData, pair.getRight());
        }
    }

    private DataPermissionResult calculateDataPermissionResult(Select select, Map<String, Object> paramMap) {
        //将多个合并成一个，如果遇到deny的，直接返回deny
        if (MapUtils.isEmpty(dataPermissionCodeMap)) {
            return null;
        }
        String[] codeArr = select.dataPermission();
        if (null == codeArr || codeArr.length == 0) {
            return null;
        }

        //optimization for 1
        if (codeArr.length == 1) {
            DataPermission permission = dataPermissionCodeMap.get(codeArr[0]);
            AssertUtils.notNull(permission, "db.data.permission.error", "invalid data permission: no data permission implement");
            return permission.permission(paramMap);
        }

        boolean pass = true;
        boolean deny = false;
        StringBuilder sql = new StringBuilder();
        for (String code : codeArr) {
            DataPermission permission = dataPermissionCodeMap.get(code);
            AssertUtils.notNull(permission, "db.data.permission.error", "invalid data permission: no data permission implement");
            DataPermissionResult result = permission.permission(paramMap);

            if (DataPermissionStrategy.DENY.equals(result.getStrategy())) {
                pass = false;
                deny = true;
                break;
            } else if (DataPermissionStrategy.FILTER.equals(result.getStrategy())) {
                pass = false;
                sql.append("(").append(result.getFilterSql()).append(") AND");
            }
        }
        if (sql.length() > 0) {
            //delete last AND
            sql.delete(sql.length() - 3, sql.length());
        }
        return DataPermissionResult.builder()
                .filterSql(pass || deny ? null : sql.toString())
                .strategy(deny ? DataPermissionStrategy.DENY : pass ? DataPermissionStrategy.PASS : DataPermissionStrategy.FILTER)
                .build();
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
        CorySqlInfo sqlInfo = CorySqlBuilder.createDeleteBuilder(table, delete.whereSql(), logicDelete, paramMap).build();

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
            model.setCreator(null == user || null == user.getId() ? 1 : user.getId());
        }
        if (null == model.getModifier()) {
            model.setModifier(null == user || null == user.getId() ? 1 : user.getId());
        }
        if (null == model.getCreateTime()) {
            model.setCreateTime(new Date());
        }
        if (null == model.getModifyTime()) {
            model.setModifyTime(new Date());
        }
    }

    public static <T> T newMapperProxy(Class<T> daoClass, CoryDb coryDb, boolean logEnable, List<DataPermission> dataPermissionList) {
        ClassLoader classLoader = daoClass.getClassLoader();
        Class<?>[] interfaces = new Class[]{daoClass};
        CoryDbProxy proxy = new CoryDbProxy(daoClass, coryDb, logEnable, dataPermissionList);
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
            return null;
        }
        if (o instanceof CoryEnum) {
            return ((CoryEnum)o).name();
        }
        //list里如果是枚举，也要转换
        if (o instanceof List) {
            List list = (List) o;
            if (list.size() > 0 && list.get(0) instanceof CoryEnum) {
                return list.stream().map(i -> ((CoryEnum)i).name()).collect(Collectors.toList());
            }
        }
        return o;
    }

    public static void main(String[] args) {
        List<Integer> l = Lists.newArrayList(1, 2, 3);
        System.out.println(List.class.isAssignableFrom(l.getClass()));
        System.out.println((l instanceof List));
    }
}
