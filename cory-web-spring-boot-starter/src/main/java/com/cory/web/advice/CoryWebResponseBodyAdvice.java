package com.cory.web.advice;

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.cory.context.GenericResult;
import com.cory.util.ClassUtil;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Created by Cory on 2021/2/13.
 */
@ControllerAdvice
public class CoryWebResponseBodyAdvice implements ResponseBodyAdvice {

    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {
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
        return false;
    }

    @Nullable
    @Override
    public Object beforeBodyWrite(@Nullable Object o, MethodParameter methodParameter, MediaType mediaType, Class aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        if (null == o) {
            return GenericResult.success();
        }
        if (o instanceof GenericResult) {
            return o;
        }
        return GenericResult.success(o);
    }
}
