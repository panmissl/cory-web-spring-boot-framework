package com.cory.enums;

/**
 * Created by Cory on 2017/5/20.
 */
public enum UserLevel implements CoryEnum {
    NORMAL("普通用户"),
    VIP("VIP"),
    SVIP("高级VIP"),
    ;

    private String text;

    UserLevel(String text) {
        this.text = text;
    }

    @Override
    public String text() {
        return text;
    }
}
