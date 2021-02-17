package com.cory.web.controller.base;

import com.cory.context.CurrentUser;
import com.cory.model.base.Role;
import com.cory.model.base.RoleResourceRel;
import com.cory.sevice.base.RoleResourceRelService;
import com.cory.sevice.base.RoleService;
import com.cory.vo.base.RoleResourceRelCt;
import com.cory.web.controller.BaseAjaxController;
import com.cory.web.security.ShiroCacheUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * generated by CodeGenerator on 2017/5/10.
 */
@Controller
@RequestMapping("/ajax/base/role/")
public class RoleController extends BaseAjaxController<Role> {

    @Autowired
    private RoleService roleService;
    @Autowired
    private RoleResourceRelService roleResourceRelService;

    @Override
    public boolean delete(int id) {
        boolean r = super.delete(id);
        ShiroCacheUtils.clearAllCache();
        return r;
    }

    @RequestMapping("doAssign")
    public boolean doAssign(RoleResourceRelCt roleResourceRelCt) {
        List<RoleResourceRel> roleResourceRelList = roleResourceRelCt.getRoleResourceRelList();
        if (!CollectionUtils.isEmpty(roleResourceRelList)) {
            for (RoleResourceRel roleResourceRel : roleResourceRelList) {
                roleResourceRel.setCreator(CurrentUser.get().getId());
                roleResourceRel.setModifier(CurrentUser.get().getId());
            }
        }
        roleResourceRelService.assign(roleResourceRelList);
        ShiroCacheUtils.clearAllCache();
        return true;
    }

    public RoleService getService() {
        return roleService;
    }
}
