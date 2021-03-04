package com.cory.enums;

/**
 * Created by Cory on 2017/5/20.
 */
public enum UserStatus implements CoryEnum {
    NORMAL("正常", 1),
    LOCKED("锁定", 2),
    CLOSED("注销", 3),
    ;

    private String text;
    private Integer order;

    UserStatus(String text, Integer order) {
        this.text = text;
        this.order = order;
    }

    @Override
    public String text() {
        return text;
    }

    @Override
    public Integer order() {
        return order;
    }
}
