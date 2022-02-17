package com.cory.web.interceptor;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.cory.constant.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
@Order(100)
public class AccessLogInterceptor implements HandlerInterceptor {

	private static final TransmittableThreadLocal<Long> TIME = new TransmittableThreadLocal<>();

	/** Y/N|status|method|uri|duration(ms)|remoteAddr|realIp|queryString */
	public static final String FORMAT = "%s|%s|%s|%s|%sms|%s|%s|%s";

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler)
			throws ServletException {
		TIME.set(System.currentTimeMillis());
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
		Object exAttr = request.getAttribute(Constants.EXCEPTION_ATTR);
		boolean hasException = null != exAttr && (Boolean) exAttr;
		String method = request.getMethod();
		String realIp = request.getHeader(Constants.REQUEST_HEADER_KEY_REAL_IP);
		String remoteAddr = request.getRemoteAddr();
		int status = response.getStatus();
		String uri = request.getRequestURI();
		long start = TIME.get();
		long duration = System.currentTimeMillis() - start;
		String query = request.getQueryString();

		log.info(String.format(FORMAT, !hasException, status, method, uri, duration, remoteAddr, realIp, query));
	}
}