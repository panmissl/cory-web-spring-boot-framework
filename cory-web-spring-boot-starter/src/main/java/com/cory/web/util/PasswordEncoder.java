package com.cory.web.util;

import com.cory.util.encoder.SimplePwdEncoder;

/**
 * Created by Cory on 2017/5/28.
 */
public class PasswordEncoder {

    private String salt;

    public String encode(String rawPassword) {
        return SimplePwdEncoder.encode(rawPassword, this.getSalt());
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}
