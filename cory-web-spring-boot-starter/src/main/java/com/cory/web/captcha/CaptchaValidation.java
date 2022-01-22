package com.cory.web.captcha;

import com.octo.captcha.service.CaptchaServiceException;
import com.octo.captcha.service.image.ImageCaptchaService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

@Component
public class CaptchaValidation {

	private static final String CAPTCHA_CACHE_NAME = "__captcha_cache__";
	
	/** 校验时间范围：30秒。如果在此时间范围内重复提交，认为是需要验证码，否则不需要 */
	private static final long VALIDATION_INTERVAL = 30 * 1000;
	//private static final long VALIDATION_INTERVAL = 60 * 1000;

	/** 允许重复提交次数：2次。在此次数范围内重复提交，不需要验证码，超过则需要 -- 实际用户可以提交3次 */
	private static final int MAX_TIME = 2;
	
	/** 验证码参数名字 */
	public static final String CAPTCHA_PARAM = "captcha";
	/** reqeust属性，标志是否需要验证码，对于ajax，设置到response里 */
	public static final String NEED_CAPTCHA_ATTR = "needCaptcha";
	/** reqeust属性，标志是否验证码输入错误，对于ajax，设置到response里 */
	public static final String CAPTCHA_ERR = "captchaErr";

	@Autowired
	private CacheManager cacheManager;

	private CaptchaItem getFromCache(String key) {
		return cacheManager.getCache(CAPTCHA_CACHE_NAME).get(key, CaptchaItem.class);
	}

	private void putCache(String key, CaptchaItem item) {
		cacheManager.getCache(CAPTCHA_CACHE_NAME).put(key, item);
	}

	public boolean valid(HttpServletRequest request, ImageCaptchaService imageCaptchaService) {
		/*老版实现，考虑过期时间等，不需要这么多了
		String sessionId = request.getSession().getId();
		String uri = request.getRequestURI();
		
		String key = sessionId + "-" + uri;
		CaptchaItem item = getFromCache(key);
		
		long now = System.currentTimeMillis();
		
		boolean result = true;
		
		//如果为空，证明上次没访问过，那么是第一次，不用验证码，此时需要设置最后一次访问时间
		if (null == item) {
			item = new CaptchaItem(now, 1);
			putCache(key, item);
			
			result = true;
		} else {
			//如果不为空，则检查上次访问时间，如果大于间隔时间了或者在允许重复提交次数内，那么也不用验证码，如果在间隔时间范围内，那么需要验证码
			//同时注意更新访问次数和时间，分情况更新
			if (item.accessTime < MAX_TIME) {
				//只需要更新访问次数
				item.accessTime += 1;
				
				result = true;
			} else if (now - item.lastVisit > VALIDATION_INTERVAL) {
				//更新访问时间和访问次数
				item.accessTime = 1;
				item.lastVisit = now;
				
				result = true;
			} else {
				//更新访问时间
				item.lastVisit = now;
				
				//需要校验输入的校验码
				String captcha = request.getParameter(CAPTCHA_PARAM);
				if (StringUtils.isNotEmpty(captcha)) {
					try {
						result = imageCaptchaService.validateResponseForID(sessionId, captcha);
						
					} catch (CaptchaServiceException e) {
						result = false;
					}
					//如果验证码校验失败,那么设置失败信息
					request.setAttribute(CAPTCHA_ERR, !result);
				} else {
					//如果没有验证码,那么验证码校验错误信息不用设置,只需要设置"需要验证码"标志
					result = false;
				}
			}
		}
		
		//如果校验为true，那么就不需要验证码，否则，需要验证码
		request.setAttribute(NEED_CAPTCHA_ATTR, !result);
		
		return result;
		*/

		String sessionId = request.getSession().getId();

		boolean result;

		//需要校验输入的校验码
		String captcha = request.getParameter(CAPTCHA_PARAM);
		if (StringUtils.isNotEmpty(captcha)) {
			try {
				result = imageCaptchaService.validateResponseForID(sessionId, captcha);
			} catch (CaptchaServiceException e) {
				result = false;
			}
			//如果验证码校验失败,那么设置失败信息
			request.setAttribute(CAPTCHA_ERR, !result);
		} else {
			//如果没有验证码,那么验证码校验错误信息不用设置,只需要设置"需要验证码"标志
			result = false;
		}

		//如果校验为true，那么就不需要验证码，否则，需要验证码
		request.setAttribute(NEED_CAPTCHA_ATTR, !result);

		return result;
	}

	private static class CaptchaItem implements Serializable {
		public long lastVisit;
		public int accessTime;
		
		public CaptchaItem(long lastVisit, int accessTime) {
			this.lastVisit = lastVisit;
			this.accessTime = accessTime;
		}
	}
}
