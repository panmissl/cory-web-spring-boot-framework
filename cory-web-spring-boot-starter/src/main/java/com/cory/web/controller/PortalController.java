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

    @Autowired
    private CurrentUserService currentUserService;

    @GetMapping("/**")
    public String index(Model model) {
        initContext(model);
        return "index";
    }

    @GetMapping("/errorPage")
    public String errorPage(Model model, @RequestParam(required = false) String type) {
        initContext(model);

        model.addAttribute("errorType", type);
        model.addAttribute("errorPage", "true");
        return "index";
    }

    private void initContext(Model model) {
        UserVO userVO = currentUserService.getCurrentUserVO();
        if (null == userVO) {
            return;
        }
        model.addAttribute("user", JSON.toJSONString(userVO));
    }
}
