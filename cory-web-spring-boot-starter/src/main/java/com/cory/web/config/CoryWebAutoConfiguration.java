package com.cory.web.config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.cory.constant.Constants;
import com.cory.web.interceptor.AccessTokenInterceptor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.MappedInterceptor;

import java.nio.charset.Charset;
import java.util.List;

/**
 * 使用前，在application.properties文件里配置数据库信息：spring.datasource.username、spring.datasource.password、spring.datasource.name
 * <br />
 * Created by Cory on 2021/2/9.
 */
@Configuration
@EnableConfigurationProperties(CoryWebProperties.class)
@EnableRedisHttpSession
public class CoryWebAutoConfiguration implements WebMvcConfigurer {

    @Autowired
    private List<HandlerInterceptor> interceptorList;
    @Autowired
    private AccessTokenInterceptor accessTokenInterceptor;

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

        converters.add(converter);
    }

    @Bean
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setMaxUploadSize(90097152);
        return resolver;
    }

    @Bean
    @ConditionalOnMissingBean
    public LettuceConnectionFactory connectionFactory() {
        return new LettuceConnectionFactory();
    }

}
