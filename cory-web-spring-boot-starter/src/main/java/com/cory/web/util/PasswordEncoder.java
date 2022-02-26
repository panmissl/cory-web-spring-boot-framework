package com.cory.web.util;

import com.cory.util.encoder.Md5Encoder;

/**
 * Created by Cory on 2017/5/28.
 */
public class PasswordEncoder {

    private String salt;

    public String encode(String userName, String rawPassword) {
        //用MD5加密，不用简单加密
        //return SimplePwdEncoder.encode(rawPassword, this.getSalt());
        return Md5Encoder.encode(rawPassword);
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}
