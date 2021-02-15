package com.cory.util.encoder;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;

@Slf4j
public class Base64Encoder {
	
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
			return new String(Base64.encodeBase64(str.getBytes(ENCODING)), ENCODING);
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
			return new String(Base64.decodeBase64(str.getBytes(encoding)), encoding);
		} catch (UnsupportedEncodingException e) {
			log.error("解密失败。原因：不支持的字符集: " + encoding);
			return str;
		}
	}

}
