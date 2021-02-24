package com.cory.enums;

/**
 * Created by Cory on 2017/5/20.
 */
public enum UserStatus implements CoryEnum {
    NORMAL("正常"),
    LOCKED("锁定"),
    CLOSED("注销"),
    ;

    private String text;

    UserStatus(String text) {
        this.text = text;
    }

    @Override
    public String text() {
        return text;
    }
}
