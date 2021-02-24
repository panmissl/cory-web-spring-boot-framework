package com.cory.enums;

/**
 * Created by Cory on 2017/5/20.
 */
public enum UserType implements CoryEnum {
    SITE("本站"),
    WEIXIN("微信"),
    QQ("QQ"),
    WEIBO("微博"),
    ALI("支付宝"),
    ;

    private String text;

    UserType(String text) {
        this.text = text;
    }

    @Override
    public String text() {
        return text;
    }
}
