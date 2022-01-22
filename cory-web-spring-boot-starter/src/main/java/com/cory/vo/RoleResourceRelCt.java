package com.cory.vo;

import com.cory.model.RoleResourceRel;

import java.io.Serializable;
import java.util.List;

/**
 * spring不能直接传递list，用一个vo封装一下
 * Created by Cory on 2017/5/22.
 */
public class RoleResourceRelCt implements Serializable {

    private List<RoleResourceRel> roleResourceRelList;

    public List<RoleResourceRel> getRoleResourceRelList() {
        return roleResourceRelList;
    }

    public void setRoleResourceRelList(List<RoleResourceRel> roleResourceRelList) {
        this.roleResourceRelList = roleResourceRelList;
    }
}
