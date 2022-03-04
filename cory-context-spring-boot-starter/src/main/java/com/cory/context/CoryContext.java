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

    private static final long serialVersionUID = -6745208140519370719L;

    private String ctx;
    private String ctxWithoutSlash;
    private String referer;
    private String requestURI;
    //登录成功后的跳转URL
    private String successUrl;

    private String domainName;
    private String siteName;
    private String siteSlogan;
    /** @see SystemConfigCacheKey#SITE_KEYWORDS */
    private String siteKeywords;
    /** @see SystemConfigCacheKey#SITE_DESCRIPTION */
    private String siteDescription;
    /** @see SystemConfigCacheKey#SITE_DESCRIPTION_BODY */
    private String siteDescriptionBody;

    //静态资源路径，以斜杠结尾，比如：http://static.xx.com/static/0.0.1/
    //版本号用另外一个系统配置存储
    private String staticDir;
    //只有一个js文件和一个css文件。文件名比如：umixxx.js, umixxx.css
    private String jsFile;
    private String cssFile;

    //true/false
    private String debugMode;
    //true/false
    private String registerEnable;

    private String adminSkin;

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
