package com.cory.util.encoder;

import java.io.UnsupportedEncodingException;

import com.sun.xml.internal.messaging.saaj.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Base64Encoder {
	
	private static final Logger log = LoggerFactory.getLogger(Base64Encoder.class);
	
	private static final String ENCODING = "utf-8";

	/**
	 * 加密：使用utf8编码
	 * @param str
	 * @return
	 */
	public static String encode(String str) {
		return encode(str, ENCODING);
	}
	
	/**
	 * 加密：使用指定编码
	 * @param str
	 * @param encoding
	 * @return
	 */
	public static String encode(String str, String encoding) {
		if (null == str) {
			return null;
		}
		
		try {
			return new String(Base64.encode(str.getBytes(ENCODING)), ENCODING);
		} catch (UnsupportedEncodingException e) {
			log.error("加密失败。原因：不支持的字符集: " + encoding);
			return str;
		}
	}
	
	/**
	 * 解密：使用utf8编码
	 * @param str
	 * @return
	 */
	public static String decode(String str) {
		return decode(str, ENCODING);
	}
	
	/**
	 * 解密：使用指定编码
	 * @param str
	 * @param encoding
	 * @return
	 */
	public static String decode(String str, String encoding) {
		if (null == str) {
			return null;
		}
		
		try {
			return new String(Base64.base64Decode(str).getBytes(), encoding);
		} catch (UnsupportedEncodingException e) {
			log.error("解密失败。原因：不支持的字符集: " + encoding);
			return str;
		}
	}

}
