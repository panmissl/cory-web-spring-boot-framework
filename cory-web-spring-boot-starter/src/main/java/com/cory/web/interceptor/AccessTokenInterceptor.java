package com.cory.web.interceptor;

import com.cory.constant.Constants;
import com.cory.constant.ErrorCode;
import com.cory.context.GenericResult;
import com.cory.web.config.CoryWebProperties;
import com.cory.web.util.AccessTokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class AccessTokenInterceptor implements HandlerInterceptor {

	@Autowired
	private CoryWebProperties coryWebProperties;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ServletException {
		String ak = request.getHeader(AccessTokenUtils.ACCESS_KEY_ID);
		String ts = request.getHeader(AccessTokenUtils.ACCESS_KEY_TIMESTAMP);
		String token = request.getHeader(AccessTokenUtils.ACCESS_KEY_TOKEN);

		try {
			if (skipAuth(request.getRequestURI())) {
				return true;
			}
			if (AccessTokenUtils.checkToken(ak, ts, token)) {
				return true;
			}
			response401(response);
			return false;
		} catch (Throwable t) {
			log.error("access token validate error.", t);
			response401(response);
			return false;
		}
	}

	private boolean skipAuth(String uri) {
		String skipPattern = coryWebProperties.getSkipOpenApiAuthUrlPattern();
		if (StringUtils.isBlank(skipPattern)) {
			return false;
		}
		if (StringUtils.isBlank(uri)) {
			return false;
		}
		if (!uri.startsWith("/")) {
			uri = "/" + uri;
		}
		return uri.matches(skipPattern);
	}

	private void response401(HttpServletResponse response) {
		try {
			response.setContentType(Constants.DEFAULT_CONTENT_TYPE);
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			response.getWriter().write(GenericResult.fail(ErrorCode.AUTH_ERROR).toString());
			response.getWriter().flush();
		} catch (Throwable t) {
			log.error("write response error.", t);
		}
	}
}