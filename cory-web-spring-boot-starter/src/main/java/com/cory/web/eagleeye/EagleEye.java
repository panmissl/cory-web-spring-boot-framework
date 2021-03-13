package com.cory.web.eagleeye;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by Cory on 2017/5/13.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EagleEye implements Serializable {

    private static final TransmittableThreadLocal<EagleEye> THREAD_LOCAL = new TransmittableThreadLocal<>();

    private String eagleEyeId;

    public static EagleEye get() {
        EagleEye eagleEye = THREAD_LOCAL.get();
        if (null == eagleEye) {
            eagleEye = new EagleEye();
            THREAD_LOCAL.set(eagleEye);
        }
        return eagleEye;
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }

}
