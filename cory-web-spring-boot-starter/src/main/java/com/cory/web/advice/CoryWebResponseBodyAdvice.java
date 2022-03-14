package com.cory.web.advice;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.cory.constant.Constants;
import com.cory.context.GenericResult;
import com.cory.util.ClassUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.io.IOException;
import java.lang.annotation.Annotation;

/**
 * Created by Cory on 2021/2/13.
 */
@ControllerAdvice
public class CoryWebResponseBodyAdvice implements ResponseBodyAdvice, ApplicationContextAware {

    public static final String CONTENT_TYPE_DEFAULT_TYPE = "text";
    public static final String CONTENT_TYPE_DEFAULT_SUB_TYPE = "html";
    public static final String CONTENT_TYPE_PARAMETER = ";charset:utf-8";

    private ApplicationContext ctx;

    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {
        if (isMultipartFile(methodParameter)) {
            return false;
        }
        if (methodParameter.hasMethodAnnotation(ResponseBody.class)) {
            return true;
        }
        if (ClassUtil.classHasAnnotationWithParent(methodParameter.getContainingClass(), RestController.class)) {
            return true;
        }
        if (ClassUtil.classHasAnnotationWithParent(methodParameter.getContainingClass(), ResponseBody.class)) {
            return true;
        }
        if (aClass.equals(StringHttpMessageConverter.class) ||
                aClass.equals(FastJsonHttpMessageConverter.class) ||
                aClass.equals(GsonHttpMessageConverter.class)) {
            return true;
        }
        if (FastJsonHttpMessageConverter.class.isAssignableFrom(aClass) ||
                MappingJackson2HttpMessageConverter.class.isAssignableFrom(aClass) ||
                StringHttpMessageConverter.class.isAssignableFrom(aClass)) {
            return true;
        }
        return false;
    }

    private boolean isMultipartFile(MethodParameter methodParameter) {
        return MultipartFile.class.isAssignableFrom(methodParameter.getMethod().getReturnType());
    }

    @Nullable
    @Override
    public Object beforeBodyWrite(@Nullable Object o, MethodParameter methodParameter, MediaType mediaType, Class aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        //不需要包装，直接输出
        GenericResultExclude genericResultExclude = findAnnotation(methodParameter, GenericResultExclude.class);
        if (null != genericResultExclude) {
            writeOriginal(o, mediaType, serverHttpResponse, genericResultExclude);
            return null;
        }
        if (null == o) {
            return GenericResult.success();
        }
        GenericResult result;
        if (o instanceof GenericResult) {
            result = (GenericResult) o;
        } else {
            result = GenericResult.success(o);
        }

        //加密
        if (null != result.getObject()) {
            GenericResultEncrypt encrypt = findAnnotation(methodParameter, GenericResultEncrypt.class);
            if (null != encrypt) {
                GenericResultEncryptor encryptor = ctx.getBean(GenericResultEncryptor.class);
                result.setObject(encryptor.encrypt(result.getObject()));
            }
        }

        return result;
    }

    private <T extends Annotation> T findAnnotation(MethodParameter methodParameter, Class<T> tClass) {
        T t = AnnotationUtils.findAnnotation(methodParameter.getMethod(), tClass);
        if (null == t) {
            t = AnnotationUtils.findAnnotation(methodParameter.getDeclaringClass(), tClass);
        }
        return t;
    }

    private void writeOriginal(Object o, MediaType mediaType, ServerHttpResponse response, GenericResultExclude genericResultExclude) {
        if (null == o) {
            return;
        }
        String body;
        if (o instanceof String || !genericResultExclude.renderAsJson()) {
            body = o.toString();
        } else {
            body = JSON.toJSONString(o);
        }

        try {
            String type = mediaType.getType();
            if (StringUtils.isBlank(type)) {
                type = CONTENT_TYPE_DEFAULT_TYPE;
            }
            String subType = mediaType.getSubtype();
            if (StringUtils.isBlank(subType)) {
                subType = CONTENT_TYPE_DEFAULT_SUB_TYPE;
            }
            String contentType = type + "/" + subType + CONTENT_TYPE_PARAMETER;

            response.getHeaders().add("Content-Type", contentType);
            response.getBody().write(body.getBytes(Constants.UTF8));
            response.getBody().flush();
        } catch (IOException e) {
            //ignore
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }
}
