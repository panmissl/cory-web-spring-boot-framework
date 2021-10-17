package com.cory.web.config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.cory.constant.Constants;
import com.cory.web.eagleeye.EagleEyeFilter;
import com.cory.web.interceptor.AccessTokenInterceptor;
import com.cory.web.util.PostRequestMatcher;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.Ordered;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.MappedInterceptor;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by Cory on 2021/2/9.
 */
@Configuration
@EnableConfigurationProperties(CoryWebProperties.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class CoryWebAutoConfiguration implements WebMvcConfigurer {

    @Autowired
    private List<HandlerInterceptor> interceptorList;
    @Autowired
    private AccessTokenInterceptor accessTokenInterceptor;

    @Bean
    public FilterRegistrationBean eagleEyeFilter() {
        FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(new EagleEyeFilter());
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    @Bean
    public HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository() {
        return new HttpSessionCsrfTokenRepository();
    }

    @Bean
    public CsrfFilter csrfFilter(HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository, RequestMatcher requestMatcher) {
        CsrfFilter filter = new CsrfFilter(httpSessionCsrfTokenRepository);
        filter.setRequireCsrfProtectionMatcher(requestMatcher);
        return filter;
    }

    @Bean
    public RequestMatcher requestMatcher() {
        return new PostRequestMatcher();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (CollectionUtils.isNotEmpty(interceptorList)) {
            for (HandlerInterceptor interceptor : interceptorList) {
                //AccessTokenInterceptor 单独添加
                if (!(interceptor instanceof AccessTokenInterceptor)) {
                    registry.addInterceptor(interceptor);
                }
            }
        }
        registry.addInterceptor(new MappedInterceptor(new String[] {"/openapi/**"}, accessTokenInterceptor));
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        FastJsonConfig config = new FastJsonConfig();
        config.setCharset(Charset.forName(Constants.UTF8));
        config.setDateFormat(Constants.DATE_FORMAT_FULL_WITH_DASH);
        config.setSerializerFeatures(SerializerFeature.DisableCircularReferenceDetect);

        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
        converter.setFastJsonConfig(config);

        converters.add(0, converter);
    }

}
