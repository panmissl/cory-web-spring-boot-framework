package com.cory.db.processor;

import com.cory.constant.ErrorCode;
import com.cory.context.CoryEnv;
import com.cory.db.annotations.Field;
import com.cory.db.annotations.Model;
import com.cory.db.config.CoryDbProperties;
import com.cory.db.jdbc.Column;
import com.cory.db.jdbc.CoryDb;
import com.cory.db.jdbc.CoryModelUtil;
import com.cory.db.jdbc.CorySqlBuilder.CorySqlInfo;
import com.cory.db.jdbc.Table;
import com.cory.exception.CoryException;
import com.cory.model.BaseModel;
import com.google.common.base.CaseFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.util.AnnotatedTypeScanner;

import java.util.*;

import static com.cory.constant.Constants.BASE_MODEL_COLUMNS;

/**
 * Created by Cory on 2021/2/9.
 */
@Slf4j
public class CoryDbChecker implements InitializingBean {

    private static final String COLUMN_SQL = "select * from information_schema.columns where table_schema = ?";

    private static final String BASE_COLUMNS_DDL =
            "`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID'," +
            "`creator` bigint(20) NOT NULL COMMENT '创建人'," +
            "`modifier` bigint(20) NOT NULL COMMENT '修改人'," +
            "`create_time` datetime NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '创建时间'," +
            "`modify_time` datetime NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '修改时间'," +
            "`is_deleted` SMALLINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',";

    private static final String CREATE_TABLE_HEADER = "CREATE TABLE `%s` (";

    private static final String CREATE_TABLE_FOOTER =
            "PRIMARY KEY (`id`)" +
            ") ENGINE=InnoDB AUTO_INCREMENT=10001 DEFAULT CHARSET=utf8mb4 COMMENT='%s';";

    private static final String DROP_COLUMN_SQL = "alter table %s drop column %s";
    private static final String ADD_COLUMN_SQL = "alter table %s add column %s";
    private static final String MODIFY_COLUMN_SQL = "alter table %s modify column %s";

    private CoryDbProperties coryDbProperties;
    private CoryDb coryDb;
    private String database;

    public CoryDbChecker(CoryDb coryDb, CoryDbProperties coryDbProperties, String database) {
        this.coryDb = coryDb;
        this.coryDbProperties = coryDbProperties;
        this.database = database;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //开发环境直接同步，线上环境报错提醒

        Map<String, Table> newTableMap = queryNowTables();
        if (MapUtils.isEmpty(newTableMap)) {
            log.info("no Model found, skip db check.");
            return;
        }

        Map<String, Table> dbTableMap = queryNotNullDbTables();

        if (CoryEnv.IS_DEV) {
            log.info("check and sync db tables and columns from code to db.");
        }

        boolean same = true;
        StringBuilder msgBuilder = new StringBuilder();

        for (Map.Entry<String, Table> entry : newTableMap.entrySet()) {
            String tableName = entry.getKey();
            Table table = entry.getValue();

            Table dbTable = dbTableMap.get(tableName);
            if (null == dbTable) {
                same = false;
                createTableForDev(table, msgBuilder);
                continue;
            }

            if (!table.equals(dbTable)) {
                same = false;
                msgBuilder.append("table " + tableName + " does not match in db:\n");

                List<Column> addColumnList = table.differentColumns(dbTable, Table.COLUMN_TYPE.ADD);
                List<Column> deleteColumnList = table.differentColumns(dbTable, Table.COLUMN_TYPE.DELETE);
                List<Column> modifyColumnList = table.differentColumns(dbTable, Table.COLUMN_TYPE.MODIFY);

                syncColumn(addColumnList, deleteColumnList, modifyColumnList, msgBuilder);
            }
        }

        if (!same && CoryEnv.IS_PROD) {
            throw new CoryException(ErrorCode.DB_ERROR, msgBuilder.toString());
        }
    }

