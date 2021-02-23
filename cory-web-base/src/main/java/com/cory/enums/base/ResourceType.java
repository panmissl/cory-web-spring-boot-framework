package com.cory.enums.base;

import com.cory.enums.CoryEnum;

/**
 * Created by Cory on 2017/5/20.
 */
public enum ResourceType implements CoryEnum {
    URL("URL"),
    BUTTON("按钮"),
    ;

    private String text;

    ResourceType(String text) {
        this.text = text;
    }

    @Override
    public String text() {
        return text;
    }
}
