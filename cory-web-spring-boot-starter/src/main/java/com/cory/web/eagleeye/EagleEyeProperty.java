package com.cory.web.eagleeye;

import ch.qos.logback.core.PropertyDefinerBase;
import com.cory.eagleeye.EagleEye;

/**
 * Created by Cory on 2021/3/13.
 */
public class EagleEyeProperty extends PropertyDefinerBase {

    @Override
    public String getPropertyValue() {
        return EagleEye.get().getEagleEyeId();
    }
}
