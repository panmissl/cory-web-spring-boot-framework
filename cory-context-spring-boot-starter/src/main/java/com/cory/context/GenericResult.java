package com.cory.context;

import com.alibaba.fastjson.JSON;
import com.cory.constant.ErrorCode;
import com.cory.exception.CoryException;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by Cory on 2017/5/13.
 */
@Data
public class GenericResult implements Serializable {

    private boolean success = true;

    private int errorCode;
    private String errorMsg;
    private Object object;

    /**
     * success = true
     */
    public GenericResult() {}

    /**
     * success = false;
     * @param exception
     */
    public GenericResult(CoryException exception) {
        this.errorCode = exception.getErrorCode();
        this.errorMsg = exception.getMessage();
        this.success = false;
    }

    /**
     * success = false;
     * @param errorCode
     */
    public GenericResult(ErrorCode errorCode) {
        this.errorCode = errorCode.getCode();
        this.errorMsg = errorCode.getMessage();
        this.success = false;
    }

    public static GenericResult success() {
        return new GenericResult();
    }

    public static GenericResult success(Object object) {
        GenericResult r = new GenericResult();
        r.setObject(object);
        return r;
    }

    public static GenericResult fail(ErrorCode errorCode) {
        return new GenericResult(errorCode);
    }

    public static GenericResult fail(CoryException ex) {
        return new GenericResult(ex);
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
