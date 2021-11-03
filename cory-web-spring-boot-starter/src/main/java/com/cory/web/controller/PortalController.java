package com.cory.web.controller;

import com.alibaba.fastjson.JSON;
import com.cory.service.CurrentUserService;
import com.cory.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by Cory on 2017/5/13.
 */
@Controller
public class PortalController extends BaseController {

    private static final String INDEX = "index";

    @Autowired
    private CurrentUserService currentUserService;

    @GetMapping({"/", "/admin", "/**"})
    public String index(Model model) {
        return initContext(model);
    }

    @GetMapping("/errorPage")
    public String errorPage(Model model, @RequestParam(required = false) String type) {
        String page = initContext(model);

        model.addAttribute("errorType", type);
        model.addAttribute("errorPage", "true");

        return page;
    }

    protected String initContext(Model model) {
        UserVO userVO = currentUserService.getCurrentUserVO();
        if (null != userVO) {
            model.addAttribute("user", JSON.toJSONString(userVO));
        }
        return INDEX;
    }
}
