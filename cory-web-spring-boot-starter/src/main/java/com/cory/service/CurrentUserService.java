package com.cory.service;

import com.cory.context.CorySystemContext;
import com.cory.context.CurrentUser;
import com.cory.enums.CoryEnum;
import com.cory.model.Resource;
import com.cory.model.Role;
import com.cory.model.User;
import com.cory.vo.UserVO;
import com.cory.web.security.UserUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
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

        Set<CorySystemContext.EnumMeta> enumMetaSet = new HashSet<>();
        modelMetaList.forEach(modelMeta -> modelMeta.getFieldList().forEach(fieldMeta -> {
            if (!CoryEnum.class.isAssignableFrom(fieldMeta.getJavaType())) {
                return;
            }
            Object[] arr = fieldMeta.getJavaType().getEnumConstants();
            Map<String, Pair<String, Integer>> valueLabelOrderMap = new HashMap<>();
            if (null != arr && arr.length > 0) {
                try {
                    Method nameMethod = fieldMeta.getJavaType().getMethod("name");
                    Method textMethod = fieldMeta.getJavaType().getMethod("text");
                    Method orderMethod = fieldMeta.getJavaType().getMethod("order");
                    for (Object o : arr) {
                        String name = (String) nameMethod.invoke(o);
                        String text = (String) textMethod.invoke(o);
                        Integer order = (Integer) orderMethod.invoke(o);
                        valueLabelOrderMap.put(name, Pair.of(text, order));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }

            enumMetaSet.add(CorySystemContext.EnumMeta.builder().className(fieldMeta.getJavaType().getName()).valueLabelOrderMap(valueLabelOrderMap).build());
        }));

        return UserVO.builder()
                .id(user.getId())
                .logonId(user.getLogonId())
                .avatar("https://gw.alipayobjects.com/zos/antfincdn/XAosXuNZyF/BiazfanxmamNRoxxVxka.png")
                .modelMetaList(modelMetaList)
                .enumMetaSet(enumMetaSet)
                .resources(resources)
                .build();
    }
}
