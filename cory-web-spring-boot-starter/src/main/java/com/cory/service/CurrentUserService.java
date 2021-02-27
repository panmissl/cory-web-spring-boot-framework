package com.cory.service;

import com.cory.context.CorySystemContext;
import com.cory.context.CurrentUser;
import com.cory.model.Resource;
import com.cory.model.Role;
import com.cory.model.User;
import com.cory.vo.UserVO;
import com.cory.web.security.UserUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by Cory on 2021/2/28.
 */
@Component
public class CurrentUserService {

    @Autowired
    private UserService userService;

    public UserVO getCurrentUserVO() {
        CurrentUser currentUser = CurrentUser.get();
        if (null == currentUser || currentUser.getId() == 0) {
            return null;
        }
        User user = userService.findByLogonId(currentUser.getPrincipal());
        if (null == user) {
            return null;
        }

        Set<String> resources = new HashSet<>();

        List<Role> rolesList = user.getRoles();
        if (CollectionUtils.isNotEmpty(rolesList)) {
            rolesList.forEach(role -> {
                List<Resource> resourceList = role.getResources();
                if (CollectionUtils.isNotEmpty(resourceList)) {
                    resourceList.forEach(r -> resources.add(r.getValue()));
                }
            });
        }

        List<CorySystemContext.ModelMeta> modelMetaList = new ArrayList<>();
        Map<String, CorySystemContext.ModelMeta> map = CorySystemContext.get().getModelMetaMap();
        map.entrySet().forEach(entry -> {
            if (UserUtils.canAccess(entry.getKey())) {
                modelMetaList.add(entry.getValue());
            }
        });

        return UserVO.builder()
                .id(user.getId())
                .logonId(user.getLogonId())
                .avatar("https://gw.alipayobjects.com/zos/antfincdn/XAosXuNZyF/BiazfanxmamNRoxxVxka.png")
                .modelMetaList(modelMetaList)
                .resources(resources)
                .build();
    }
}
