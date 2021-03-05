package com.cory.db.enums;

/**
 * Created by Cory on 2021/2/13.
 */
public enum  CoryDbType {

    INT("int(11)"),

    BIGINT("bigint(20)"),

    DOUBLE("double"),

    VARCHAR("varchar"),

    TEXT("text"),

    //SMALLINT
    BOOLEAN("smallint(1)"),

    DATE("datetime"),

    DATETIME("datetime"),

    ENUM("varchar(64)"),

    ;

    private String type;

    CoryDbType(String type) {
        this.type = type;
    }

    public String buildDbType(int len) {
        if (this.equals(VARCHAR)) {
            return type + "(" + len + ")";
        }
        return type;
    }

}
