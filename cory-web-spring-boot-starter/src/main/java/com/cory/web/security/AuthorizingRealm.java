package com.cory.web.security;

import com.cory.model.Resource;
import com.cory.model.Role;
import com.cory.model.User;
import com.cory.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 自定义DB Realm
 * 
 */
public class AuthorizingRealm extends org.apache.shiro.realm.AuthorizingRealm {

	protected UserService userService;

	/**
	 * 登录认证
	 */
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) throws AuthenticationException {
		UsernamePasswordToken token = (UsernamePasswordToken) authcToken;
		User user = userService.findByLogonId(token.getUsername());
		if (user != null) {
			return new SimpleAuthenticationInfo(user.getLogonId(), user.getPassword(), getName());
		} else {
			return null;
		}
	}

	/**
	 * 授权
	 */
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		String logonId = (String) principals.getPrimaryPrincipal();
		User user = userService.findByLogonId(logonId);

		SimpleAuthorizationInfo auth = new SimpleAuthorizationInfo();
		if (user != null) {
			List<Role> roles = user.getRoles();
			Set<String> strRoles = new HashSet<String>();
			Set<String> strResource = new HashSet<String>();
			if (null != roles) {
				for (Role role : roles) {
					strRoles.add(role.getName());
					
					List<Resource> resources = role.getResources();
					if (null != resources) {
						for (Resource r : resources) {
							strResource.add(r.getValue());
						}
					}
				}
			}
			
			auth.setRoles(strRoles);
			auth.setStringPermissions(strResource);
		}
		return auth;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}
}
