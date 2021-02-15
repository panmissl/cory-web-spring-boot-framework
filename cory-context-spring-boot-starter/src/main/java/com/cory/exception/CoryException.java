package com.cory.exception;

import com.cory.constant.ErrorCode;

/**
 * Created by Cory on 2017/5/13.
 */
public class CoryException extends RuntimeException {

    private int errorCode;
    private String errorMsg;

    public CoryException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public CoryException(ErrorCode errorCode, Object... params) {
        this.errorCode = errorCode.getCode();
        this.errorMsg = this.buildErrorMsg(errorCode.getMessage(), params);
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Override
    public String getMessage() {
        return this.getLocalizedMessage();
    }

    @Override
    public String getLocalizedMessage() {
        //return "[errorCode: " + errorCode + "], errorMsg: " + (null == errorMsg ? "" : errorMsg);
        return null == errorMsg ? "错误" : errorMsg;
    }

    private String buildErrorMsg(String msg, Object... params) {
        if (null == params || params.length == 0) {
            return msg;
        }
        StringBuilder sb = new StringBuilder(msg);
        sb.append("[");
        int len = params.length, index = 0;
        for (Object param : params) {
            sb.append(null == param ? "NULL" : param.toString());
            if (index < len - 1) {
                sb.append(", ");
            }
            index ++;
        }
        sb.append("]");
        return sb.toString();
    }

    public static void main(String[] args) {
        CoryException e = new CoryException(ErrorCode.GENERIC_ERROR);
        System.out.println(e.buildErrorMsg("123"));
        System.out.println(e.buildErrorMsg("123", null));
        System.out.println(e.buildErrorMsg("123", null, null));
        System.out.println(e.buildErrorMsg("123", null, "abc"));
        System.out.println(e.buildErrorMsg("123", null, "abc", 123));
        System.out.println(e.buildErrorMsg("123", null, 123));
    }
}
