package com.cory.web.filter;

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
import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class CaptchaFilter implements OrderedFilter {

	@Autowired
	private GenericManageableCaptchaService captchaService;
	@Autowired
	private CaptchaProperties captchaProperties;
	@Autowired
	private CaptchaValidation captchaValidation;

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		if (servletRequest instanceof HttpServletRequest && servletResponse instanceof HttpServletResponse) {
			HttpServletRequest request = (HttpServletRequest) servletRequest;
			HttpServletResponse response = (HttpServletResponse) servletResponse;

			if (captchaEnabled() && urlMatch(request) && !captchaValidation.valid(request, captchaService)) {
				log.error("invalid captcha, uri: {}, pattern: {}", request.getRequestURI(), captchaProperties.getUrlPattern());
				responseError(response);
				return;
			}
			filterChain.doFilter(servletRequest, servletResponse);
		} else {
			filterChain.doFilter(servletRequest, servletResponse);
		}
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

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 100;
	}
}