    private void createTableForDev(Table table, StringBuilder msgBuilder) {
        /*
        CREATE TABLE `base_resource` (
            `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
            `creator` bigint(20) NOT NULL COMMENT '创建人',
            `modifier` bigint(20) NOT NULL COMMENT '修改人',
            `create_time` datetime NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '创建时间',
            `modify_time` datetime NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '修改时间',
            `is_deleted` SMALLINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',

            `value` varchar(200) NOT NULL COMMENT '资源值',
            `type` varchar(50) NOT NULL COMMENT '资源类型',
            `description` varchar(200) NOT NULL COMMENT '描述',
            PRIMARY KEY (`id`)
        ) ENGINE=InnoDB AUTO_INCREMENT=10001 DEFAULT CHARSET=utf8mb4 COMMENT='资源表';
        */

        StringBuilder builder = new StringBuilder();
        builder.append(String.format(CREATE_TABLE_HEADER, table.getName()));
        builder.append(BASE_COLUMNS_DDL);

        for (Column column : table.getColumnList()) {
            builder.append(column.buildDDL());
            builder.append(",");
        }

        builder.append(String.format(CREATE_TABLE_FOOTER, table.getComment()));

        String sql = builder.toString();

        if (CoryEnv.IS_DEV) {
            coryDb.executeSql(sql);
            log.info("create table " + table.getName() + ": " + sql);
        }
        if (CoryEnv.IS_PROD) {
            msgBuilder.append("table " + table.getName() + " does not exist, please create using follow sql;\n");
            msgBuilder.append(sql);
            msgBuilder.append("\n");
        }
    }

    private void syncColumn(List<Column> addColumnList, List<Column> deleteColumnList, List<Column> modifyColumnList, StringBuilder msgBuilder) {
        if (CollectionUtils.isNotEmpty(addColumnList)) {
            for (Column column : addColumnList) {
                if (CoryEnv.IS_DEV) {
                    String sql = String.format(ADD_COLUMN_SQL, column.getTableName(), column.buildDDL());
                    coryDb.executeSql(sql);
                    log.info("add column " + column.getTableName() + "." + column.getName() + ": " + sql);
                }
                if (CoryEnv.IS_PROD) {
                    msgBuilder.append("\tplease add column: " + column.buildDDL() + ",\n");
                }
            }
        }
        if (CollectionUtils.isNotEmpty(deleteColumnList)) {
            for (Column column : deleteColumnList) {
                if (CoryEnv.IS_DEV) {
                    String sql = String.format(DROP_COLUMN_SQL, column.getTableName(), column.getName());
                    coryDb.executeSql(sql);
                    log.info("drop column " + column.getTableName() + "." + column.getName() + ": " + sql);
                }
                if (CoryEnv.IS_PROD) {
                    msgBuilder.append("\tplease drop column: " + column.buildDDL() + ",\n");
                }
            }
        }
        if (CollectionUtils.isNotEmpty(modifyColumnList)) {
            for (Column column : modifyColumnList) {
                if (CoryEnv.IS_DEV) {
                    String sql = String.format(MODIFY_COLUMN_SQL, column.getTableName(), column.buildDDL());
                    coryDb.executeSql(sql);
                    log.info("modify column " + column.getTableName() + "." + column.getName() + ": " + sql);
                }
                if (CoryEnv.IS_PROD) {
                    msgBuilder.append("\tplease modify column: " + column.buildDDL() + ",\n");
                }
            }
        }
    }

