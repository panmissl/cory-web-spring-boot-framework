package com.cory.util;

import com.cory.constant.ErrorCode;
import com.cory.exception.CoryException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ognl.*;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Cory on 2021/2/12.
 */
public class OgnlUtil {

    private static final MemberAccess MEMBER_ACCESS = new AbstractMemberAccess() {
        @Override
        public boolean isAccessible(Map context, Object target, Member member, String propertyName) {
            int modifiers = member.getModifiers();
            return Modifier.isPublic(modifiers);
        }
    };

    public static Object get(Object object, String expression) {
        try {
            Map context = Ognl.createDefaultContext(object, MEMBER_ACCESS);
            return Ognl.getValue(expression, context, object);
        } catch (OgnlException e) {
            throw new CoryException(ErrorCode.GENERIC_ERROR, e.getMessage());
        }
    }

    public static void main(String[] args) {
        Resource r1 = new Resource("c1", "n1");
        Resource r2 = new Resource("c2", "n2");
        Role r = new Role("role1", "角色", r1);
        User user = new User();
        user.setAge(15);
        user.setName("zhangsan");
        user.setRole(r);
        user.setResourceList(Arrays.asList(r1, r2));

        Map<String, Object> map = new HashMap<>();
        map.put("user", user);
        map.put("ex", "ex 123");

        System.out.println(get(map, "user.role.r.name"));
        System.out.println(get(map, "ex"));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class User {
        private int age;
        private String name;
        private Role role;
        private List<Resource> resourceList;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class Role {
        private String code;
        private String name;

        private Resource r;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class Resource {
        private String code;
        private String name;
    }
}
