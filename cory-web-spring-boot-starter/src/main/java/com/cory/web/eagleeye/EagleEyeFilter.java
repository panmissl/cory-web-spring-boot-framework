package com.cory.web.eagleeye;

import com.cory.eagleeye.EagleEye;
import com.cory.eagleeye.EagleEyeIdGenerator;
import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static com.cory.eagleeye.EagleEye.EAGLE_EYE_ID;

public class EagleEyeFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		String parentEagleEyeId = null;
		if (request instanceof HttpServletRequest) {
			parentEagleEyeId = ((HttpServletRequest) request).getHeader(EAGLE_EYE_ID);
		}
		EagleEye.get().setEagleEyeId(EagleEyeIdGenerator.generateEagleEyeId(parentEagleEyeId));
		MDC.put(EAGLE_EYE_ID, EagleEye.get().getEagleEyeId());
		try {
			chain.doFilter(request, response);
		} finally {
			EagleEye.remove();
			MDC.remove(EAGLE_EYE_ID);
		}
	}
}
