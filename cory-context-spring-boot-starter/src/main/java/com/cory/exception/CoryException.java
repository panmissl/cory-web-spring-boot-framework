package com.cory.exception;

import com.cory.constant.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 可以用{@link ErrorCode}构建，也可以直接用builder进行构建。
 *
 * <br />
 *
 * Created by Cory on 2017/5/13.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoryException extends RuntimeException {

    private String errorCode;
    private String errorMsg;

    public CoryException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public CoryException(ErrorCode errorCode, Object... params) {
        this.errorCode = errorCode.getCode() + "";
        this.errorMsg = this.buildErrorMsg(errorCode.getMessage(), params);
    }

    @Override
    public String getMessage() {
        return this.getLocalizedMessage();
    }

    @Override
    public String getLocalizedMessage() {
        //return "[errorCode: " + errorCode + "], errorMsg: " + (null == errorMsg ? "" : errorMsg);
        return null == errorMsg ? ("Error, code: " + errorCode) : errorMsg;
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
