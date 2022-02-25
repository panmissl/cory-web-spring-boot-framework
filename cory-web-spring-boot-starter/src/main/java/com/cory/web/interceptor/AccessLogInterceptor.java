package com.cory.web.interceptor;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.cory.constant.Constants;
import com.cory.model.AccessCount;
import com.cory.service.AccessCountService;
import com.cory.util.ConcurrentUtil;
import com.cory.util.DateFormatUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.ExecutorService;

@Component
@Slf4j
@Order(100)
public class AccessLogInterceptor implements HandlerInterceptor {

	private static final TransmittableThreadLocal<Long> TIME = new TransmittableThreadLocal<>();

	/** Y/N|status|method|uri|duration(ms)|remoteAddr|realIp|queryString */
	public static final String FORMAT = "%s|%s|%s|%s|%sms|%s|%s|%s";

	private static final String HOUR_FORMAT = "yyyyMMddHH";

	/** row: HOUR_FORMAT, column: api, value: count */
	private static final Map<String, Map<String, Integer>> ACCESS_COUNT_TABLE = new HashMap<>(128);

	private static final ExecutorService POOL = ConcurrentUtil.newSingleThreadPool();

	@Autowired
	private AccessCountService accessCountService;

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
		String flag = hasException ? "N" : "Y";
		int status = response.getStatus();
		String method = request.getMethod();
		String uri = request.getRequestURI();
		long start = TIME.get();
		long duration = System.currentTimeMillis() - start;
		String remoteAddr = request.getRemoteAddr();
		String realIp = request.getHeader(Constants.REQUEST_HEADER_KEY_REAL_IP);
		String query = request.getQueryString();

		log.info(String.format(FORMAT, flag, status, method, uri, duration, remoteAddr, realIp, query));
		access(uri);
	}

	public void access(String uri) {
		String hour = DateFormatUtils.format(new Date(), HOUR_FORMAT);

		//uri, count
		Map<String, Integer> row = ACCESS_COUNT_TABLE.get(hour);
		if (null == row) {
			row = new HashMap<>(2048);
			ACCESS_COUNT_TABLE.put(hour, row);
		}
		Integer count = row.get(uri);
		if (null == count) {
			count = 0;
		}
		count ++;
		row.put(uri, count);

		flushIfNeed(hour);
	}

	private void flushIfNeed(String nowHour) {
		Set<String> rowKeySet = ACCESS_COUNT_TABLE.keySet();
		if (CollectionUtils.isEmpty(rowKeySet)) {
			return;
		}
		Iterator<String> iterator = rowKeySet.iterator();
		while (iterator.hasNext()) {
			String hour = iterator.next();
			if (hour.compareTo(nowHour) < 0) {
				doFlushCount(hour);
			}
		}
	}

	private void doFlushCount(String hour) {
		POOL.submit(() -> {
			String day = hour.substring(0, 8);
			Map<String, Integer> row = ACCESS_COUNT_TABLE.get(hour);
			row.entrySet().forEach(entry -> accessCountService.add(AccessCount.builder()
					.day(day)
					.hour(hour)
					.uri(entry.getKey())
					.accessCount(entry.getValue())
					.build()));
			ACCESS_COUNT_TABLE.remove(hour);
		});
	}
}