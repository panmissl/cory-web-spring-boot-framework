package com.cory.web.interceptor;

import com.alibaba.fastjson.JSON;
import com.cory.context.GenericResult;
import com.cory.web.config.Constant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;

@Component
@Order(130)
public class FormTokenInterceptor implements HandlerInterceptor {

	private static final String GENERATE_FORM_TOKEN_URL = "generateFormToken";

	@Autowired
	private RequestMatcher requestMatcher;
	private AccessDeniedHandler accessDeniedHandler = new AccessDeniedHandlerImpl();

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler)
			throws ServletException {
		HttpSession session = request.getSession(false);
		try {
			if (requestMatcher.matches(request)) {
				String serverToken = (String) session.getAttribute(Constant.FORM_TOKEN);
				String clientToken = request.getParameter(Constant.FORM_TOKEN);

				if (StringUtils.isEmpty(serverToken) || StringUtils.isEmpty(clientToken)) {
					accessDeniedHandler.handle(request, response, new AccessDeniedException("Missing Form Token."));
				} else if (!StringUtils.equals(serverToken, clientToken)) {
					accessDeniedHandler.handle(request, response, new AccessDeniedException("请勿重复提交."));
				} else {
					session.removeAttribute(Constant.FORM_TOKEN);
					return true;
				}
				return false;
			} else if (isGenerateFormTokenUrl(request)) {
				String token = UUID.randomUUID().toString();
				session.setAttribute(Constant.FORM_TOKEN, token);
				response.getWriter().write(JSON.toJSONString(GenericResult.success(token)));
				return false;
			} else {
				return true;
			}
		} catch (IOException e) {
			return false;
		}
	}

	private boolean isGenerateFormTokenUrl(HttpServletRequest request) {
		if (request.getRequestURI().endsWith(GENERATE_FORM_TOKEN_URL)) {
			return true;
		}
		return false;
	}
}