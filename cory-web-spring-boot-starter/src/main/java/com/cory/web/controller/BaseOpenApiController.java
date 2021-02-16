package com.cory.web.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Cory on 2017/5/14.
 */
@RestController
@RequestMapping("/openapi")
public abstract class BaseOpenApiController extends BaseController {}
