package com.cory.swagger.config;

import com.cory.swagger.annotations.SwaggerApiController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * https://my.oschina.net/u/3872757/blog/1844742
 * Created by Cory on 2021/1/17.
 */
@Configuration
@ConditionalOnProperty(prefix = Constant.PREFIX, name = "enable", havingValue = "true")
@EnableConfigurationProperties(CorySwaggerProperties.class)
@EnableSwagger2
@ComponentScan(basePackages= {"com.cory.web"})
@EnableWebMvc
public class CorySwaggerAutoConfiguration {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                //apis() 控制哪些接口暴露给swagger，
                // RequestHandlerSelectors.any() 所有都暴露
                // RequestHandlerSelectors.basePackage("com.info.*")  指定包位置
                //.apis(RequestHandlerSelectors.any())
                //.apis(RequestHandlerSelectors.basePackage("com.cory.web.controller.lm.openapi"))
                .apis(RequestHandlerSelectors.withClassAnnotation(SwaggerApiController.class))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("接口文档")
                .description("接口文档")
                //联系人实体类
                .contact(new Contact("潘亮", "xxx.com", "panmissl@163.com"))
                //版本号
                .version("1.0.0-SNAPSHOT")
                .build();
    }
}
