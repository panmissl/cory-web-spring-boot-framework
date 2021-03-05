package com.cory.web.controller.base;

import com.cory.context.CurrentUser;
import com.cory.model.User;
import com.cory.model.UserRoleRel;
import com.cory.service.UserRoleRelService;
import com.cory.service.UserService;
import com.cory.web.controller.BaseAjaxController;
import com.cory.web.security.ShiroCacheUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * generated by CodeGenerator on 2017/5/10.
 */
@Controller
@RequestMapping("/ajax/base/user/")
public class UserController extends BaseAjaxController<User> {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRoleRelService userRoleRelService;

    @Override
    public int save(User entity) {
        int r = super.save(entity);
        ShiroCacheUtils.clearAllCache();
        return r;
    }

    @Override
    public boolean delete(int id) {
        boolean r= super.delete(id);
        ShiroCacheUtils.clearAllCache();
        return r;
    }

    @RequestMapping("doAssign")
    public boolean doAssign(UserRoleRel userRoleRel) {
        userRoleRel.setCreator(CurrentUser.get().getId());
        userRoleRel.setModifier(CurrentUser.get().getId());
        userRoleRelService.assign(userRoleRel);
        ShiroCacheUtils.clearAllCache();
        return true;
    }

    /**
     * 直接从列表页面修改用户密码
     * @param userId
     * @param newPassword
     * @return
     */
    @RequestMapping("changePasswordDirectly")
    public boolean changePassword(Integer userId, String newPassword) {
        this.getService().changePasswordDirectly(userId, newPassword);
        ShiroCacheUtils.clearCache(SecurityUtils.getSubject().getPrincipal().toString());
        return true;
    }

    @RequestMapping(value = "changePassword", method = RequestMethod.POST)
    public boolean changePassword2(String password, String newPassword, String passwordConfirm) {
        this.getService().checkAndChangePassword(password, newPassword, passwordConfirm);
        ShiroCacheUtils.clearCache(SecurityUtils.getSubject().getPrincipal().toString());
        return true;
    }

    public UserService getService() {
        return userService;
    }

}