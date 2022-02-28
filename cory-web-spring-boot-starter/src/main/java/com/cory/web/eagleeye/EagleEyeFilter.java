package com.cory.web.eagleeye;

import com.cory.eagleeye.EagleEye;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

import static com.cory.eagleeye.EagleEye.EAGLE_EYE_ID;

public class EagleEyeFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		String eagleEyeId = null;
		if (request instanceof HttpServletRequest) {
			eagleEyeId = ((HttpServletRequest) request).getHeader(EAGLE_EYE_ID);
		}
		if (StringUtils.isBlank(eagleEyeId)) {
			eagleEyeId = UUID.randomUUID().toString().replaceAll("-", "");
		}
		EagleEye.get().setEagleEyeId(eagleEyeId);
		MDC.put(EAGLE_EYE_ID, EagleEye.get().getEagleEyeId());
		try {
			chain.doFilter(request, response);
		} finally {
			EagleEye.remove();
			MDC.remove(EAGLE_EYE_ID);
		}
	}
}
