package com.cory.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatUtils extends org.apache.commons.lang3.time.DateFormatUtils {

	public static final String YEAR_FORMAT = "yyyy";
	public static final String MONTH_FORMAT = "yyyy-MM";
	public static final String DAY_FORMAT = "yyyy-MM-dd";
	public static final String HOUR_FORMAT = "yyyy-MM-dd HH";
	public static final String MINUTE_FORMAT = "yyyy-MM-dd HH:mm";
	public static final String SECOND_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private DateFormatUtils(){}

	/** yyyy, eg: 2022 */
	public static String formatAsYear(Date date) {
		return DateFormatUtils.format(date, YEAR_FORMAT);
	}

	/** yyyy-MM, eg: 2022-05 */
	public static String formatAsMonth(Date date) {
		return DateFormatUtils.format(date, MONTH_FORMAT);
	}

	/** yyyy-MM-dd, eg: 2022-05-23 */
	public static String formatAsDay(Date date) {
		return DateFormatUtils.format(date, DAY_FORMAT);
	}

	/** yyyy-MM-dd HH, eg: 2022-05-23 15 */
	public static String formatAsHour(Date date) {
		return DateFormatUtils.format(date, HOUR_FORMAT);
	}

	/** yyyy-MM-dd HH:mm, eg: 2022-05-23 15:20 */
	public static String formatAsMinute(Date date) {
		return DateFormatUtils.format(date, MINUTE_FORMAT);
	}

	/** yyyy-MM-dd HH:mm:ss, eg: 2022-05-23 15:20:55 */
	public static String formatAsSecond(Date date) {
		return DateFormatUtils.format(date, SECOND_FORMAT);
	}

	/** yyyy, eg: 2022 */
	public static String formatNowAsYear() {
		return DateFormatUtils.format(new Date(), YEAR_FORMAT);
	}

	/** yyyy-MM, eg: 2022-05 */
	public static String formatNowAsMonth() {
		return DateFormatUtils.format(new Date(), MONTH_FORMAT);
	}

	/** yyyy-MM-dd, eg: 2022-05-23 */
	public static String formatNowAsDay() {
		return DateFormatUtils.format(new Date(), DAY_FORMAT);
	}

	/** yyyy-MM-dd HH, eg: 2022-05-23 15 */
	public static String formatNowAsHour() {
		return DateFormatUtils.format(new Date(), HOUR_FORMAT);
	}

	/** yyyy-MM-dd HH:mm, eg: 2022-05-23 15:20 */
	public static String formatNowAsMinute() {
		return DateFormatUtils.format(new Date(), MINUTE_FORMAT);
	}

	/** yyyy-MM-dd HH:mm:ss, eg: 2022-05-23 15:20:55 */
	public static String formatNowAsSecond() {
		return DateFormatUtils.format(new Date(), SECOND_FORMAT);
	}

	public static String formatDate(Date date){
		return DateFormat.getDateInstance().format(date);
	}
	
	public static String formatTime(Date date){
		return DateFormat.getTimeInstance().format(date);
	}
	
	public static String formatDateTime(Date date){
		if(DateFormat.getDateTimeInstance().format(date).contains("0:00:00")){
			return formatDate(date);
		}
		return DateFormat.getDateTimeInstance().format(date);
	}
	
}
