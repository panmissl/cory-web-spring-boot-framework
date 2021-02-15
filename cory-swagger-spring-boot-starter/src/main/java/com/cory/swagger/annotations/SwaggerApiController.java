package com.cory.swagger.annotations;

import java.lang.annotation.*;

/**
 * Created by Cory on 2021/1/17.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface SwaggerApiController {
}
