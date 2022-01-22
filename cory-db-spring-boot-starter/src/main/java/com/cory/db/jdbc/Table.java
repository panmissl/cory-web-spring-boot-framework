package com.cory.db.jdbc;

import com.cory.constant.ErrorCode;
import com.cory.exception.CoryException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        if (!StringUtils.equals(name.toUpperCase(), t.name.toUpperCase())) {
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

    public List<Column> differentColumns(Table table, COLUMN_TYPE type) {
        //以当前代码里的为准

        if (type.equals(COLUMN_TYPE.ADD)) {
            //添加：当前有，但数据库没有
            if (CollectionUtils.isEmpty(table.getColumnList())) {
                return this.columnList;
            }
            if (CollectionUtils.isEmpty(columnList)) {
                return new ArrayList<>();
            }
            return columnList.stream().filter(c -> null == table.getColumnMap().get(c.getName())).collect(Collectors.toList());
        } else if (type.equals(COLUMN_TYPE.DELETE)) {
            //删除：当前没有，但数据库有
            if (CollectionUtils.isEmpty(this.columnList)) {
                return table.getColumnList();
            }
            if (CollectionUtils.isEmpty(table.getColumnList())) {
                return new ArrayList<>();
            }
            return table.getColumnList().stream().filter(c -> null == this.columnMap.get(c.getName())).collect(Collectors.toList());
        } else if (type.equals(COLUMN_TYPE.MODIFY)) {
            //修改：当前和数据库有，但不一样
            if (CollectionUtils.isEmpty(this.columnList) || CollectionUtils.isEmpty(table.getColumnList())) {
                return new ArrayList<>();
            }
            return this.columnList.stream()
                    .filter(c -> null != table.getColumnMap().get(c.getName()) && !c.equals(table.getColumnMap().get(c.getName())))
                    .collect(Collectors.toList());
        } else {
            throw new CoryException(ErrorCode.DB_ERROR, "un supported column type(COLUMN_TYPE)");
        }
    }

    public enum COLUMN_TYPE {
        ADD,
        MODIFY,
        DELETE,
    }
}