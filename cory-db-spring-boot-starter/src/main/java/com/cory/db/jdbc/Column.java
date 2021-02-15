package com.cory.db.jdbc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Column {
    private String name;
    private String tableName;
    private String defaultValue;
    private boolean nullable;
    private String columnType;
    private String columnComment;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Column)) {
            return false;
        }
        Column c = (Column) o;
        return StringUtils.equals(name, c.name) &&
                StringUtils.equals(tableName, c.tableName) &&
                StringUtils.equals(defaultValue, c.defaultValue) &&
                nullable == c.nullable &&
                StringUtils.equals(columnType, c.columnType);
    }

    @Override
    public int hashCode() {
        return name.hashCode() + tableName.hashCode() * 31 + (null == defaultValue ? 0 : defaultValue.hashCode() * 13) + ((Boolean) nullable).hashCode() * 17 + columnType.hashCode() * 11;
    }

    public String buildDDL() {
        //`modify_time` datetime NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '修改时间',
        String na = nullable ? "NULL" : "NOT NULL";
        String dv = StringUtils.isBlank(defaultValue) ? "" : ("DEFAULT '" + defaultValue + "'");
        return String.format("`%s` %s %s %s COMMENT '%s'", name, columnType, na, dv, columnComment);
    }
}