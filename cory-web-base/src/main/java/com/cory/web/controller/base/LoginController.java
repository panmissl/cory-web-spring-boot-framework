package com.cory.web.controller.base;

import com.cory.constant.ErrorCode;
import com.cory.context.CurrentUser;
import com.cory.sevice.base.UserService;
import com.cory.util.AssertUtils;
import com.cory.web.captcha.CaptchaValidation;
import com.cory.web.controller.BaseController;
import com.octo.captcha.service.multitype.GenericManageableCaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Cory on 2017/5/13.
 */
@Controller
public class LoginController extends BaseController {

    @Autowired
    private GenericManageableCaptchaService captchaService;
    @Autowired
    private UserService userService;

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
    @ResponseBody
    public boolean doRegister(HttpServletRequest request, String phone, String password, String passwordConfirm, String captcha) {
        //这里是验证手机验证码，不是普通验证码
        AssertUtils.isTrue(CaptchaValidation.valid(request, captchaService), "验证码输入错误", ErrorCode.LOGIN_ERROR);
        AssertUtils.isTrue(password.equals(passwordConfirm), "两次输入密码不一致", ErrorCode.LOGIN_ERROR);

        userService.register(phone, password);
        return true;
    }

    @GetMapping("/currentUser")
    @ResponseBody
    public CurrentUser currentUser() {
        return CurrentUser.get();
    }

}
