package com.cory.web.interceptor;

import com.alibaba.fastjson.JSON;
import com.cory.constant.Constants;
import com.cory.context.GenericResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(110)
public class CsrfTokenGeneratorInterceptor implements HandlerInterceptor {

	private static final String GENERATE_CSRF_TOKEN_URL = "generateCsrfToken";

	@Autowired
	private HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository;

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler)
			throws ServletException {
		try {
			if (isGenerateCsrfTokenUrl(request)) {
				CsrfToken csrfToken = httpSessionCsrfTokenRepository.generateToken(request);
				httpSessionCsrfTokenRepository.saveToken(csrfToken, request, response);

				String token = csrfToken.getToken();
				response.setContentType(Constants.DEFAULT_CONTENT_TYPE);
				response.getWriter().write(JSON.toJSONString(GenericResult.success(token)));
				return false;
			} else {
				return true;
			}
		} catch (IOException e) {
			return false;
		}
	}

	private boolean isGenerateCsrfTokenUrl(HttpServletRequest request) {
		if (request.getRequestURI().endsWith(GENERATE_CSRF_TOKEN_URL)) {
			return true;
		}
		return false;
	}

	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
	}
}