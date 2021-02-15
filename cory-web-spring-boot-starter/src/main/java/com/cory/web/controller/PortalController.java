package com.cory.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Created by Cory on 2017/5/13.
 */
@Controller
public class PortalController {

    @GetMapping("/")
    public String index() {
        return "index.vm";
    }
}
