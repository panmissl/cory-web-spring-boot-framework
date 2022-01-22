package com.cory.web.controller;

import com.alibaba.fastjson.JSON;
import com.cory.service.CurrentUserService;
import com.cory.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

/**
 * Created by Cory on 2017/5/14.
 */
@Controller
@Slf4j
public abstract class BasePortalController extends BaseController {

    private static final String INDEX = "index";

    @Autowired
    protected CurrentUserService currentUserService;

    protected String initPortalPageContext(Model model) {
        UserVO userVO = currentUserService.getCurrentUserVO();
        if (null != userVO) {
            model.addAttribute("user", JSON.toJSONString(userVO));
        }
        return INDEX;
    }
}
