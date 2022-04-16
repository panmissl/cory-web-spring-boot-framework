package com.cory.enums;

/**
 * Created by Cory on 2017/5/20.
 */
public enum ClusterStatus implements CoryEnum {
    online("在线", 1),
    offline("离线", 2),
    ;

    private String text;
    private Integer order;

    ClusterStatus(String text, Integer order) {
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
