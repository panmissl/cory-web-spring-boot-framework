package com.cory.enums;

/**
 * Created by Cory on 2017/5/20.
 */
public enum UserType implements CoryEnum {
    SITE("本站", 1),
    WEIXIN("微信", 2),
    QQ("QQ", 3),
    WEIBO("微博", 4),
    ALI("支付宝", 5),
    ;

    private String text;
    private Integer order;

    UserType(String text, Integer order) {
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
