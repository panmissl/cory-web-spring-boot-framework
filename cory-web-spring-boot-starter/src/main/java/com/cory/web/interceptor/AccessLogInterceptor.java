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

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler)
			throws ServletException {
		TIME.set(System.currentTimeMillis());
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
		long start = TIME.get();
		Object exAttr = request.getAttribute(Constants.EXCEPTION_ATTR);
		boolean hasException = null != exAttr && (Boolean) exAttr;
		String method = request.getMethod();

		log.info("remoteAddr={}, uri={}, method={}, queryString={}, hasException={}, duration={}ms",
				request.getRemoteAddr(), request.getRequestURI(), method, request.getQueryString(), hasException, System.currentTimeMillis() - start);
	}
}