package com.cory.db.jdbc;

import com.alibaba.fastjson.JSON;
import com.cory.constant.ErrorCode;
import com.cory.exception.CoryException;
import com.cory.util.AssertUtils;
import com.cory.util.MapBuilder;
import com.cory.util.OgnlUtil;
import com.google.common.base.CaseFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;

/**
 * where 条件，直接写条件，不用加where。
 * 例子：col_a = #{colA} and col_b < #{colB} and col_c > 5 and col_d in #{colDList}
 * <br />
 * 会自动加：is_deleted = 0的条件。
 * <br />
 * 里面可以加参数，写法和mybatis一样，参数从@Param注解里获取。类型为list或set或数组的会自动解析为IN。参数必须写Param注解
 * <br />
 * 如果需要做判空处理，那么在写参数时形式写成这样：col_a = #!{colA}，在#和{}之间加一个叹号。会自动判空
 *
 * Created by Cory on 2021/2/9.
 */
public class CorySqlBuilder {

    private static final String ID = "ID";
    private static final String CREATOR = "CREATOR";
    private static final String MODIFIER = "MODIFIER";
    private static final String CREATE_TIME = "CREATE_TIME";
    private static final String MODIFY_TIME = "MODIFY_TIME";
    private static final String IS_DELETED = "IS_DELETED";

    private static final String PARAM_SORT = "sort";
    private static final String PARAM_PAGE_START = "pageStart";
    private static final String PARAM_PAGE_SIZE = "pageSize";

    private static final String COMMA = ",";
    private static final String SPACE = " ";
    private static final String QUESTION_MARK = "?";
    private static final String EQUAL = "=";
    private static final String IN = "IN";
    private static final String NOT = "NOT";
    private static final String BRACKET = ")";

    private static final String ADD_PATTERN = "and|AND";
    private static final String OP_PATTERN = "=|!=|>|>=|<|<=|in|like|IN|LIKE";
    private static final String PARAM_PATTERN = "#\\{.*?\\}\\)?";
    private static final String NULLABLE_PARAM_PATTERN = "#!\\{.*?\\}\\)?";
    private static final String PARAM_PATTERN_FULL = ".*?#\\{.*?\\}.*";
    private static final String NULLABLE_PARAM_PATTERN_FULL = ".*?#!\\{.*?\\}.*";
    private static final String ALL_SPACE_PATTERN = " +";

    private CorySqlBuilder() {}

    /**
     * 构建insert语句。各个字段及值用column方法添加
     * @param table 表明
     * @return
     */
    public static CoryInsertSqlBuilder createInsertBuilder(String table) {
        return new CoryInsertSqlBuilder(table);
    }

    /**
     * 构建updateModel语句。各个字段及值用column方法添加
     * @param table 表明
     * @return
     */
    public static CoryUpdateModelSqlBuilder createUpdateModelBuilder(String table) {
        return new CoryUpdateModelSqlBuilder(table);
    }

    /**
     * 构建delete语句
     * @param table 表明
     * @param whereSql where语句：直接写条件，不用加where。详见CorySqlBuilder上的注释
     * @return
     */
    public static CoryDeleteSqlBuilder createDeleteBuilder(String table, String whereSql, Map<String, Object> ognlParamMap) {
        return new CoryDeleteSqlBuilder(table, whereSql, ognlParamMap);
    }

    /**
     * 构建update语句
     * @param table 表明
     * @param columnSql 更新语句中，set 后面的sql部分。格式：col_a = #{colA}, col_b = #{colB}, col_c = 'abc', col_d = 5
     * @param whereSql where语句：直接写条件，不用加where。详见CorySqlBuilder上的注释
     * @return
     */
    public static CoryUpdateSqlBuilder createUpdateBuilder(String table, String columnSql, String whereSql, Map<String, Object> ognlParamMap) {
        return new CoryUpdateSqlBuilder(table, columnSql, whereSql, ognlParamMap);
    }

    public static CorySelectSqlBuilder createSelectBuilder(String table, String whereSql, boolean whereModel, boolean orderBy, boolean limit, String customSql, Map<String, Object> ognlParamMap) {
        return new CorySelectSqlBuilder(table, whereSql, whereModel, orderBy, limit, customSql, ognlParamMap);
    }

