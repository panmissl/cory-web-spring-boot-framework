package com.cory.enums;

/**
 * Created by Cory on 2021/2/22.
 */
public interface CoryEnum {

    /** name，相当于code，存数据库 */
    String name();

    /** 在页面上显示的内容 */
    String text();

    /** 排序：数字越小约在前 */
    Integer order();
}
