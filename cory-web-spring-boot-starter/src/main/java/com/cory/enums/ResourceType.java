package com.cory.enums;

/**
 * Created by Cory on 2017/5/20.
 */
public enum ResourceType implements CoryEnum {
    URL("URL", 1),
    BUTTON("按钮", 2),
    ;

    private String text;
    private Integer order;

    ResourceType(String text, Integer order) {
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
