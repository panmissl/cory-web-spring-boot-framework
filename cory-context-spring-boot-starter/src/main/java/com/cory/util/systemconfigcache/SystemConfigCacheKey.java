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

    /** ends with / : www.xx.com/ */
    public static final String JS_DIR = "js_dir";
    /** ends with / : www.xx.com/ */
    public static final String CSS_DIR = "css_dir";
    /** ends with / : www.xx.com/ */
    public static final String IMAGE_DIR = "image_dir";
    /** umixxx.js */
    public static final String JS_FILE = "js_file";
    /** umixxx.css */
    public static final String CSS_FILE = "css_file";

    /** true/false */
    public static final String DEBUG_MODE = "debug_mode";

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
    public static final String TOKEN_EXPIRE_TIME_IN_SECOND = "__token_expire_time_in_second__";
}
