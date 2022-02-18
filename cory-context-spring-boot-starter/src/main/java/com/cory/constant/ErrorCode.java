package com.cory.constant;

/**
 * 10000以下的code保留，业务层用大于10000的。
 *
 * <br />
 *
 * Created by Cory on 2017/5/13.
 */
public enum ErrorCode {

    /* common */
    GENERIC_ERROR(1000, "错误"),
    JSON_ERROR(1001, "JSON错误"),
    SAVE_ERROR(1002, "保存失败"),
    LOGIN_ERROR(1003, "登录失败"),
    REGISTER_ERROR(1004, "注册失败"),
    AUTH_ERROR(1005, "鉴权失败"),
    DB_ERROR(1006, "DB处理失败"),
    HTTP_STATUS_ERROR(1007, "Http状态错误"),
    CAPTCHA_ERROR(1008, "验证码错误"),
    ENCODING_ERROR(1009, "字符集错误：不支持的字符集"),
    ILLEGAL_ACCESS(1010, "非法访问"),

    ;

    private int code;
    private String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
