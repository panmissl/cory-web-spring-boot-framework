package com.cory.web.security;

import com.alibaba.fastjson.JSON;
import com.cory.constant.Constants;
import com.cory.constant.ErrorCode;
import com.cory.context.CurrentUser;
import com.cory.context.GenericResult;
import com.cory.model.User;
import com.cory.service.UserService;
import com.cory.util.systemconfigcache.SystemConfigCacheKey;
import com.cory.util.systemconfigcache.SystemConfigCacheUtil;
import com.cory.web.util.CookieUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AuthenticationFilter自定义登录认证filter
 */
public class AuthenticationFilter extends FormAuthenticationFilter {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

	public static final String COOKIE_ERROR_REMAINING = "_error_remaining";

	/** 允许最大重试次数：3次 */
	public static final int MAX_ERROR_TIMES = 3;
	/** 错误存在最长时间：5分钟 */
	public static final int ERROR_INTERVAL = 5 * 60;

	private static final long MILLIS_PER_MINUTE = 60 * 1000;
	private static final String SUCCESS_URL_SIMPLE_PREFIX = "SIMPLE:";
	private static final String SUCCESS_URL_ROLE_PREFIX = "ROLE:";
	private static final SuccessUrl SUCCESS_URL = new SuccessUrl();

	@Value("${cory.shiro.success-url}")
	private String successUrl;

	@Autowired
	private UserService userService;

	//登录处理url
	private String loginHandleUrl;

	/**
	 * 登录处理，这里是公用的，因为还有一个ajaxLogin
	 *
	 * @param request
	 * @param response
	 * @param token
	 * @throws Exception
	 */
	public void processLogin(HttpServletRequest request, HttpServletResponse response, AuthenticationToken token) throws Exception {
		String username = (String) token.getPrincipal();
		/* CaptchaFilter处理了
		//先验证码校验,防止一直查询数据库
		if (isCaptchaRequired(username, request, response) && !captchaValidation.valid(request, captchaService)) {
			throw new CaptchaRequiredException();
		}
		*/
		//do process -- 登录
		if (isDisabled(username)) {
			throw new DisabledAccountException();
		}
		try {
			Subject subject = getSubject(request, response);
			subject.login(token);
		} catch (AuthenticationException e) {
			throw e;
		}
	}

