package com.cory.web.interceptor;

import com.cory.constant.Constants;
import com.cory.constant.ErrorCode;
import com.cory.context.GenericResult;
import com.cory.web.captcha.CaptchaValidation;
import com.cory.web.config.CaptchaProperties;
import com.cory.web.config.Constant;
import com.octo.captcha.service.multitype.GenericManageableCaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class CaptchaInterceptor implements HandlerInterceptor {

	@Autowired
	private GenericManageableCaptchaService captchaService;
	@Autowired
	private CaptchaProperties captchaProperties;
	@Autowired
	private CaptchaValidation captchaValidation;

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler)
			throws ServletException {
		if (captchaEnabled() && urlMatch(request) && !captchaValidation.valid(request, captchaService)) {
			log.error("invalid captcha, uri: {}, pattern: {}", request.getRequestURI(), captchaProperties.getUrlPattern());
			responseError(response);
			return false;
		}
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
	}

	private boolean urlMatch(HttpServletRequest request) {
		String pattern = captchaProperties.getUrlPattern();
		if (StringUtils.isBlank(pattern)) {
			return false;
		}
		String uri = request.getRequestURI();
		if (!uri.startsWith("/")) {
			uri = "/" + uri;
		}
		if (uri.startsWith(Constant.CAPTCHA_URL)) {
			return false;
		}
		return uri.matches(pattern);
	}

	private boolean captchaEnabled() {
		return captchaProperties.isEnable();
	}

	private void responseError(HttpServletResponse response) {
		try {
			response.setContentType(Constants.DEFAULT_CONTENT_TYPE);
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			response.getWriter().write(GenericResult.fail(ErrorCode.CAPTCHA_ERROR).toString());
			response.getWriter().flush();
		} catch (Throwable t) {
			log.error("write response error.", t);
		}
	}
}