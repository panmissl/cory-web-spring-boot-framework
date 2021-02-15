package com.cory.db.processor;

import com.cory.constant.ErrorCode;
import com.cory.context.CoryEnv;
import com.cory.db.annotations.Field;
import com.cory.db.annotations.Model;
import com.cory.db.config.CoryDbProperties;
import com.cory.db.jdbc.Column;
import com.cory.db.jdbc.CoryDb;
import com.cory.db.jdbc.CorySqlBuilder.CorySqlInfo;
import com.cory.db.jdbc.Table;
import com.cory.exception.CoryException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.util.AnnotatedTypeScanner;

import java.util.*;

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
            ") ENGINE=InnoDB AUTO_INCREMENT=10001 DEFAULT CHARSET=utf8 COMMENT='%s';";

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

        checkDb();

        Map<String, Table> newTableMap = queryNowTables();
        if (MapUtils.isEmpty(newTableMap)) {
            log.info("no Model found, skip db check.");
            return;
        }

        Map<String, Table> dbTableMap = queryNotNullDbTables();

        boolean same = true;
        StringBuilder msgBuilder = new StringBuilder();

        for (Map.Entry<String, Table> entry : newTableMap.entrySet()) {
            String tableName = entry.getKey();
            Table table = entry.getValue();

            Table dbTable = dbTableMap.get(tableName);
            if (null == dbTable) {
                same = false;
                if (CoryEnv.IS_DEV) {
                    createTable(table);
                }
                if (CoryEnv.IS_PROD) {
                    msgBuilder.append("table " + tableName + " does not exist;\n");
                }
                continue;
            }

            if (!table.equals(dbTable)) {
                same = false;
                msgBuilder.append("table " + tableName + " does not match in db:\n");
                List<Column> columnList = table.differentColumns(dbTable);
                if (CollectionUtils.isNotEmpty(columnList)) {
                    for (Column column : columnList) {
                        if (CoryEnv.IS_DEV) {
                            syncColumn(column);
                        }
                        if (CoryEnv.IS_PROD) {
                            msgBuilder.append("\t" + column.buildDDL() + ",\n");
                        }
                    }
                }
            }
        }

        if (!same && CoryEnv.IS_PROD) {
            throw new CoryException(ErrorCode.DB_ERROR, msgBuilder.toString());
        }
    }

    private void checkDb() {
        int count = coryDb.selectCount(CorySqlInfo.builder().sql("select count(*) from information_schema.SCHEMATA where schema_name = ?").params(Arrays.asList(database)).build());
        if (count > 0) {
            return;
        }
        if (CoryEnv.IS_DEV) {
            coryDb.executeSql("create database " + database);
            log.info("create database " + database);
        }
        if (CoryEnv.IS_PROD) {
            throw new CoryException(ErrorCode.DB_ERROR, "database " + database + " does not exist, please create it then restart");
        }
    }

    private void syncColumn(Column column) {
        //alter table 表名 drop column 列名;
        //alter table 表名 add column 列名 列的数据类型;
        String sql = String.format("alter table %s drop column %s;", column.getTableName(), column.getName()) +
                String.format("alter table %s add column %s;", column.getTableName(), column.buildDDL());
        coryDb.executeSql(sql);

        log.info("sync column " + column.getTableName() + "." + column.getName());
    }

    private void createTable(Table table) {
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
        ) ENGINE=InnoDB AUTO_INCREMENT=10001 DEFAULT CHARSET=utf8 COMMENT='资源表';
        */

        StringBuilder builder = new StringBuilder();
        builder.append(String.format(CREATE_TABLE_HEADER, table.getName()));
        builder.append(BASE_COLUMNS_DDL);

        for (Column column : table.getColumnList()) {
            builder.append(column.buildDDL());
            builder.append(",");
        }

        builder.append(String.format(CREATE_TABLE_FOOTER, table.getComment()));

        coryDb.executeSql(builder.toString());

        log.info("create table " + table.getName());
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
            if (null == model) {
                continue;
            }

            String tableName = model.table();

            //for duplicate check
            Integer ct = tableDuplicateMap.get(tableName);
            if (null == ct) {
                ct = 0;
            }
            tableDuplicateMap.put(tableName, ct + 1);

            java.lang.reflect.Field[] fields = cls.getFields();
            if (null != fields || fields.length > 0) {
                for (java.lang.reflect.Field javaField : fields) {
                    Field field = javaField.getAnnotation(Field.class);
                    if (null == field) {
                        continue;
                    }

                    String columnName = field.name();
                    String columnDefault = field.defaultValue();
                    boolean nullable = field.nullable();
                    String columnType = field.type().buildDbType(field.len());
                    String columnComment = field.comment();

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
}
