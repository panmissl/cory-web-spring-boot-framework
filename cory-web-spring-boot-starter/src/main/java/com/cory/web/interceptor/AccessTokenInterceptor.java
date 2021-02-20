package com.cory.web.interceptor;

import com.cory.constant.Constants;
import com.cory.constant.ErrorCode;
import com.cory.context.GenericResult;
import com.cory.util.DateUtils;
import com.cory.util.encoder.Md5Encoder;
import com.cory.util.systemconfigcache.SystemConfigCacheKey;
import com.cory.util.systemconfigcache.SystemConfigCacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class AccessTokenInterceptor implements HandlerInterceptor {

	private static final String COMMA = ",";
	private static final String COLON = ":";

	private static final String ACCESS_KEY_ID = "Access-Key-Id";
	private static final String ACCESS_KEY_TIMESTAMP = "Access-Key-Timestamp";
	private static final String ACCESS_KEY_TOKEN = "Access-Key-Token";

	/** ID::SECRET::TIMESTAMP */
	private static final String TOKEN_FORMAT = "%s::%s::%s";

	private static final int EXPIRE_TIME = 10 * 60 * 1000;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ServletException {
		String id = request.getHeader(ACCESS_KEY_ID);
		String ts = request.getHeader(ACCESS_KEY_TIMESTAMP);
		String token = request.getHeader(ACCESS_KEY_TOKEN);

		try {
			if (StringUtils.isBlank(id) || StringUtils.isBlank(ts) || StringUtils.isBlank(token)) {
				response401(response);
				return false;
			}

			//先校验时间
			long time = DateUtils.parseDate(ts, Constants.DATE_FORMAT_FULL_WITHOUT_DASH).getTime();
			if (System.currentTimeMillis() - time > EXPIRE_TIME) {
				response401(response);
				return false;
			}

			String dbSecret = parseDbSecret(id);
			if (StringUtils.isBlank(dbSecret)) {
				response401(response);
				return false;
			}

			if (!token.equals(Md5Encoder.encode(String.format(TOKEN_FORMAT, id, dbSecret, ts)))) {
				response401(response);
				return false;
			}

			return true;
		} catch (Throwable t) {
			log.error("access token validate error.", t);
			response401(response);
			return false;
		}
	}

	private String parseDbSecret(String accessKeyId) {
		String tokenConfigs = SystemConfigCacheUtil.getCache(SystemConfigCacheKey.TOKEN_CONFIGS);
		if (StringUtils.isBlank(tokenConfigs)) {
			return null;
		}
		//key1:secret1,key2:secret2
		String[] tokenConfigArr = tokenConfigs.split(COMMA);
		for (String config : tokenConfigArr) {
			String[] aksk = config.split(COLON);
			if (accessKeyId.equals(aksk[0])) {
				return aksk[1];
			}
		}
		return null;
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