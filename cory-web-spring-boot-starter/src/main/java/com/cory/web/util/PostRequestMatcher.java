package com.cory.web.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

/**
 * Created by Cory on 2017/5/17.
 */
public class PostRequestMatcher implements RequestMatcher {

    private Pattern allowedMethods = Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");

    private String excludeUrlRegExp = ".*?/openapi/.*";

    /* (non-Javadoc)
     * @see org.springframework.security.web.util.matcher.RequestMatcher#matches(javax.servlet.http.HttpServletRequest)
     */
    public boolean matches(HttpServletRequest request) {
        if (isExcludeUrl(request)) {
            return false;
        }
        if (ServletFileUpload.isMultipartContent(request)) {
            return false;
        }
        return !allowedMethods.matcher(request.getMethod()).matches();
    }

    private boolean isExcludeUrl(HttpServletRequest request) {
        String uri = request.getRequestURI().substring(request.getContextPath().length());
        return null != uri && StringUtils.isNotBlank(excludeUrlRegExp) && uri.matches(excludeUrlRegExp);
    }

    public void setExcludeUrlRegExp(String excludeUrlRegExp) {
        this.excludeUrlRegExp = excludeUrlRegExp;
    }
}
