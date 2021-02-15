package com.cory.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

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
