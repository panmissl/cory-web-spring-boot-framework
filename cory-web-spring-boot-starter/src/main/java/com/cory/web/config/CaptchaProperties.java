package com.cory.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by Cory on 2021/2/9.
 */
@ConfigurationProperties(prefix = Constant.CAPTCHA_PREFIX)
@Data
public class CaptchaProperties {

    private boolean enable = false;
    private String urlPattern;

}
