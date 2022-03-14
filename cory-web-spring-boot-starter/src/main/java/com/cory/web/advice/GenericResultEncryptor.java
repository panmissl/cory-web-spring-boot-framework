package com.cory.web.advice;

/**
 * @author cory
 * @date 2022/3/14
 */
public interface GenericResultEncryptor {

    /**
     * 加密数据
     * @param input 原始数据
     * @return 加密过的数据
     */
    Object encrypt(Object input);
}