    private Map<String,Table> queryNowTables() {
        AnnotatedTypeScanner scanner = new AnnotatedTypeScanner(true, Model.class);
        Set<Class<?>> set = scanner.findTypes(coryDbProperties.getModelPackages());
        if (CollectionUtils.isEmpty(set)) {
            log.info("no model found in packages: {}", coryDbProperties.getModelPackages());
            return null;
        }

        Map<String, Table> tableMap = new HashMap<>();
        Map<String, Integer> tableDuplicateMap = new HashMap<>();

        for (Class<?> cls : set) {
            Model model = cls.getAnnotation(Model.class);
            if (null == model || model.noTable()) {
                continue;
            }

            String tableName = CoryModelUtil.buildTableName((Class<? extends BaseModel>) cls);

            //for duplicate check
            Integer ct = tableDuplicateMap.get(tableName);
            if (null == ct) {
                ct = 0;
            }
            tableDuplicateMap.put(tableName, ct + 1);

            java.lang.reflect.Field[] fields = cls.getDeclaredFields();
            if (null != fields || fields.length > 0) {
                for (java.lang.reflect.Field javaField : fields) {
                    Field field = javaField.getAnnotation(Field.class);
                    if (null == field) {
                        continue;
                    }

                    String columnName = CoryModelUtil.buildColumnName(javaField.getName());
                    String columnDefault = field.defaultValue();
                    boolean nullable = field.nullable();
                    String columnType = field.type().buildDbType(field.len());
                    String columnComment = field.comment();

                    if (StringUtils.isBlank(columnDefault)) {
                        columnDefault = null;
                    }

                    Table table = tableMap.get(tableName);
                    if (null == table) {
                        table = Table.builder().name(tableName).columnList(new ArrayList<>()).columnMap(new HashMap<>()).comment(model.name()).build();
                        tableMap.put(tableName, table);
                    }
                    Column column = Column.builder().name(columnName).tableName(tableName).defaultValue(columnDefault).nullable(nullable).columnType(columnType).columnComment(columnComment).build();
                    table.getColumnList().add(column);
                    table.getColumnMap().put(columnName, column);
                }
            }
        }

        checkDuplicated(tableDuplicateMap);

        return tableMap;
    }

    private void checkDuplicated(Map<String, Integer> tableDuplicateMap) {
        boolean duplicated = false;
        StringBuilder msgBuilder = new StringBuilder();
        for (Map.Entry<String, Integer> entry : tableDuplicateMap.entrySet()) {
            if (entry.getValue() > 1) {
                duplicated = true;
                msgBuilder.append("Dao table name: " + entry.getKey() + " is duplicated");
            }
        }

        if (duplicated) {
            throw new CoryException(ErrorCode.DB_ERROR, msgBuilder.toString());
        }
    }

    private Map<String, Table> queryNotNullDbTables() {
        CorySqlInfo sqlInfo = CorySqlInfo.builder().sql(COLUMN_SQL).params(Arrays.asList(database)).build();
        List<Map<String, Object>> list = coryDb.select(sqlInfo);
        if (CollectionUtils.isEmpty(list)) {
            return new HashMap<>();
        }
        Map<String, Table> tableMap = new HashMap<>();
        for (Map<String, Object> map : list) {
            String tableName = (String) map.get("TABLE_NAME");
            String columnName = (String) map.get("COLUMN_NAME");
            String columnDefault = (String) map.get("COLUMN_DEFAULT");
            boolean nullable = "YES".equals(map.get("IS_NULLABLE"));
            String columnType = (String) map.get("COLUMN_TYPE");
            String columnComment = (String) map.get("COLUMN_COMMENT");

            //跳过BaseModel的几个字段
            if (isBaseModelColumn(columnName)) {
                continue;
            }

            Table table = tableMap.get(tableName);
            if (null == table) {
                table = Table.builder().name(tableName).columnList(new ArrayList<>()).columnMap(new HashMap<>()).build();
                tableMap.put(tableName, table);
            }
            Column column = Column.builder().name(columnName).tableName(tableName).defaultValue(columnDefault).nullable(nullable).columnType(columnType).columnComment(columnComment).build();
            table.getColumnList().add(column);
            table.getColumnMap().put(columnName, column);
        }
        return tableMap;
    }

    private boolean isBaseModelColumn(String columnName) {
        for (String baseModelColumn : BASE_MODEL_COLUMNS) {
            baseModelColumn = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, baseModelColumn).toUpperCase();
            //DB里的不要转换
            columnName = columnName.toUpperCase();

            if (baseModelColumn.equals(columnName)) {
                return true;
            }
        }
        return false;
    }

}
