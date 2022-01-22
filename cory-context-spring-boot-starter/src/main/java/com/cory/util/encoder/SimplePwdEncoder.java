package com.cory.util.encoder;

import com.cory.constant.Constants;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by Cory on 2017/5/21.
 */
public class SimplePwdEncoder {

    private static final String DEFAULT_SALT = "!@#$%__123";

    public static String encode(String rawPassword, String salt) {
        if (StringUtils.isEmpty(salt)) {
            salt = DEFAULT_SALT;
        }
        String encoded = Base64Encoder.encode(rawPassword + salt, Constants.UTF8);
        encoded = "__" + encoded + "__";
        return encoded;
    }

    public static void main(String[] args) {
        System.out.println(encode("123456", "!@0#$1%^2*()"));
    }
}
