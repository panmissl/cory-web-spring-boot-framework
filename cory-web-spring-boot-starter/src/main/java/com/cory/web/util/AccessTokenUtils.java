package com.cory.web.util;

import com.cory.context.CoryEnv;
import com.cory.util.MapBuilder;
import com.cory.util.encoder.Md5Encoder;
import com.cory.util.systemconfigcache.SystemConfigCacheKey;
import com.cory.util.systemconfigcache.SystemConfigCacheUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Slf4j
public class AccessTokenUtils {

	private static final String COMMA = ",";
	private static final String COLON = ":";

	public static final String ACCESS_KEY_ID = "Access-Key-Id";
	public static final String ACCESS_KEY_TIMESTAMP = "Access-Key-Timestamp";
	public static final String ACCESS_KEY_TOKEN = "Access-Key-Token";

	/** ID::SECRET::TIMESTAMP */
	public static final String TOKEN_FORMAT = "%s::%s::%s";

	private static final int DEFAULT_EXPIRE_TIME = 10 * 1000;

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class AccessToken {
		private String ak;
		private String ts;
		private String token;
	}

	public static AccessToken generateAccessToken(String ak, String sk) {
		String ts = String.valueOf(System.currentTimeMillis());
		return AccessToken.builder().ak(ak).ts(ts).token(generateToken(ak, sk, ts)).build();
	}

	public static Map<String, String> generateAccessTokenHeaders(String ak, String sk) {
		AccessToken at = generateAccessToken(ak, sk);
		return MapBuilder.create(String.class, String.class)
				.put(ACCESS_KEY_ID, ak)
				.put(ACCESS_KEY_TIMESTAMP, at.ts)
				.put(ACCESS_KEY_TOKEN, at.token)
				.build();
	}

	/**
	 * token校验
	 * @param ak
	 * @param ts 用System.currentTimeMillis()得到的时间，long型，不要用格式化的日期字符串，因为会有时区问题
	 * @param token
	 * @return
	 */
	public static boolean checkToken(String ak, String ts, String token) {
		if (StringUtils.isBlank(ak) || StringUtils.isBlank(ts) || StringUtils.isBlank(token)) {
			log.error("ak, ts or token is blank, ak: {}, time: {}, token: {}", ak, ts, token);
			return false;
		}

		//先校验时间
		long time = Long.valueOf(ts);

		//开发环境，用0来表示跳过校验
		if (CoryEnv.IS_DEV && time == 0) {
			return true;
		}

		int expireTime = SystemConfigCacheUtil.getIntCache(SystemConfigCacheKey.TOKEN_EXPIRE_TIME_IN_SECOND, DEFAULT_EXPIRE_TIME);
		if (System.currentTimeMillis() - time > expireTime) {
			log.error("timeout, ak: {}, time: {}, token: {}", ak, ts, token);
			return false;
		}

		String dbSecret = parseDbSecret(ak);
		if (StringUtils.isBlank(dbSecret)) {
			log.error("db secret is blank, ak: {}, time: {}, token: {}", ak, ts, token);
			return false;
		}

		String dbToken = generateToken(ak, dbSecret, ts);
		if (!token.equals(dbToken)) {
			log.error("token not match, ak: {}, time: {}, token: {}, db token: {}", ak, ts, token, dbToken);
			return false;
		}

		return true;
	}

	private static String parseDbSecret(String ak) {
		String tokenConfigs = SystemConfigCacheUtil.getCache(SystemConfigCacheKey.TOKEN_CONFIGS);
		if (StringUtils.isBlank(tokenConfigs)) {
			return null;
		}
		//key1:secret1,key2:secret2
		String[] tokenConfigArr = tokenConfigs.split(COMMA);
		for (String config : tokenConfigArr) {
			String[] aksk = config.split(COLON);
			if (ak.equals(aksk[0])) {
				return aksk[1];
			}
		}
		return null;
	}

	private static String generateToken(String ak, String sk, String ts) {
		return Md5Encoder.encode(String.format(TOKEN_FORMAT, ak, sk, ts));
	}

	public static void main(String[] args) {
		AccessToken at = generateAccessToken("123", "456");
		System.out.println(String.format("ak: %s, ts: %s, token: %s", at.ak, at.ts, at.token));
	}

}
