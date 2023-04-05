package com.cory.util.systemconfigcache;

/**
 * Created by Cory on 2017/5/14.
 */
public class SystemConfigCacheKey {

    /** 网站域名 */
    public static final String DOMAIN_NAME = "domain_name";
    /** 站点标识：会渲染到html里的window对象里，用于区分站点，比如一套代码用多个站点时 */
    public static final String SITE = "site";
    /** 网站名称：显示在网页的标题栏里 */
    public static final String SITE_NAME = "site_name";
    /** 网站标语：可选。如果有，显示在标题栏里，和网站名称用 - 分隔 */
    public static final String SITE_SLOGAN = "site_slogan";
    /** 网站关键字：可选。SEO用，用英语半角逗号分隔 */
    public static final String SITE_KEYWORDS = "site_keywords";
    /** 网站描述：可选。SEO用，一段文本 */
    public static final String SITE_DESCRIPTION = "site_description";
    /** 可选。专门给SEO用的，放在网站的html里，让搜索引擎搜索时展示的。是一段HTML，比如可以放图片，放介绍文字等。正常访问页面加载时马上删除，所以不要在这里处理正常访问的逻辑 */
    public static final String SITE_DESCRIPTION_BODY = "site_description_body";

    /** session过期时间，单位：分钟 */
    public static final String SESSION_TIMEOUT_IN_MINUTE = "session_timeout_in_minute";

    /** ends with / : www.xx.com/static/ */
    public static final String STATIC_DIR = "static_dir";
    /** 0.0.1 */
    public static final String STATIC_VERSION = "static_version";
    /** umixxx.js */
    public static final String JS_FILE = "js_file";
    /** umixxx.css */
    public static final String CSS_FILE = "css_file";
    /** favicon.ico */
    public static final String FAVICON_FILE = "favicon_file";

    /** true/false */
    public static final String DEBUG_MODE = "debug_mode";

    /** admin skin: blue/green/... */
    public static final String ADMIN_SKIN = "admin-skin";
    /** login skin: default/simple */
    public static final String LOGIN_SKIN = "login-skin";
    /** 是否开启注册功能 */
    public static final String REGISTER_ENABLE = "register_enable";

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
