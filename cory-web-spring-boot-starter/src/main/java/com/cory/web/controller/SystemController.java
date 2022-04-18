package com.cory.web.controller;

import com.cory.web.advice.GenericResultExcludeNoJson;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Cory on 2017/5/13.
 */
@RestController
@GenericResultExcludeNoJson
public class SystemController extends BaseController {

    public static final String OK = "OK";

    @GetMapping("/status")
    public String status() {
        return OK;
    }
}
