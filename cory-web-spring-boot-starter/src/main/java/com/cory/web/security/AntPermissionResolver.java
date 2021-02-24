package com.cory.web.security;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.PermissionResolver;

public class AntPermissionResolver implements PermissionResolver {

	public Permission resolvePermission(String permissionString) {
        return new AntPermission(permissionString);
	}

}
