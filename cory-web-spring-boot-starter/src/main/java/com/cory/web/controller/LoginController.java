package com.cory.web.controller;

import com.cory.constant.ErrorCode;
import com.cory.service.CurrentUserService;
import com.cory.service.UserService;
import com.cory.util.AssertUtils;
import com.cory.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Cory on 2017/5/13.
 */
@RestController
public class LoginController extends BaseController {

    @Autowired
    private UserService userService;
    @Autowired
    private CurrentUserService currentUserService;

    /* 会访问到PortalController里的/**，不用单独定义
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("loginPage", "true");
        return "index";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("registerPage", "true");
        return "index";
    }
    */

    @PostMapping("/doRegister")
    public boolean doRegister(HttpServletRequest request, String phone, String password, String passwordConfirm, String captcha) {
        //这里是验证手机验证码，不是普通验证码
        //验证码由Filter统一验证，不用专门验证了
        //AssertUtils.isTrue(captchaValidation.valid(request, captchaService), "验证码输入错误", ErrorCode.LOGIN_ERROR);
        AssertUtils.isTrue(password.equals(passwordConfirm), "两次输入密码不一致", ErrorCode.LOGIN_ERROR);

        userService.register(phone, password);
        return true;
    }

    @GetMapping("/currentUser")
    public UserVO currentUser() {
        return currentUserService.getCurrentUserVO();
    }

}
