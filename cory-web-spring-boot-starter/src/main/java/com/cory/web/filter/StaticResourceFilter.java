package com.cory.web.filter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 放在resource/static/目录下的文件，会被当作静态资源处理。默认已经加了jquery.min.js和一些图片(static/image/)
 *
 * @author cory
 * @date 2024/1/3
 */
@Component
public class StaticResourceFilter implements OrderedFilter {

    private static final String STATIC_PREFIX = "/static/";

    private static final Map<String, String> CONTENT_TYPE_MAP = new HashMap<>(128);

    static {
        put(".htm", "text/html;charset=utf-8");
        put(".html", "text/html;charset=utf-8");
        put(".xhtml", "application/xhtml+xml;charset=utf-8");
        put(".txt", "text/plain;charset=utf-8");
        put(".css", "text/css;charset=utf-8");
        put(".js", "text/javascript;charset=utf-8");
        put(".json", "application/json;charset=utf-8");
        put(".png", "image/png");
        put(".jpeg", "image/jpeg");
        put(".jpg", "image/jpeg");
        put(".webp", "image/webp");
        put(".gif", "image/gif");
        put(".bmp", "image/bmp");
        put(".ico", "image/vnd.microsoft.icon");
        put(".svg", "image/svg+xml");
        put(".tif", "image/tiff");
        put(".tiff", "image/tiff");
        put(".tff", "font/ttf");
        put(".woff", "font/woff");
        put(".woff2", "font/woff2");
        put(".mp3", "audio/mpeg");
        put(".wav", "audio/wav");
        put(".aac", "audio/aac");
        put(".weba", "audio/webm");
        put(".mp4", "video/mp4");
        put(".webm", "video/webm");
        put(".avi", "video/x-msvideo");
        put(".bin", "application/octet-stream");
        put(".csv", "text/csv");
        put(".doc", "application/msword");
        put(".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        put(".xls", "application/vnd.ms-excel");
        put(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        put(".ppt", "application/vnd.ms-powerpoint");
        put(".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        put(".pdf", "application/pdf");
        put(".xml", "text/xml");
        put(".rar", "application/x-rar-compressed");
        put(".tar", "application/x-tar");
        put(".zip", "application/zip");
        put(".7z", "application/x-7z-compressed");
        put(".sh", "application/x-sh");
    }

    @Autowired
    private ResourceLoader resourceLoader;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String uri = request.getRequestURI();
        if (!uri.startsWith(STATIC_PREFIX)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        response.setContentType(parseContentType(uri));

        uri = uri.substring(1);

        Resource resource = resourceLoader.getResource("classpath:" + uri);
        InputStream inputStream = resource.getInputStream();

        byte[] buffer = new byte[2048];
        int len;
        do {
            len = inputStream.read(buffer);
            response.getOutputStream().write(buffer, 0, len);
            response.getOutputStream().flush();
        } while (len > 0);
    }

    private String parseContentType(String uri) {
        int index = uri.lastIndexOf('.');
        if (index < 0) {
            return "text/text;charset=utf-8";
        }

        String ext = uri.substring(index);
        String contentType = CONTENT_TYPE_MAP.get(ext);
        if (StringUtils.isNotBlank(contentType)) {
            return contentType;
        }

        return "text/text;charset=utf-8";
    }

    private static void put(String ext, String contentType) {
        CONTENT_TYPE_MAP.put(ext, contentType);
        CONTENT_TYPE_MAP.put(ext.toUpperCase(), contentType);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }
}
