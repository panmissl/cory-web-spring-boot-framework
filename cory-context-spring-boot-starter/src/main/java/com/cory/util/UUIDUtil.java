package com.cory.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.UUID;

/**
 * @author cory
 * @date 2022/3/2
 */
@Slf4j
public class UUIDUtil {

	private static final String DASH = "-";

	/**
	 * 生成一个基于时间的数字编码：yyyyMMddhhmmss + randomNumeric(6)
	 * @return
	 */
	public static String generateNumberCodeBaseOnTime() {
		return DateFormatUtils.formatNowAsSecondWithoutDash() + RandomStringUtils.randomNumeric(8);
	}

	/**
	 * 生成一个UUID，去除-，只保留字母和数字
	 * @return
	 */
	public static String uuid() {
		return UUID.randomUUID().toString().replaceAll(DASH, "");
	}
}
