package com.cory.constant;

/**
 * Created by Cory on 2017/5/13.
 */
public class Constants {

    public static final String PROFILE_PROD = "prod";

    public static final String DATA_DICT_ROOT_VALUE = "cory_web_data_dict_root";
    public static final String DATA_DICT_ROOT_PARENT_VALUE = "cory_web_data_dict_root_parent_000";

    public static final int CONTEXT_INITIALIZER_ORDER_CONTEXT = 1000;
    public static final int CONTEXT_INITIALIZER_ORDER_DB = 990;
    public static final int CONTEXT_INITIALIZER_ORDER_WEB = 980;

    public static final String EXCEPTION_ATTR = "__CORY_EXCEPTION_ATTR__";

    public static final String RETURN_URL = "returnUrl";

    /**
     * 路径分隔符
     */
    public static final String SPT = "/";
    /**
     * 索引页
     */
    public static final String INDEX = "index";
    /**
     * 默认模板
     */
    public static final String DEFAULT = "default";
    /**
     * UTF-8编码
     */
    public static final String UTF8 = "UTF-8";
    /**
     * ISO-8859-1编码
     */
    public static final String ISO88591 = "ISO-8859-1";
    /**
     * 提示信息
     */
    public static final String MESSAGE = "message";
    /**
     * cookie中的JSESSIONID名称
     */
    public static final String JSESSION_COOKIE = "JSESSIONID";
    /**
     * url中的jsessionid名称
     */
    public static final String JSESSION_URL = "jsessionid";

    /**
     * 放request里的分页对象属性名字，装的是Pagination对象
     */
    public static final String PAGINATION = "pagination";

    /**
     * 放request里的model对象属性名字
     */
    public static final String MODEL = "model";

    /**
     * 放request里的list对象属性名字
     */
    public static final String LIST = "list";

    public static final String SUCCESS = "success";

    public static final String DEFAULT_CONTENT_TYPE = "text/html;charset=" + UTF8;

    public static final String CURRENT_USER = "currentUser";

    /**
     * 如果校验失败，错误消息放在model里，也就是request里，Key为此值
     */
    public static final String VALIDATE_ERROR_MESSAGE = "VALIDATE_ERROR_MESSAGE";

    public static final String HEADER_ACCEPT_ENCODING = "accept-encoding";
    public static final String HEADER_CONTENT_LENGTH = "content-length";
    public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
    public static final String REQUEST_HEADER_KEY_REAL_IP = "X-Real-IP";

    public static final String DATE_FORMAT_SHORT_WITH_DASH = "yyyy-MM-dd";
    public static final String DATE_FORMAT_SHORT_WITHOUT_DASH = "yyyyMMdd";
    public static final String DATE_FORMAT_FULL_WITH_DASH = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT_FULL_EXTRA_WITH_DASH = "yyyy-MM-dd HH:mm:ss.S";
    public static final String DATE_FORMAT_FULL_EXTRA_LONG_WITH_DASH = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String DATE_FORMAT_FULL_WITHOUT_DASH = "yyyyMMddHHmmss";
    public static final String[] ALL_DATE_FORMAT = new String[] {
            DATE_FORMAT_SHORT_WITH_DASH,
            DATE_FORMAT_SHORT_WITHOUT_DASH,
            DATE_FORMAT_FULL_WITH_DASH,
            DATE_FORMAT_FULL_WITHOUT_DASH,
            DATE_FORMAT_FULL_EXTRA_WITH_DASH,
            DATE_FORMAT_FULL_EXTRA_LONG_WITH_DASH,
    };

    public static final String[] BASE_MODEL_COLUMNS = new String[] {"id", "creator", "modifier", "createTime", "modifyTime", "isDeleted"};

    public static final String FILTER_FIELD_POSTFIX_START = "Start";
    public static final String FILTER_FIELD_POSTFIX_END = "End";
    public static final String FILTER_FIELD_POSTFIX_IN = "In";
    public static final String FILTER_FIELD_POSTFIX_LIKE = "Like";
    public static final String FILTER_FIELD_POSTFIX_LIKE_LEFT = "LikeLeft";
    public static final String FILTER_FIELD_POSTFIX_LIKE_RIGHT = "LikeRight";
    public static final String FILTER_FIELD_POSTFIX_NOT_IN = "NotIn";
    public static final String FILTER_FIELD_POSTFIX_NOT_LIKE = "NotLike";
    public static final String FILTER_FIELD_POSTFIX_NOT_LIKE_LEFT = "NotLikeLeft";
    public static final String FILTER_FIELD_POSTFIX_NOT_LIKE_RIGHT = "NotLikeRight";
    public static final String FILTER_FIELD_POSTFIX_NOT_EQ = "NotEq";
}
