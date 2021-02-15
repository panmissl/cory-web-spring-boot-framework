package com.cory.context;

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
public class CoryContext implements Serializable {

    private static final TransmittableThreadLocal<CoryContext> THREAD_LOCAL = new TransmittableThreadLocal<>();

    private String ctx;
    private String ctxWithoutSlash;
    private String referer;
    private String requestURI;

    private String domainName;
    private String siteName;
    private String siteSlogan;
    private String jsDir;
    private String cssDir;
    private String imageDir;

    private String adminSkin;

    private Boolean debugMode;
    private String JS_CSS_MIN;

    public static CoryContext get() {
        return THREAD_LOCAL.get();
    }

    public static void set(CoryContext ctx) {
        THREAD_LOCAL.set(ctx);
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }

}
