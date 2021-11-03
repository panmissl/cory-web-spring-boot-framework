package com.cory.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by Cory on 2017/5/13.
 */
@Controller
public class PortalController extends BasePortalController {

    @GetMapping({"/", "/admin", "/**"})
    public String index(Model model) {
        return initPortalPageContext(model);
    }

    @GetMapping("/errorPage")
    public String errorPage(Model model, @RequestParam(required = false) String type) {
        String page = initPortalPageContext(model);

        model.addAttribute("errorType", type);
        model.addAttribute("errorPage", "true");

        return page;
    }
}