	@Override
	protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
		AuthenticationToken token = createToken(request, response);
		if (token == null) {
			String msg = "create AuthenticationToken error";
			return this.onLoginFailure(token, new AuthenticationException(msg), request, response);
		}
		try {
			processLogin((HttpServletRequest) request, (HttpServletResponse) response, token);
			Subject subject = getSubject(request, response);
			return onLoginSuccess(token, subject, request, response);
		} catch (Exception e) {
			return this.onLoginFailure(token, new AuthenticationException(e.getMessage()), request, response);
		}
	}

	@Override
	public boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
		boolean isAllowed = isAccessAllowed(request, response, mappedValue);
		//已经登录的，并且是登录请求，直接返回成功，不要再登录了
		if (isAllowed && pathsMatch(getLoginHandleUrl(), request) && this.isLoginSubmission(request, response)) {
			issueSuccessRedirect(request, response);
			return false;
		}
		// 登录跳转
		if (isAllowed && isLoginRequest(request, response)) {
			try {
				issueSuccessRedirect(request, response);
			} catch (Exception e) {
				logger.error("", e);
			}
			return false;
		}
		return isAllowed || onAccessDenied(request, response, mappedValue);
	}

	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		if (pathsMatch(getLoginHandleUrl(), request) && this.isLoginSubmission(request, response)) {
			return this.executeLogin(request, response);
		}
		return super.onAccessDenied(request, response);
	}

	@Override
	protected void issueSuccessRedirect(ServletRequest request, ServletResponse response) throws Exception {
		/*
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String successUrl = req.getParameter(Constants.RETURN_URL);
		if (StringUtils.isBlank(successUrl)) {
			successUrl = getSuccessUrl();
		}
		WebUtils.redirectToSavedRequest(req, res, successUrl);
		*/
		//不跳转、直接返回登录成功
		writeResponse(response, GenericResult.success(true));
	}

	@Override
	protected boolean isLoginRequest(ServletRequest req, ServletResponse resp) {
		return pathsMatch(getLoginUrl(), req);
	}

	/**
	 * 登录成功
	 */
	@Override
	protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response) throws Exception {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		//清除需要验证码cookie
		removeCookieErrorRemaining(req, res);

		String principal = token.getPrincipal().toString();
		User user = userService.findByUserName(principal);
		CurrentUser currentUser = convert2UserVO(principal, user);
		CurrentUser.set(currentUser);
		((HttpServletRequest) request).getSession(true).setAttribute(Constants.CURRENT_USER, currentUser);
		updateLastLogonInfo(principal, req, true);

		int sessionTimeoutInMinute = SystemConfigCacheUtil.getIntCache(SystemConfigCacheKey.SESSION_TIMEOUT_IN_MINUTE, 30);
		SecurityUtils.getSubject().getSession().setTimeout(sessionTimeoutInMinute * MILLIS_PER_MINUTE);

		return super.onLoginSuccess(token, subject, request, response);
	}

	private void updateLastLogonInfo(String userName, HttpServletRequest request, boolean success) {
		User user = userService.findByUserName(userName);
		if (null == user) {
			return;
		}

		String realIp = request.getHeader(Constants.REQUEST_HEADER_KEY_REAL_IP);
		if (StringUtils.isBlank(realIp)) {
			realIp = request.getRemoteAddr();
		}
		userService.updateLastLogonInfo(user.getId(), realIp, success, new Date());
	}

	private CurrentUser convert2UserVO(String principle, User user) {
		return CurrentUser.builder()
				.principal(principle)
				.id(user.getId())
				.isAdmin(UserUtils.isAdmin())
				.isRoot(UserUtils.isRoot())
				.roles(CollectionUtils.isEmpty(user.getRoles()) ? null : user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toList()))
				.build();
	}

	@Override
	protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		writeCookieErrorRemaining(req, res);
		updateLastLogonInfo(token.getPrincipal().toString(), req, false);

		//return super.onLoginFailure(token, e, request, response);
		//不跳转、直接返回登录失败
		writeResponse(response, GenericResult.fail(ErrorCode.LOGIN_ERROR));
		return false;
	}

	private boolean isCaptchaRequired(String username, HttpServletRequest request, HttpServletResponse response) {
		/*
		//应该是如果登录错误次数大于3次，那么就需要输入
		String captcha = request.getParameter(CaptchaValidation.CAPTCHA_PARAM);
		int errorRemaining = getCookieErrorRemaining(request, response);
		// 如果输入了验证码，那么必须验证；如果没有输入验证码，则根据当前用户判断是否需要验证码。
		if (!StringUtils.isBlank(captcha) || errorRemaining < 0) {
			return true;
		}
		return false;
		*/
		//已经由Filter来检查了，不用检查了
		return false;
	}

	// 用户禁用返回true 未查找到用户或者非禁用返回false
	private boolean isDisabled(String userName) {
		User user = userService.findByUserName(userName);
		if (user != null) {
			if (user.isDisabled()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private void writeCookieErrorRemaining(HttpServletRequest request, HttpServletResponse response) {
		// 所有访问的页面都需要写一个cookie，这样可以判断已经登录了几次。
		int errorRemaining = getCookieErrorRemaining(request, response);
		if (errorRemaining <= 0) {
			errorRemaining = 0;
		} else {
			errorRemaining --;
		}
		CookieUtils.addCookie(request, response, COOKIE_ERROR_REMAINING, errorRemaining + "", ERROR_INTERVAL, null);
	}

	private void removeCookieErrorRemaining(HttpServletRequest request, HttpServletResponse response) {
		CookieUtils.cancleCookie(request, response, COOKIE_ERROR_REMAINING, null);
	}

	private Integer getCookieErrorRemaining(HttpServletRequest request, HttpServletResponse response) {
		Cookie cookie = CookieUtils.getCookie(request, COOKIE_ERROR_REMAINING);
		if (cookie != null) {
			String value = cookie.getValue();
			if (NumberUtils.isDigits(value)) {
				return Integer.parseInt(value);
			}
		}
		return MAX_ERROR_TIMES;
	}

	private void writeResponse(ServletResponse response, GenericResult result) {
		try {
			response.setContentType(Constants.DEFAULT_CONTENT_TYPE);
			PrintWriter writer = response.getWriter();
			writer.write(JSON.toJSONString(result));
			writer.flush();
			writer.close();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	public String getLoginHandleUrl() {
		return loginHandleUrl;
	}

	public void setLoginHandleUrl(String loginHandleUrl) {
		this.loginHandleUrl = loginHandleUrl;
	}

	@Override
	public String getSuccessUrl() {
		if (SUCCESS_URL.isSimpleType) {
			return SUCCESS_URL.simpleSuccessUrl;
		}
		if (SUCCESS_URL.isRoleType) {
			return SUCCESS_URL.getRoleSuccessUrl();
		}
		return super.getSuccessUrl();
	}

	@PostConstruct
	public void init() {
		initSuccessUrl();
	}

	private void initSuccessUrl() {
		//SIMPLE:/, SIMPLE:/admin, ROLE:roleName1=/admin,roleName2=/test,roleName3=/haha,/defaultPage
		if (StringUtils.isBlank(successUrl)) {
			return;
		}
		if (successUrl.startsWith(SUCCESS_URL_SIMPLE_PREFIX)) {
			SUCCESS_URL.isSimpleType = true;
			SUCCESS_URL.simpleSuccessUrl = successUrl.substring(SUCCESS_URL_SIMPLE_PREFIX.length());
			return;
		}
		if (successUrl.startsWith(SUCCESS_URL_ROLE_PREFIX)) {
			String[] arr = successUrl.substring(SUCCESS_URL_ROLE_PREFIX.length()).split(",");
			for (String item : arr) {
				if (item.contains("=")) {
					String[] itemArr = item.split("=");
					SUCCESS_URL.roleMap.put(itemArr[0].trim(), itemArr[1].trim());
				} else {
					SUCCESS_URL.roleDefaultSuccessUrl = item.trim();
				}
			}
		}
	}

	private static class SuccessUrl {

		public boolean isSimpleType = false;
		public boolean isRoleType = false;

		public String simpleSuccessUrl;

		//key: roleName, value: successUrl
		public Map<String, String> roleMap = new HashMap<>();
		public String roleDefaultSuccessUrl;

		public String getRoleSuccessUrl() {
			//ROLE:roleName1=/admin,roleName2=/test,roleName3=/haha,/defaultPage
			CurrentUser user = CurrentUser.get();
			if (null == user || CollectionUtils.isEmpty(user.getRoles())) {
				return null;
			}
			String url = roleMap.get(user.getRoles().get(0));
			if (StringUtils.isNotBlank(url)) {
				return url;
			}
			return roleDefaultSuccessUrl;
		}
	}
}
