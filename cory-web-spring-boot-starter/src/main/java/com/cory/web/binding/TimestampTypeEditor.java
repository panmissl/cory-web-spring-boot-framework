package com.cory.web.binding;

import org.springframework.util.StringUtils;

import java.beans.PropertyEditorSupport;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * 日期编辑器
 * 
 * 根据日期字符串长度判断是长日期还是短日期。只支持yyyy-MM-dd，yyyy-MM-dd HH:mm:ss两种格式。
 * 扩展支持yyyy,yyyy-MM日期格式
 */
public class TimestampTypeEditor extends PropertyEditorSupport {
	public static final DateFormat DF_LONG = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final DateFormat DF_SHORT = new SimpleDateFormat("yyyy-MM-dd");
	public static final DateFormat DF_YEAR = new SimpleDateFormat("yyyy");
	public static final DateFormat DF_MONTH = new SimpleDateFormat("yyyy-MM");

	/**
	 * 短类型日期长度
	 */
	public static final int SHORT_DATE = 10;
	
	public static final int YEAR_DATE = 4;
	
	public static final int MONTH_DATE = 7;

	public void setAsText(String text) throws IllegalArgumentException {
		text = text.trim();
		if (!StringUtils.hasText(text)) {
			setValue(null);
			return;
		}
		try {
			if (text.length() <= YEAR_DATE) {
				setValue(new Timestamp(DF_YEAR.parse(text).getTime()));
			}else  if (text.length() <= MONTH_DATE) {
				setValue(new Timestamp(DF_MONTH.parse(text).getTime()));
			}else if (text.length() <= SHORT_DATE) {
				setValue(new Timestamp(DF_SHORT.parse(text).getTime()));
			} else {
				setValue(new Timestamp(DF_LONG.parse(text).getTime()));
			}
		} catch (ParseException ex) {
			IllegalArgumentException iae = new IllegalArgumentException(
					"Could not parse datetime: " + ex.getMessage());
			iae.initCause(ex);
			throw iae;
		}
	}

	/**
	 * Format the Date as String, using the specified DateFormat.
	 */
	public String getAsText() {
		Timestamp value = (Timestamp) getValue();
		return (value != null ? DF_LONG.format(value) : "");
	}
}