    private static String formatColumn(String column) {
        //驼峰转下划线分割，然后全部大写
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, column).toUpperCase();
    }

    private static CorySqlInfo parseColumnPart(String columnSql, Map<String, Object> ognlParamMap) {
        //col_a = #{colA}, col_b = #{colB}, col_c = 3
        AssertUtils.hasText(columnSql, "column sql 不能为空", ErrorCode.DB_ERROR);

        String[] columns = columnSql.split(COMMA);
        List<String> sqlList = new ArrayList<>();
        List<Object> paramList = new ArrayList<>();

        for (String column : columns) {
            //xx = #{xx}
            String[] arr = column.split(EQUAL);
            AssertUtils.isTrue(arr.length == 2, "column sql 格式错误(" + column + "). sql: " + columnSql, ErrorCode.DB_ERROR);

            String name = arr[0].trim();
            String param = arr[1].trim();
            boolean isVariable = false;
            Object value = param;
            if (param.matches(PARAM_PATTERN)) {
                //remove #{ and }
                param = param.trim().substring(2);
                param = param.substring(0, param.length() - 1);

                value = OgnlUtil.get(ognlParamMap, param);
                AssertUtils.notNull(value, "字段(" + param + ")未设置值. sql: " + columnSql + ", paramMap: " + JSON.toJSONString(ognlParamMap), ErrorCode.DB_ERROR);
                isVariable = true;
            }
            if (isVariable) {
                sqlList.add(name + " = ?");
                paramList.add(value);
            } else {
                sqlList.add(name + " = " + value);
            }
        }
        return CorySqlInfo.builder().sql(StringUtils.join(sqlList, COMMA)).params(paramList).build();
    }

    /**
     * @param whereSql
     * @param ognlParamMap
     * @param wrapAnd 自定义sql的地方，不要包裹AND ()
     * @return
     */
    private static CorySqlInfo parseWherePart(String whereSql, Map<String, Object> ognlParamMap, boolean wrapAnd) {
        if (StringUtils.isBlank(whereSql)) {
            return CorySqlInfo.builder().sql("").build();
        }

        //col_a = #{colA} and col_b like #{colB} and exists (select 1 from xx where ff in #{ss})
        String[] conditions = whereSql.split(ADD_PATTERN);
        List<String> whereList = new ArrayList<>();
        List<Object> paramList = new ArrayList<>();

        for (int i = 0; i < conditions.length; i++) {
            String condition = conditions[i];

            condition = buildConditionAndAddParam(condition, paramList, ognlParamMap);
            if (StringUtils.isNotBlank(condition)) {
                whereList.add(condition);
            }
        }

        //处理完还是没有条件，则返回空即可
        if (whereList.size() == 0) {
            return CorySqlInfo.builder().sql("").build();
        }

        // AND (col_a = ? and col_b = ?)
        String sql = StringUtils.join(whereList, " AND ");
        if (wrapAnd) {
            sql = " AND (" + sql + ")";
        }
        return CorySqlInfo.builder().sql(sql).params(paramList).build();
    }

    private static String buildConditionAndAddParam(String condition, List<Object> paramList, Map<String, Object> ognlParamMap) {
        //col_a = #{colA}
        //col_b = #!{colB}
        //col_c = 55
        //col_d like 'abd%'
        //(col_e = 'abc')

        //如果不包含变量，直接返回即可
        if (!condition.matches(PARAM_PATTERN_FULL) && !condition.matches(NULLABLE_PARAM_PATTERN_FULL)) {
            return condition;
        }

        StringBuilder builder = new StringBuilder();
        String[] arr = condition.split(SPACE);

        for (int i = 0; i < arr.length; i++) {
            builder.append(SPACE);

            String part1 = arr[i];
            String part2 = i < arr.length - 1 ? arr[i + 1] : null;
            String part3 = i < arr.length - 2 ? arr[i + 2] : null;

            //是条件表达式。条件处理，值可能是：无变量、有变量：单个变量、列表变量
            if (null != part2 && part2.matches(OP_PATTERN)) {
                AssertUtils.hasText(part3, "SQL Where语法错误(" + condition + ")", ErrorCode.DB_ERROR);

                //值是不是变量
                VariableInfo variableInfo = parseVariable(part3);

                //变量，处理参数sql和参数值
                if (variableInfo.isVariable()) {
                    //处理not
                    boolean not = false;
                    if (part1.equalsIgnoreCase(NOT)) {
                        not = true;
                        part1 = arr[i - 1];
                    }

                    Object paramValue = OgnlUtil.get(ognlParamMap, variableInfo.getVariableName());

                    if (variableInfo.isNullable() && isNullOrEmpty(paramValue)) {
                        //do nothing: 如果可空且参数值为空，则直接跳过
                        //处理最后一个括号
                        if (variableInfo.haveEndBracket) {
                            builder.append(BRACKET);
                        }
                    } else {
                        //添加整个条件表达式
                        AssertUtils.notNull(paramValue,
                                "参数" + variableInfo.getVariableName() + "的值不能为空. Where: " + condition + ", params: " + JSON.toJSONString(ognlParamMap),
                                ErrorCode.DB_ERROR);
                        builder.append(part1);
                        if (not) {
                            builder.append(SPACE);
                            builder.append(NOT);
                        }
                        builder.append(SPACE);
                        builder.append(part2);
                        builder.append(SPACE);

                        //处理是不是IN条件，如果是IN，则需要处理成IN (?, ?, ?)的格式
                        if (part2.equalsIgnoreCase(IN)) {
                            builder.append(parseInWhereAndAddParam(paramValue, paramList));
                        } else {
                            //不是IN，直接添加
                            builder.append(QUESTION_MARK);
                            paramList.add(paramValue);
                        }
                        //处理最后一个括号
                        if (variableInfo.haveEndBracket) {
                            builder.append(BRACKET);
                        }
                    }

                    //只要是变量，则跳过：变量名 + 表达式 + 变量值
                    i ++;
                    i ++;
                    //这里即使是not，也不用+1，因为not的处理没有多用一个，还是之前的index
                } else {
                    //非变量，直接添加一个即可，不用添加part2和part3
                    builder.append(part1);
                }
            } else {
                //处理not：如果下一个是not，那么不添加，因为后面处理not时会添加。
                boolean nextNot = false;
                if (NOT.equalsIgnoreCase(part2)) {
                    nextNot = true;
                }
                if (!nextNot) {
                    //不是条件表达式，直接添加
                    builder.append(part1);
                }
            }

            builder.append(SPACE);
        }
        return builder.toString();
    }

    private static VariableInfo parseVariable(String paramName) {
        boolean isVariable = false;
        boolean nullable = false;
        int prefixLen = 0;
        boolean bracket = paramName.endsWith(BRACKET);

        if (paramName.matches(PARAM_PATTERN)) {
            prefixLen = 2;
            isVariable = true;
        } else if (paramName.matches(NULLABLE_PARAM_PATTERN)) {
            prefixLen = 3;
            isVariable = true;
            nullable = true;
        }

        //remove #{ or #!{ and }
        paramName = paramName.trim().substring(prefixLen);
        paramName = paramName.substring(0, paramName.length() - 1);

        if (bracket) {
            paramName = paramName.substring(0, paramName.length() - 1);
        }

        return VariableInfo.builder().isVariable(isVariable).variableName(paramName).isNullable(nullable).haveEndBracket(bracket).build();
    }

    private static String parseInWhereAndAddParam(Object paramValue, List<Object> paramList) {
        //(?, ?, ?)
        if (paramValue.getClass().isArray()) {
            int len = Array.getLength(paramValue);
            List<String> list = new ArrayList<>();
            for (int i = 0; i < len; i++) {
                Object item = Array.get(paramValue, i);
                paramList.add(item);
                list.add(QUESTION_MARK);
            }
            return "(" + StringUtils.join(list, COMMA) + ")";
        }
        if (paramValue instanceof Collection<?>) {
            Collection collection = (Collection) paramValue;
            List<String> list = new ArrayList<>();
            for (Object item : collection) {
                paramList.add(item);
                list.add(QUESTION_MARK);
            }
            return "(" + StringUtils.join(list, COMMA) + ")";
        }
        throw new CoryException(ErrorCode.DB_ERROR, "IN 条件目前只支持数组、列表或Set，传入的类型(" + paramValue.getClass() + ")暂不支持");
    }

    private static String formatSql(String sql) {
        if (StringUtils.isBlank(sql)) {
            return sql;
        }
        sql = sql.replaceAll(",", ", ");
        return StringUtils.join(sql.split(ALL_SPACE_PATTERN), SPACE);
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CorySqlInfo implements Serializable {

        private String sql;
        private List<Object> params = new ArrayList<>();

        @Override
        public String toString() {
            return "sql: " + sql + ", params: " + JSON.toJSONString(params);
        }
    }

    @Data
    @Builder
    private static class CorySqlColumnValue implements Serializable {
        /** 字段名：大写，驼峰形式自动转下划线分割 */
        private String column;

        private Object param;
    }

    private static boolean isNullOrEmpty(Object paramValue) {
        return ObjectUtils.isEmpty(paramValue);
    }

    public static class CoryInsertSqlBuilder {

        /** 表名 */
        private String table;

        private List<CorySqlColumnValue> columnValueList = new ArrayList<>();

        CoryInsertSqlBuilder(String table) {
            this.table = table;
        }

        public CoryInsertSqlBuilder column(String column, Object value) {
            AssertUtils.hasText(column, "字段名不能为空", ErrorCode.DB_ERROR);
            columnValueList.add(CorySqlColumnValue.builder().column(formatColumn(column)).param(value).build());
            return this;
        }

        public CorySqlInfo build() {
            //insert into xxx (xx, xx) values (?, ?);

            List<String> columnList = new ArrayList<>();
            List<String> paramPlaceholderList = new ArrayList<>();
            List<Object> paramList = new ArrayList<>();

            columnValueList.forEach(cv -> {
                columnList.add(cv.getColumn());
                paramPlaceholderList.add(QUESTION_MARK);
                paramList.add(cv.getParam());
            });

            String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", table, StringUtils.join(columnList, COMMA), StringUtils.join(paramPlaceholderList, COMMA));
            return CorySqlInfo.builder().sql(formatSql(sql)).params(paramList).build();
        }
    }

    public static class CoryUpdateModelSqlBuilder {

        /** 表名 */
        private String table;

        private List<CorySqlColumnValue> columnValueList = new ArrayList<>();

        CoryUpdateModelSqlBuilder(String table) {
            this.table = table;
        }

        public CoryUpdateModelSqlBuilder column(String column, Object value) {
            AssertUtils.hasText(column, "字段名不能为空", ErrorCode.DB_ERROR);
            columnValueList.add(CorySqlColumnValue.builder().column(formatColumn(column)).param(value).build());
            return this;
        }

        public CorySqlInfo build() {
            //update xxx set CA = ?, CB = ? where ID = ?
            List<String> columnList = new ArrayList<>();
            List<Object> paramList = new ArrayList<>();

            columnValueList.forEach(cv -> {
                //skip id column
                if (cv.getColumn().equalsIgnoreCase(ID)) {
                    return;
                }
                columnList.add(cv.getColumn() + EQUAL + QUESTION_MARK);
                paramList.add(cv.getParam());
            });
            //add id as last parameter
            Object id = columnValueList.stream().filter(cv -> cv.getColumn().equalsIgnoreCase(ID)).findFirst().get().getParam();
            paramList.add(id);

            String sql = String.format("UPDATE %s SET %s where ID = ?", table, StringUtils.join(columnList, COMMA), id);
            return CorySqlInfo.builder().sql(formatSql(sql)).params(paramList).build();
        }
    }

    public static class CoryDeleteSqlBuilder extends CoryUpdateSqlBuilder {

        CoryDeleteSqlBuilder(String table, String whereSql, Map<String, Object> ognlParamMap) {
            super(table, null, whereSql, ognlParamMap);
        }

        @Override
        public CorySqlInfo build() {
            //update xxx set is_deleted = 1, MODIFY_TIME = now() where IS_DELETED = 0 and col_a = #{colA} and col_b in #{colB}
            CorySqlInfo wherePart = parseWherePart(whereSql, ognlParamMap, true);

            String sql = String.format("UPDATE %s SET IS_DELETED = 1, MODIFY_TIME = now() WHERE IS_DELETED = 0 %s", table, wherePart.getSql());
            return CorySqlInfo.builder().sql(formatSql(sql)).params(wherePart.getParams()).build();
        }
    }

    public static class CoryUpdateSqlBuilder {

        /** 表名 */
        protected String table;

        /** 参见@Update里的注释说明 */
        protected String columnSql;

        /** 参见@Update里的注释说明 */
        protected String whereSql;

        protected Map<String, Object> ognlParamMap = new HashMap<>();

        CoryUpdateSqlBuilder(String table, String columnSql, String whereSql, Map<String, Object> ognlParamMap) {
            this.table = table;
            this.columnSql = columnSql;
            this.whereSql = whereSql;
            this.ognlParamMap = ognlParamMap;
        }

        public CorySqlInfo build() {
            //update xxx set is_deleted = 1, MODIFY_TIME = now() where IS_DELETED = 0 and col_a = #{colA} and col_b in #{colB}

            CorySqlInfo columnPart = parseColumnPart(columnSql, ognlParamMap);
            CorySqlInfo wherePart = parseWherePart(whereSql, ognlParamMap, true);

            List<Object> params = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(columnPart.getParams())) {
                params.addAll(columnPart.getParams());
            }
            if (CollectionUtils.isNotEmpty(wherePart.getParams())) {
                params.addAll(wherePart.getParams());
            }

            String sql = String.format("UPDATE %s SET %s WHERE IS_DELETED = 0 %s", table, columnPart.getSql(), wherePart.getSql());
            return CorySqlInfo.builder().sql(formatSql(sql)).params(params).build();
        }
    }

    public static class CorySelectSqlBuilder {

        /** 表名 */
        protected String table;

        /** 参见@Select里的注释说明 */
        protected String whereSql;

        /**
         * 参见@Select里的注释说明
         */
        protected boolean whereModel;

        /** 参见@Select里的注释说明 */
        protected boolean orderBy;

        /** 参见@Select里的注释说明 */
        protected boolean limit;

        /** 参见@Select里的注释说明 */
        protected String customSql;

        protected Map<String, Object> ognlParamMap = new HashMap<>();

        /** whereModel = true时有用，其他时间没用 */
        protected List<CorySqlColumnValue> columnValueList = new ArrayList<>();

        CorySelectSqlBuilder(String table, String whereSql, boolean whereModel, boolean orderBy, boolean limit, String customSql, Map<String, Object> ognlParamMap) {
            this.table = table;
            this.whereSql = whereSql;
            this.whereModel = whereModel;
            this.orderBy = orderBy;
            this.limit = limit;
            this.customSql = customSql;
            this.ognlParamMap = ognlParamMap;
        }

        public CorySelectSqlBuilder column(String column, Object value) {
            AssertUtils.hasText(column, "字段名不能为空", ErrorCode.DB_ERROR);
            columnValueList.add(CorySqlColumnValue.builder().column(formatColumn(column)).param(value).build());
            return this;
        }

        private CorySqlInfo buildSelectWherePart() {
            CorySqlInfo wherePart = parseWherePart(whereSql, ognlParamMap, true);
            List<Object> params = wherePart.getParams();
            StringBuilder whereSql = new StringBuilder(wherePart.getSql());

            if (this.whereModel) {
                columnValueList.forEach(cv -> {
                    //skip base columns
                    if (cv.getColumn().equalsIgnoreCase(ID) ||
                            cv.getColumn().equalsIgnoreCase(CREATOR) ||
                            cv.getColumn().equalsIgnoreCase(MODIFIER) ||
                            cv.getColumn().equalsIgnoreCase(CREATE_TIME) ||
                            cv.getColumn().equalsIgnoreCase(MODIFY_TIME) ||
                            cv.getColumn().equalsIgnoreCase(IS_DELETED)) {
                        return;
                    }
                    whereSql.append(" AND " + cv.getColumn() + EQUAL + QUESTION_MARK);
                    params.add(cv.getParam());
                });
            }
            return CorySqlInfo.builder().sql(whereSql.toString()).params(params).build();
        }

        public CorySqlInfo buildDataSql() {
            //select * from xxx where is_deleted = 0 and col_a = #{colA} order by id desc limit 30, 10

            if (StringUtils.isNotBlank(customSql)) {
                CorySqlInfo wherePart = parseWherePart(customSql, ognlParamMap, false);
                return CorySqlInfo.builder().sql(formatSql(wherePart.getSql())).params(wherePart.getParams()).build();
            }

            CorySqlInfo wherePart = buildSelectWherePart();

            String sql = String.format("SELECT * FROM %s WHERE IS_DELETED = 0 %s", table, wherePart.getSql());
            if (orderBy) {
                String sort = (String) ognlParamMap.get(PARAM_SORT);
                if (StringUtils.isBlank(sort)) {
                    sort = "MODIFY_TIME DESC";
                }

                sql += " ORDER BY " + sort;
            }
            if (limit) {
                Integer pageStart = (Integer) ognlParamMap.get(PARAM_PAGE_START);
                Integer pageSize = (Integer) ognlParamMap.get(PARAM_PAGE_SIZE);
                AssertUtils.notNull(pageStart, "limit需要pageStart参数，请添加", ErrorCode.DB_ERROR);
                AssertUtils.notNull(pageStart, "limit需要pageSize参数，请添加", ErrorCode.DB_ERROR);

                sql += String.format(" LIMIT %s, %s", pageStart, pageSize);
            }
            return CorySqlInfo.builder().sql(formatSql(sql)).params(wherePart.getParams()).build();
        }

        public CorySqlInfo buildCountSql() {
            //select count(*) from xxx where is_deleted = 0 and col_a = #{colA}

            CorySqlInfo wherePart = buildSelectWherePart();

            String sql = String.format("SELECT COUNT(*) FROM %s WHERE IS_DELETED = 0 %s", table, wherePart.getSql());
            return CorySqlInfo.builder().sql(formatSql(sql)).params(wherePart.getParams()).build();
        }
    }

    @Data
    @Builder
    private static class VariableInfo {
        /** 是否时变量，用#{}或#!{}包含的参数 */
        private boolean isVariable;

        /** 是否可空：用#!{}包含的参数 */
        private boolean isNullable;

        /** 是否有括号结尾 */
        private boolean haveEndBracket;

        /** 如果是变量，解析出的变量名 */
        private String variableName;
    }

    public static void main(String[] args) {
        CorySqlInfo sqlInfo = CorySqlBuilder.createInsertBuilder("lm_device")
                .column("id", 1)
                .column("code", "TOOTH")
                .column("name", "牙套")
                .column("typeCode", "Test")
                .column("typeCodeName", "测试")
                .column("spec_code", "tooth_spec")
                .column("spec_code_name", "测试规格")
                .column("ext_1", true)
                .column("ext2", new Object())
                .build();
        System.out.println("sql: " + sqlInfo.getSql());
        System.out.println("params: " + JSON.toJSONString(sqlInfo.getParams()));

        sqlInfo = CorySqlBuilder.createUpdateModelBuilder("lm_device")
                .column("id", 1)
                .column("code", "TOOTH")
                .column("name", "牙套")
                .column("typeCode", "Test")
                .column("typeCodeName", "测试")
                .column("spec_code", "tooth_spec")
                .column("spec_code_name", "测试规格")
                .column("ext_1", true)
                .column("ext2", new Object())
                .build();
        System.out.println("sql: " + sqlInfo.getSql());
        System.out.println("params: " + JSON.toJSONString(sqlInfo.getParams()));

        List<String> codeList = Arrays.asList("123", "456");
        Map<String, Object> params = MapBuilder.create(String.class, Object.class)
                .put("codeList", codeList)
                .put("name", "张三")
                .put("typeCode", "789")
                .put("foreignCode", "fff")
                .put("pageStart", 20)
                .put("pageSize", 10)
                .put("sort", "code desc")
                .build();

        sqlInfo = CorySqlBuilder.createDeleteBuilder("lm_device", "code in #{codeList} and name like #!{name} and name not like #{name} and not exists (select 1 from xx where code = #{foreignCode}) and type_code = #{typeCode}", params).build();

        System.out.println("sql: " + sqlInfo.getSql());
        System.out.println("params: " + JSON.toJSONString(sqlInfo.getParams()));

        sqlInfo = CorySqlBuilder.createUpdateBuilder("lm_device", "name = #{name}, modifier = 0, modify_time = now(), type_code = #{typeCode}", "code in #{codeList}", params).build();
        System.out.println("sql: " + sqlInfo.getSql());
        System.out.println("params: " + JSON.toJSONString(sqlInfo.getParams()));

        sqlInfo = CorySqlBuilder.createSelectBuilder("lm_device", "code in #{codeList} and name like #!{name} and type_code = #{typeCode}", true, true, true, null, params).column("code", "123").column("type", "DEVICE").column("age", 19).buildDataSql();
        System.out.println("sql: " + sqlInfo.getSql());
        System.out.println("params: " + JSON.toJSONString(sqlInfo.getParams()));

        sqlInfo = CorySqlBuilder.createSelectBuilder("lm_device", "code in #{codeList} and name like #!{name} and type_code = #{typeCode}", true, true, true, null, params).column("code", "123").column("type", "DEVICE").column("age", 19).buildCountSql();
        System.out.println("sql: " + sqlInfo.getSql());
        System.out.println("params: " + JSON.toJSONString(sqlInfo.getParams()));
    }
}
