package com.cory.util.systemconfigcache;

/**
 * Created by Cory on 2017/5/14.
 */
public class SystemConfigCacheKey {

    /** www.top.com */
    public static final String DOMAIN_NAME = "domain_name";
    /** 靓靓排行 */
    public static final String SITE_NAME = "site_name";
    /** 靓靓排行，您想要的都在这里 */
    public static final String SITE_SLOGAN = "site_slogan";
    /** ends with / : www.top.com:8000/ */
    public static final String JS_DOMAIN_PORT = "js_domain_port";
    /** ends with / : www.top.com:8001/ */
    public static final String CSS_DOMAIN_PORT = "css_domain_port";
    /** ends with / : www.top.com:8002/ */
    public static final String IMAGE_DOMAIN_PORT = "image_domain_port";
    /** ends with / : www.top.com:8003/ */
    public static final String JS_CSS_MIN = "js_css_min";

    /** debug mode */
    public static final String DEBUG = "debug";

    /** admin skin: blue/green/... */
    public static final String ADMIN_SKIN = "admin-skin";
    /** login skin: default/simple */
    public static final String LOGIN_SKIN = "login-skin";

    /** root role name */
    public static final String ROOT_ROLE_NAME = "root_role_name";
    /** admin role names */
    public static final String ADMIN_ROLE_NAMES = "admin_role_names";
    /** anon role names */
    public static final String ANON_ROLE_NAME = "anon_role_name";
    /** normal role names */
    public static final String NORMAL_ROLE_NAME = "normal_role_name";

    /** user_enabled */
    public static final String USER_ENABLED = "user_enabled";
    /** pwd salt */
    public static final String PWD_SALT = "pwd_salt";

    /** static resource path */
    public static final String STATIC_RESOURCE_PATH = "STATIC_RESOURCE_PATH";
    /** context path */
    public static final String CONTEXT_PATH = "CONTEXT_PATH";

    /** quartz ip */
    public static final String QUARTZ_IP = "quartz_ip";

    /** token configs */
    public static final String TOKEN_CONFIGS = "__token_configs__";
}
