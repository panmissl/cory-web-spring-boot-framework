package com.cory.db.jdbc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Table {
    private String name;
    private String comment;
    private List<Column> columnList;
    private Map<String, Column> columnMap;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Table)) {
            return false;
        }
        Table t = (Table) o;
        if (!StringUtils.equals(name, t.name)) {
            return false;
        }
        if (CollectionUtils.isEmpty(columnList)) {
            if (CollectionUtils.isNotEmpty(t.columnList)) {
                return false;
            }
        } else {
            if (CollectionUtils.isEmpty(t.columnList)) {
                return false;
            }
            if (columnList.size() != t.columnList.size()) {
                return false;
            }
            for (Column column : columnList) {
                Column tc = t.columnMap.get(column.getName());
                if (null == tc || !column.equals(tc)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode() + columnList.hashCode() * 31;
    }

    public List<Column> differentColumns(Table table) {
        //以当前代码里的为准
        if (CollectionUtils.isEmpty(this.columnList)) {
            return null;
        }
        if (CollectionUtils.isEmpty(table.getColumnList())) {
            return this.columnList;
        }
        List<Column> list = new ArrayList<>();
        for (Column column : this.columnList) {
            Column dbColumn = table.getColumnMap().get(column.getName());
            if (null == dbColumn || !column.equals(dbColumn)) {
                list.add(column);
            }
        }
        return list;
    }
}