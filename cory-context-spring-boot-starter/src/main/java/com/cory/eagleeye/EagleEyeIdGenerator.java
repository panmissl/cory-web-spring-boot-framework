package com.cory.eagleeye;

import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

/**
 * @author cory
 * @date 2022/3/1
 */
public class EagleEyeIdGenerator {

    /**
     * @param parentEagleEyeId 之前的parentEagleEyeId，比如通过Http调用时(或者多线程里)，如果已经在header里设置了，则直接用它。否则才生成新的
     * @return
     */
    public static String generateEagleEyeId(String parentEagleEyeId) {
        if (StringUtils.isNotBlank(parentEagleEyeId)) {
            return parentEagleEyeId;
        }
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

}
