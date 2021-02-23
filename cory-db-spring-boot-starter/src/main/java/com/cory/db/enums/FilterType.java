package com.cory.db.enums;

/**
 * Created by Cory on 2021/2/13.
 */
public enum FilterType {

    /** 文本 */
    TEXT,

    /** 下拉：使用固定数据(自己在前端提供数据: [{label: 'abc', value: '123'},...]) */
    SELECT,

    /** 远程下拉：通过url加载数据(前端设定显示和值的字段，默认用：label, value，如果有自定义的可以传入) */
    REMOTE_SELECT,

    /** 日期：格式：yyyy-MM-dd。是此类型时，因为选择的是一个时间段，所以提交时的字段名称时Filed名称加上Start和End，比如：approveDateStart, approveDateEnd，时间相关的都是这样处理 */
    DATE,

    /** 时间：格式：HH:mm:ss */
    TIME,

    /** 日期+时间：格式：yyyy-MM-dd HH:mm:ss*/
    DATE_TIME,

    ;

}
