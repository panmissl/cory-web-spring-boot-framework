package com.cory.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Cory on 2017/5/10.
 */
@Data
public abstract class BaseModel implements Serializable {

    private Integer id;
    private Integer creator;
    private Integer modifier;
    private Date createTime = new Date();
    private Date modifyTime = new Date();
    private Boolean isDeleted = false;

    /**
     * 显示字段，比如对于status，一般都存的Name，所以页面显示时需要显示text字段。
     */
    protected Map<String, String> renderFieldMap = new HashMap<>();

    /**
     * 过滤字段，比如对于operateDate，是一个时间段，会存两个字段：operateDateStart, operateDateEnd。会在请求时自动解析并设置到此字段里，sql里直接写变量名即可
     */
    protected Map<String, Object> filterFieldMap = new HashMap<>();

    public void resetDateAndOperator(Date date, Integer operator) {
        if (null == this.id || this.id <= 0) {
            this.createTime = date;
            this.creator = operator;
        }
        this.modifyTime = date;
        this.modifier = operator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseModel baseModel = (BaseModel) o;

        return id != null ? id.equals(baseModel.id) : baseModel.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
