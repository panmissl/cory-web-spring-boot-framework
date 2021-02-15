package com.cory.context.config;

import com.cory.constant.Constants;
import com.cory.context.CoryEnv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;

/**
 * Created by Cory on 2021/2/13.
 */
@Slf4j
@Order(Constants.CONTEXT_INITIALIZER_ORDER_CONTEXT)
public class CoryContextApplicationContextInitializer implements ApplicationContextInitializer {

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        log.info("CoryContextApplicationContextInitializer initialize");

        String[] profiles = configurableApplicationContext.getEnvironment().getActiveProfiles();

        if (haveProfile(profiles, Constants.PROFILE_PROD)) {
            CoryEnv.IS_PROD = true;
        }
        if (haveProfile(profiles, Constants.PROFILE_DEV)) {
            CoryEnv.IS_DEV = true;
        }
    }

    private boolean haveProfile(String[] profiles, String target) {
        if (null == profiles || profiles.length == 0) {
            return false;
        }
        for (String profile : profiles) {
            if (profile.equals(target)) {
                return true;
            }
        }
        return false;
    }
}
