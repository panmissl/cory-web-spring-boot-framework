package com.cory.util.encoder;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.cory.constant.Constants;
import org.apache.commons.codec.binary.Hex;

/**
 * Md5加密
 * 
 * @author CORY
 *
 */
public class Md5Encoder {

	/**
	 * 使用utf8编码进行md5加密
	 * 
	 * @param text
	 * @return
	 */
	public static String encode(String text) {
		return encode(text, Constants.UTF8);
	}
	
	/**
	 * 使用指定编码进行md5加密
	 * 
	 * @param text
	 * @param encoding
	 * @return
	 */
	public static String encode(String text, String encoding) {
		MessageDigest messageDigest = getMessageDigest();
		byte[] digest;
		try {
			digest = messageDigest.digest(text.getBytes(encoding));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(encoding + " not supported!");
		}
		return new String(Hex.encodeHex(digest));
	}

	private static final MessageDigest getMessageDigest() {
		String algorithm = "MD5";
		try {
			return MessageDigest.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("No such algorithm ["
					+ algorithm + "]");
		}
	}
}
