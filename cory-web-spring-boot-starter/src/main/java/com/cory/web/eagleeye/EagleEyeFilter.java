package com.cory.web.eagleeye;

import org.slf4j.MDC;

import javax.servlet.*;
import java.io.IOException;
import java.util.UUID;

public class EagleEyeFilter implements Filter {

	private static final String EAGLE_EYE_ID = "EAGLE_EYE_ID";

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		EagleEye.get().setEagleEyeId(UUID.randomUUID().toString().replaceAll("-", ""));
		MDC.put(EAGLE_EYE_ID, EagleEye.get().getEagleEyeId());
		try {
			chain.doFilter(request, response);
		} finally {
			EagleEye.remove();
			MDC.remove(EAGLE_EYE_ID);
		}
	}
}
