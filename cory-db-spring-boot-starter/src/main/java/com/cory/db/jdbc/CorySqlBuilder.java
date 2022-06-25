package com.cory.db.jdbc;

import com.alibaba.fastjson.JSON;
import com.cory.constant.ErrorCode;
import com.cory.db.datapermission.DataPermissionResult;
import com.cory.db.datapermission.DataPermissionStrategy;
import com.cory.exception.CoryException;
import com.cory.model.BaseModel;
import com.cory.util.AssertUtils;
import com.cory.util.MapBuilder;
import com.google.common.base.CaseFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cory.constant.Constants.*;

/**
 * where 条件，直接写条件，不用加where。
 * 例子：col_a = #{colA} and col_b < #{colB} and col_c > 5 and col_d in #{colDList}
 * <br />
 * 会自动加：is_deleted = 0的条件。
 * <br />
 * 里面可以加参数，写法和mybatis一样，参数从@Param注解里获取。类型为list或set或数组的会自动解析为IN。参数必须写Param注解
 * <br />
 * 如果需要做判空处理，那么在写参数时用：#![ xxx ]包裹，则会判断里面的参数，如果参数没有值，则不输出。注意：1、AND等连接符也需要加在里面；2、不能嵌套，一个#![]里只能包含一个参数。如果参数是数组，会展开成(x, y, z)的形式（IN的语法）
 * #![AND col_a = #{colA}] #![ or col_b in #{colB}]
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
    private static final String EQUAL = "=";
    private static final String QUESTION_MARK = "?";

    private static final Pattern PARAM_PATTERN_REG = Pattern.compile("#\\{.*?}");
    private static final Pattern NULLABLE_PARAM_PATTERN_REG = Pattern.compile("#!\\[.*?]");

    private static final String PARAM_PATTERN_FULL = ".*?#\\{.*?}.*";
    private static final String NULLABLE_PARAM_PATTERN_FULL = ".*?#!\\[.*?].*";

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
     * @param logicDelete 逻辑删除
     * @return
     */
    public static CoryDeleteSqlBuilder createDeleteBuilder(String table, String whereSql, boolean logicDelete, Map<String, Object> ognlParamMap) {
        return new CoryDeleteSqlBuilder(table, whereSql, logicDelete, ognlParamMap);
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

    public static CorySelectSqlBuilder createSelectBuilder(String table, String whereSql, boolean whereModel, boolean orderBy, boolean limit, String customSql, Map<String, Object> ognlParamMap, DataPermissionResult dataPermissionResult) {
        return new CorySelectSqlBuilder(table, whereSql, whereModel, orderBy, limit, customSql, ognlParamMap, dataPermissionResult);
    }

    private static String formatColumn(String column) {
        //驼峰转下划线分割，然后全部大写
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, column).toUpperCase();
    }

    private static CorySqlInfo parseColumnPart(String columnSql, Map<String, Object> ognlParamMap) {
        return parseParamSql(columnSql, ognlParamMap);
    }

    /**
     * @param whereSql
     * @param ognlParamMap
     * @param wrapAnd 自定义sql的地方，不要包裹AND ()
     * @return
     */
    private static CorySqlInfo parseWherePart(String whereSql, Map<String, Object> ognlParamMap, boolean wrapAnd) {
        CorySqlInfo sqlInfo = parseParamSql(whereSql, ognlParamMap);

        //处理完后没有条件（全部都是可空参数），则返回空即可
        if (StringUtils.isBlank(sqlInfo.getSql())) {
            return CorySqlInfo.builder().sql("").build();
        }
        if (wrapAnd) {
            String sql = " AND (" + sqlInfo.getSql() + ")";
            sqlInfo.setSql(sql);
        }
        return sqlInfo;
    }

    private static CorySqlInfo parseParamSql(String sql, Map<String, Object> ognlParamMap) {
        if (StringUtils.isBlank(sql)) {
            return CorySqlInfo.builder().sql("").build();
        }

        boolean hasParam = sql.matches(PARAM_PATTERN_FULL);
        boolean hasNullableParam = sql.matches(NULLABLE_PARAM_PATTERN_FULL);

        //如果不包含变量，直接返回即可
        if (!hasParam && !hasNullableParam) {
            return CorySqlInfo.builder().sql(sql).build();
        }

        //包含可空变量，先处理可空变量：把空的删除，不空的留下
        if (hasNullableParam) {
            Matcher nullableMatcher = NULLABLE_PARAM_PATTERN_REG.matcher(sql);

            while (nullableMatcher.find()) {
                String group = nullableMatcher.group(0);
                //remove #![ and ]
                String subSql = group.trim().substring(3);
                subSql = subSql.substring(0, subSql.length() - 1);
                subSql = subSql.trim();

                Matcher paramMatcher = PARAM_PATTERN_REG.matcher(subSql);

                //只找一个，不找多个
                if (paramMatcher.find()) {
                    String paramGroup = paramMatcher.group(0);
                    //remove #{ and }
                    String paramName = paramGroup.trim().substring(2);
                    paramName = paramName.substring(0, paramName.length() - 1);
                    paramName = paramName.trim();
                    Object value = ognlParamMap.get(paramName);
                    //可变参数有值，输出，否则输出成空字符串
                    if (null != value) {
                        sql = sql.replace(group, subSql);
                    } else {
                        sql = sql.replace(group, "");
                    }
                } else {
                    //没有参数，则全部输出
                    sql = sql.replace(group, subSql);
                }

                nullableMatcher = NULLABLE_PARAM_PATTERN_REG.matcher(sql);
            }
        }

        //最后处理变量
        Matcher matcher = PARAM_PATTERN_REG.matcher(sql);

        List<Object> paramList = new ArrayList<>();

        while (matcher.find()) {
            String group = matcher.group(0);
            //remove #{ and }
            String paramName = group.trim().substring(2);
            paramName = paramName.substring(0, paramName.length() - 1);
            paramName = paramName.trim();
            Object value = ognlParamMap.get(paramName);
            //20211017：变量可能重复，比如：name = #{userName} or email = #{userName}，所以要一个一个处理，不能直接全部替换
            sql = sql.replaceFirst(group.replace("{", "\\{").replace("}", "\\}"), parseQuestionAndAddParam(paramName, value, paramList));

            matcher = PARAM_PATTERN_REG.matcher(sql);
        }

        return CorySqlInfo.builder().sql(sql).params(paramList).build();
    }

    /**
     * 解析出问号sql，并且把参数值添加到参数列表里
     * @param paramName 参数名
     * @param paramValue 参数值
     * @param paramList 解析后的参数列表
     * @return 返回问号sql。如果是单个返回 ?，如果是列表返回：(?, ?, ?)
     */
    private static String parseQuestionAndAddParam(String paramName, Object paramValue, List<Object> paramList) {
        //(?, ?, ?)
        if (null == paramValue) {
            throw new CoryException(ErrorCode.DB_ERROR, paramName + "的值不能为空");
        } else if (paramValue.getClass().isArray()) {
            int len = Array.getLength(paramValue);
            List<String> list = new ArrayList<>();
            for (int i = 0; i < len; i++) {
                Object item = Array.get(paramValue, i);
                paramList.add(item);
                list.add(QUESTION_MARK);
            }
            return "(" + StringUtils.join(list, COMMA) + ")";
        } else if (paramValue instanceof Collection<?>) {
            Collection collection = (Collection) paramValue;
            List<String> list = new ArrayList<>();
            for (Object item : collection) {
                paramList.add(item);
                list.add(QUESTION_MARK);
            }
            return "(" + StringUtils.join(list, COMMA) + ")";
        } else {
            paramList.add(paramValue);
            return "?";
        }
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
        @Builder.Default
        private List<Object> params = new ArrayList<>();

        /** 数据权限使用，如果数据权限判断出来不让查询，直接返回空 */
        @Builder.Default
        private Boolean selectDenied = false;

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
                columnList.add(cv.getColumn() + SPACE + EQUAL + SPACE + QUESTION_MARK);
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

        private boolean logicDelete;

        CoryDeleteSqlBuilder(String table, String whereSql, boolean logicDelete, Map<String, Object> ognlParamMap) {
            super(table, null, whereSql, ognlParamMap);
            this.logicDelete = logicDelete;
        }

        @Override
        public CorySqlInfo build() {
            //update xxx set is_deleted = 1, MODIFY_TIME = now() where IS_DELETED = 0 and col_a = #{colA} and col_b in #{colB}
            //delete from xxx where xxx
            CorySqlInfo wherePart;
            String sql;
            if (logicDelete) {
                wherePart = parseWherePart(whereSql, ognlParamMap, true);
                sql = String.format("UPDATE %s SET IS_DELETED = 1, MODIFY_TIME = now() WHERE IS_DELETED = 0 %s", table, wherePart.getSql());
            } else {
                wherePart = parseWherePart(whereSql, ognlParamMap, false);
                sql = String.format("DELETE FROM %s WHERE %s", table, wherePart.getSql());
            }
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

            String sql = String.format("UPDATE %s SET %s WHERE IS_DELETED = 0 %s", table, "MODIFY_TIME = now(), " + columnPart.getSql(), wherePart.getSql());
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

        protected DataPermissionResult dataPermissionResult;

        /** whereModel = true时有用，其他时间没用 */
        protected List<CorySqlColumnValue> columnValueList = new ArrayList<>();

        CorySelectSqlBuilder(String table, String whereSql, boolean whereModel, boolean orderBy, boolean limit, String customSql, Map<String, Object> ognlParamMap, DataPermissionResult dataPermissionResult) {
            this.table = table;
            this.whereSql = whereSql;
            this.whereModel = whereModel;
            this.orderBy = orderBy;
            this.limit = limit;
            this.customSql = customSql;
            this.ognlParamMap = ognlParamMap;
            this.dataPermissionResult = dataPermissionResult;
        }

        public CorySelectSqlBuilder column(String column, Object value) {
            AssertUtils.hasText(column, "字段名不能为空", ErrorCode.DB_ERROR);
            columnValueList.add(CorySqlColumnValue.builder().column(formatColumn(column)).param(value).build());
            return this;
        }

        private CorySqlInfo buildSelectWherePart() {
            CorySqlInfo wherePart = parseWherePart(whereSql, ognlParamMap, true);

            if (null != dataPermissionResult && DataPermissionStrategy.DENY.equals(dataPermissionResult.getStrategy())) {
                return CorySqlInfo.builder().selectDenied(true).build();
            }

            List<Object> params = wherePart.getParams();
            StringBuilder whereSql = new StringBuilder(wherePart.getSql());

            if (null != dataPermissionResult && DataPermissionStrategy.FILTER.equals(dataPermissionResult.getStrategy())) {
                whereSql.append("AND (" + dataPermissionResult.getFilterSql() + ")");
            }

            if (this.whereModel) {
                columnValueList.forEach(cv -> {
                    //skip base columns
                    //20220610：可以按ID过滤的
                    //if (cv.getColumn().equalsIgnoreCase(ID) ||
                    if (cv.getColumn().equalsIgnoreCase(CREATOR) ||
                            cv.getColumn().equalsIgnoreCase(MODIFIER) ||
                            cv.getColumn().equalsIgnoreCase(CREATE_TIME) ||
                            cv.getColumn().equalsIgnoreCase(MODIFY_TIME) ||
                            cv.getColumn().equalsIgnoreCase(IS_DELETED)) {
                        return;
                    }
                    whereSql.append(" AND " + cv.getColumn() + SPACE + EQUAL + SPACE + QUESTION_MARK);
                    params.add(cv.getParam());
                });
            }

            //处理特殊过滤字段：model.getFilterFieldMap()
            if (MapUtils.isNotEmpty(ognlParamMap)) {
                ognlParamMap.entrySet().forEach(entry -> {
                    Object value = entry.getValue();
                    if (!(value instanceof BaseModel)) {
                        return;
                    }
                    BaseModel model = (BaseModel) value;
                    if (MapUtils.isEmpty(model.getFilterFieldMap())) {
                        return;
                    }
                    model.getFilterFieldMap().entrySet().forEach(filter -> {
                        if (null == filter.getValue()) {
                            return;
                        }
                        //Start(包含)、End(不包含)、In、Like、LikeLeft、LikeRight、NotIn，NotLike、NotLikeLeft、NotLikeRight、NotEq(不等)、IsNull、NotNull
                        //createTimeStart >= ? AND createTimeEnd < ?
                        String key = filter.getKey();
                        if (key.endsWith(FILTER_FIELD_POSTFIX_START_INCLUSIVE)) {
                            String columnField = key.substring(0, key.length() - FILTER_FIELD_POSTFIX_START_INCLUSIVE.length());
                            String columnName = CoryModelUtil.buildColumnName(columnField);
                            whereSql.append(" AND " + columnName + " >= " + QUESTION_MARK);
                            params.add(filter.getValue());
                        } else if (key.endsWith(FILTER_FIELD_POSTFIX_START_EXCLUSIVE)) {
                            String columnField = key.substring(0, key.length() - FILTER_FIELD_POSTFIX_START_EXCLUSIVE.length());
                            String columnName = CoryModelUtil.buildColumnName(columnField);
                            whereSql.append(" AND " + columnName + " > " + QUESTION_MARK);
                            params.add(filter.getValue());
                        } else if (key.endsWith(FILTER_FIELD_POSTFIX_END_INCLUSIVE)) {
                            String columnField = key.substring(0, key.length() - FILTER_FIELD_POSTFIX_END_INCLUSIVE.length());
                            String columnName = CoryModelUtil.buildColumnName(columnField);
                            whereSql.append(" AND " + columnName + " <= " + QUESTION_MARK);
                            params.add(filter.getValue());
                        } else if (key.endsWith(FILTER_FIELD_POSTFIX_END_EXCLUSIVE)) {
                            String columnField = key.substring(0, key.length() - FILTER_FIELD_POSTFIX_END_EXCLUSIVE.length());
                            String columnName = CoryModelUtil.buildColumnName(columnField);
                            whereSql.append(" AND " + columnName + " < " + QUESTION_MARK);
                            params.add(filter.getValue());
                        } else if (key.endsWith(FILTER_FIELD_POSTFIX_IN) || key.endsWith(FILTER_FIELD_POSTFIX_NOT_IN)) {
                            boolean isNotIn = key.endsWith(FILTER_FIELD_POSTFIX_NOT_IN);
                            String columnField = key.substring(0, key.length() - (isNotIn ? FILTER_FIELD_POSTFIX_NOT_IN.length() : FILTER_FIELD_POSTFIX_IN.length()));
                            String columnName = CoryModelUtil.buildColumnName(columnField);

                            List list = (List) filter.getValue();
                            StringBuilder inSql = new StringBuilder("(");
                            for (int i = 0; i < list.size(); i++) {
                                inSql.append(QUESTION_MARK);
                                inSql.append(COMMA);
                                params.add(list.get(i));
                            }
                            inSql.deleteCharAt(inSql.length() - 1);
                            inSql.append(")");
                            whereSql.append(" AND " + columnName + (isNotIn ? " not in " : " in ") + inSql);
                        } else if (key.endsWith(FILTER_FIELD_POSTFIX_LIKE_LEFT) ||
                                key.endsWith(FILTER_FIELD_POSTFIX_LIKE_RIGHT) ||
                                key.endsWith(FILTER_FIELD_POSTFIX_LIKE_BOTH) ||
                                key.endsWith(FILTER_FIELD_POSTFIX_NOT_LIKE_LEFT) ||
                                key.endsWith(FILTER_FIELD_POSTFIX_NOT_LIKE_RIGHT) ||
                                key.endsWith(FILTER_FIELD_POSTFIX_NOT_LIKE_BOTH)) {
                            boolean onlyLikeLeft = key.endsWith(FILTER_FIELD_POSTFIX_LIKE_LEFT);
                            boolean onlyLikeRight = key.endsWith(FILTER_FIELD_POSTFIX_LIKE_RIGHT);
                            boolean bothLike = key.endsWith(FILTER_FIELD_POSTFIX_LIKE_BOTH);
                            boolean onlyNotLikeLeft = key.endsWith(FILTER_FIELD_POSTFIX_NOT_LIKE_LEFT);
                            boolean onlyNotLikeRight = key.endsWith(FILTER_FIELD_POSTFIX_NOT_LIKE_RIGHT);
                            boolean bothNotLike = key.endsWith(FILTER_FIELD_POSTFIX_NOT_LIKE_BOTH);

                            String columnField;
                            if (onlyNotLikeLeft) {
                                columnField = key.substring(0, key.length() - FILTER_FIELD_POSTFIX_NOT_LIKE_LEFT.length());
                            } else if (onlyNotLikeRight) {
                                columnField = key.substring(0, key.length() - FILTER_FIELD_POSTFIX_NOT_LIKE_RIGHT.length());
                            } else if (bothNotLike) {
                                columnField = key.substring(0, key.length() - FILTER_FIELD_POSTFIX_NOT_LIKE_BOTH.length());
                            } else if (onlyLikeLeft) {
                                columnField = key.substring(0, key.length() - FILTER_FIELD_POSTFIX_LIKE_LEFT.length());
                            } else if (onlyLikeRight) {
                                columnField = key.substring(0, key.length() - FILTER_FIELD_POSTFIX_LIKE_RIGHT.length());
                            } else {
                                columnField = key.substring(0, key.length() - FILTER_FIELD_POSTFIX_LIKE_BOTH.length());
                            }
                            String columnName = CoryModelUtil.buildColumnName(columnField);

                            //whereSql.append(" AND name not like concat('%', ?, '%')");
                            whereSql.append(" AND " + columnName);
                            if (onlyNotLikeLeft || onlyNotLikeRight || bothNotLike) {
                                whereSql.append(" not");
                            }
                            whereSql.append(" like concat(");
                            if (onlyLikeLeft || onlyNotLikeLeft || bothLike || bothNotLike) {
                                whereSql.append("'%', ");
                            }
                            whereSql.append(QUESTION_MARK);
                            if (onlyLikeRight || onlyNotLikeRight || bothLike || bothNotLike) {
                                whereSql.append(", '%'");
                            }
                            whereSql.append(" ) ");
                            params.add(filter.getValue());
                        } else if (key.endsWith(FILTER_FIELD_POSTFIX_NOT_EQ)) {
                            String columnField = key.substring(0, key.length() - FILTER_FIELD_POSTFIX_NOT_EQ.length());
                            String columnName = CoryModelUtil.buildColumnName(columnField);
                            whereSql.append(" AND " + columnName + " <> " + QUESTION_MARK);
                            params.add(filter.getValue());
                        } else if (key.endsWith(FILTER_FIELD_POSTFIX_IS_NULL)) {
                            String columnField = key.substring(0, key.length() - FILTER_FIELD_POSTFIX_IS_NULL.length());
                            String columnName = CoryModelUtil.buildColumnName(columnField);
                            whereSql.append(" AND " + columnName + " is null");
                        } else if (key.endsWith(FILTER_FIELD_POSTFIX_NOT_NULL)) {
                            String columnField = key.substring(0, key.length() - FILTER_FIELD_POSTFIX_NOT_NULL.length());
                            String columnName = CoryModelUtil.buildColumnName(columnField);
                            whereSql.append(" AND " + columnName + " is not null");
                        } else if (key.equals(FILTER_FIELD_CUSTOM)) {
                            whereSql.append(" AND (" + filter.getValue() + ")");
                        }
                    });
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
            if (wherePart.selectDenied) {
                return wherePart;
            }

            checkForLimit(limit, ognlParamMap);

            String sql;
            if (needOptForLimit(limit, ognlParamMap)) {
                //分页查询时优化：先查id再联合查询。在pagestart大于阈值才优化。经实验，超过10000行即可进行优化。
                //select id from base_action_log where object_id > '10006' and object_id < '10028' order by object_id limit 3, 5
                //select a.* from base_action_log a right join (select id from base_action_log where object_id > '10006' and object_id < '10028' order by object_id limit 3, 5) b on a.id = b.id order by object_id;

                String orderByPart = "";
                if (orderBy) {
                    String sort = (String) ognlParamMap.get(PARAM_SORT);
                    if (StringUtils.isBlank(sort)) {
                        sort = "MODIFY_TIME DESC";
                    }
                    orderByPart = " ORDER BY " + sort;
                }

                Integer pageStart = (Integer) ognlParamMap.get(PARAM_PAGE_START);
                Integer pageSize = (Integer) ognlParamMap.get(PARAM_PAGE_SIZE);

                sql = String.format("SELECT a.* from %s a right join (select id from %s where is_deleted = 0 %s %s limit %s, %s) b on a.id = b.id %s",
                        table, table, wherePart.getSql(), orderByPart, pageStart, pageSize, orderByPart);
            } else {
                sql = String.format("SELECT * FROM %s WHERE IS_DELETED = 0 %s", table, wherePart.getSql());
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
                    sql += String.format(" LIMIT %s, %s", pageStart, pageSize);
                }
            }

            return CorySqlInfo.builder().sql(formatSql(sql)).params(wherePart.getParams()).build();
        }

        private boolean needOptForLimit(boolean limit, Map<String, Object> ognlParamMap) {
            //分页查询时优化：先查id再联合查询。在pagestart大于阈值才优化。经实验，超过10000行即可进行优化。
            if (!limit) {
                return false;
            }
            Integer pageStart = (Integer) ognlParamMap.get(PARAM_PAGE_START);
            return pageStart > 10000;
        }

        private void checkForLimit(boolean limit, Map<String, Object> ognlParamMap) {
            if (!limit) {
                return;
            }
            Integer pageStart = (Integer) ognlParamMap.get(PARAM_PAGE_START);
            Integer pageSize = (Integer) ognlParamMap.get(PARAM_PAGE_SIZE);
            AssertUtils.notNull(pageStart, "limit需要pageStart参数，请添加", ErrorCode.DB_ERROR);
            AssertUtils.notNull(pageSize, "limit需要pageSize参数，请添加", ErrorCode.DB_ERROR);
        }

        public CorySqlInfo buildCountSql() {
            //select count(*) from xxx where is_deleted = 0 and col_a = #{colA}

            CorySqlInfo wherePart = buildSelectWherePart();
            if (wherePart.selectDenied) {
                return wherePart;
            }

            String sql = String.format("SELECT COUNT(*) FROM %s WHERE IS_DELETED = 0 %s", table, wherePart.getSql());
            return CorySqlInfo.builder().sql(formatSql(sql)).params(wherePart.getParams()).build();
        }
    }

    public static void main(String[] args) throws ParseException {
        String sql = "email = #{userName} or phone = #{userName}";
        Matcher matcher = PARAM_PATTERN_REG.matcher(sql);
        while (matcher.find()) {
            String group = matcher.group(0);
            //remove #{ and }
            sql = sql.replaceFirst(group.replace("{", "\\{").replace("}", "\\}"), "?");
            System.out.println(sql);

            matcher = PARAM_PATTERN_REG.matcher(sql);
        }
        System.out.println("done 1");

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

        sqlInfo = CorySqlBuilder.createDeleteBuilder("lm_device", "code in #{codeList} #![and name like #{name}] and name not like #{name} and not exists (select 1 from xx where code = #{foreignCode}) and type_code = #{typeCode}", true, params).build();

        System.out.println("sql: " + sqlInfo.getSql());
        System.out.println("params: " + JSON.toJSONString(sqlInfo.getParams()));

        sqlInfo = CorySqlBuilder.createUpdateBuilder("lm_device", "name = #{name}, modifier = 0, modify_time = now(), type_code = #{typeCode}", "code in #{codeList}", params).build();
        System.out.println("sql: " + sqlInfo.getSql());
        System.out.println("params: " + JSON.toJSONString(sqlInfo.getParams()));

        params.remove("name");
        sqlInfo = CorySqlBuilder.createSelectBuilder("lm_device", "code in #{codeList} #![and name like #{name}] and type_code = #{typeCode}", true, true, true, null, params, null).column("code", "123").column("type", "DEVICE").column("age", 19).buildDataSql();
        System.out.println("sql: " + sqlInfo.getSql());
        System.out.println("params: " + JSON.toJSONString(sqlInfo.getParams()));

        params.remove("name");
        sqlInfo = CorySqlBuilder.createSelectBuilder("lm_device", "code in #{codeList} #![and name like #{name}] and type_code = #{typeCode}", true, true, true, null, params, null).column("code", "123").column("type", "DEVICE").column("age", 19).buildCountSql();
        System.out.println("sql: " + sqlInfo.getSql());
        System.out.println("params: " + JSON.toJSONString(sqlInfo.getParams()));
    }
}
