package com.cory.enums;

/**
 * Created by Cory on 2017/5/20.
 */
public enum ExportJobStatus implements CoryEnum {
    init("已创建", 1),
    running("运行中", 2),
    success("成功", 3),
    fail("失败", 4),
    ;

    private String text;
    private Integer order;

    ExportJobStatus(String text, Integer order) {
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
