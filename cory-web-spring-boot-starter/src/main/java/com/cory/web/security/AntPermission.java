package com.cory.web.security;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.util.AntPathMatcher;
import org.apache.shiro.util.PatternMatcher;

public class AntPermission implements Permission {
	
	private PatternMatcher pathMatcher = new AntPathMatcher();
	private String permissionString;

	public AntPermission(String permissionString) {
		this.permissionString = permissionString;
	}

	@Override
	public boolean implies(Permission p) {
		if (!(p instanceof AntPermission)) {
			return false;
        }
		return pathMatcher.matches(permissionString, ((AntPermission) p).permissionString);
	}

}
