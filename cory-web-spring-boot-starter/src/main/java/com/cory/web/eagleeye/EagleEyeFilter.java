package com.cory.web.eagleeye;

import com.cory.eagleeye.EagleEye;
import com.cory.eagleeye.EagleEyeIdGenerator;
import org.slf4j.MDC;
import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static com.cory.eagleeye.EagleEye.EAGLE_EYE_ID;

@Component
public class EagleEyeFilter implements OrderedFilter {

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

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 200;
	}
}
