package com.cory.enums;

/**
 * Created by Cory on 2017/5/20.
 */
public enum UserLevel implements CoryEnum {
    NORMAL("普通用户", 1),
    VIP("VIP", 2),
    SVIP("高级VIP", 3),
    ;

    private String text;
    private Integer order;

    UserLevel(String text, Integer order) {
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
