package com.cory.util;

import com.cory.constant.Constants;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Tom
 */
public class DateUtils extends org.apache.commons.lang3.time.DateUtils {

	public static SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
	public static SimpleDateFormat format1 = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
	public static SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * 使用 {@link Constants#ALL_DATE_FORMAT} 里的所有格式，尝试解析日期。解析失败抛错
	 * @param date
	 * @return
	 * @throws ParseException
	 */
	public static Date parseDate(String date) throws ParseException {
		return parseDate(date, Constants.ALL_DATE_FORMAT);
	}

	public static int getDateField(Date date, int field) {
		Calendar c = getCalendar();
		c.setTime(date);
		return c.get(field);
	}

	public static int getYearsBetweenDate(Date begin, Date end) {
		int bYear = getDateField(begin, Calendar.YEAR);
		int eYear = getDateField(end, Calendar.YEAR);
		return eYear - bYear;
	}

	public static int getMonthsBetweenDate(Date begin, Date end) {
		int bMonth = getDateField(begin, Calendar.MONTH);
		int eMonth = getDateField(end, Calendar.MONTH);
		return eMonth - bMonth;
	}

	public static int getWeeksBetweenDate(Date begin, Date end) {
		int bWeek = getDateField(begin, Calendar.WEEK_OF_YEAR);
		int eWeek = getDateField(end, Calendar.WEEK_OF_YEAR);
		return eWeek - bWeek;
	}

	public static int getDaysBetweenDate(Date begin, Date end) {
		return (int) ((end.getTime()-begin.getTime())/(1000 * 60 * 60 * 24));
	}

	public static void main(String args[]) {
		System.out.println(getSpecficDateStart(new Date(), 288));
	}

	/**
	 * 获取date年后的amount年的第一天的开始时间
	 * 
	 * @param amount
	 *            可正、可负
	 * @return
	 */
	public static Date getSpecficYearStart(Date date, int amount) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.YEAR, amount);
		cal.set(Calendar.DAY_OF_YEAR, 1);
		return getStartDate(cal.getTime());
	}

	/**
	 * 获取date年后的amount年的最后一天的终止时间
	 * 
	 * @param amount
	 *            可正、可负
	 * @return
	 */
	public static Date getSpecficYearEnd(Date date, int amount) {
		Date temp = getStartDate(getSpecficYearStart(date, amount + 1));
		Calendar cal = Calendar.getInstance();
		cal.setTime(temp);
		cal.add(Calendar.DAY_OF_YEAR, -1);
		return getFinallyDate(cal.getTime());
	}

	/**
	 * 获取date月后的amount月的第一天的开始时间
	 * 
	 * @param amount
	 *            可正、可负
	 * @return
	 */
	public static Date getSpecficMonthStart(Date date, int amount) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MONTH, amount);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return getStartDate(cal.getTime());
	}

	/**
	 * 获取当前自然月后的amount月的最后一天的终止时间
	 * 
	 * @param amount
	 *            可正、可负
	 * @return
	 */
	public static Date getSpecficMonthEnd(Date date, int amount) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(getSpecficMonthStart(date, amount + 1));
		cal.add(Calendar.DAY_OF_YEAR, -1);
		return getFinallyDate(cal.getTime());
	}

	/**
	 * 获取date周后的第amount周的开始时间（这里星期一为一周的开始）
	 * 
	 * @param amount
	 *            可正、可负
	 * @return
	 */
	public static Date getSpecficWeekStart(Date date, int amount) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.setFirstDayOfWeek(Calendar.MONDAY); /* 设置一周的第一天为星期一 */
		cal.add(Calendar.WEEK_OF_MONTH, amount);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		return getStartDate(cal.getTime());
	}

	/**
	 * 获取date周后的第amount周的最后时间（这里星期日为一周的最后一天）
	 * 
	 * @param amount
	 *            可正、可负
	 * @return
	 */
	public static Date getSpecficWeekEnd(Date date, int amount) {
		Calendar cal = Calendar.getInstance();
		cal.setFirstDayOfWeek(Calendar.MONDAY); /* 设置一周的第一天为星期一 */
		cal.add(Calendar.WEEK_OF_MONTH, amount);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		return getFinallyDate(cal.getTime());
	}

	public static Date getSpecficDateStart(Date date, int amount) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_YEAR, amount);
		return getStartDate(cal.getTime());
	}

	/**
	 * 得到指定日期的一天的的最后时刻23:59:59
	 * 
	 * @param date
	 * @return
	 */
	public static Date getFinallyDate(Date date) {
		String temp = format.format(date);
		temp += " 23:59:59";

		try {
			return format1.parse(temp);
		} catch (ParseException e) {
			return null;
		}
	}

	/**
	 * 得到指定日期的一天的开始时刻00:00:00
	 * 
	 * @param date
	 * @return
	 */
	public static Date getStartDate(Date date) {
		String temp = format.format(date);
		temp += " 00:00:00";

		try {
			return format1.parse(temp);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 
	 * @param date
	 *            指定比较日期
	 * @param compareDate
	 * @return
	 */
	public static boolean isInDate(Date date, Date compareDate) {
		if (compareDate.after(getStartDate(date))
				&& compareDate.before(getFinallyDate(date))) {
			return true;
		} else {
			return false;
		}

	}
	
	/**
	 * 获取两个时间的差值秒
	 * @param d1
	 * @param d2
	 * @return
	 */
	public static Integer getSecondBetweenDate(Date d1,Date d2){
		Long second=(d2.getTime()-d1.getTime())/1000;
		return second.intValue();
	}

	private int getYear(Calendar calendar) {
		return calendar.get(Calendar.YEAR);
	}

	private int getMonth(Calendar calendar) {
		return calendar.get(Calendar.MONDAY) + 1;
	}

	private int getDate(Calendar calendar) {
		return calendar.get(Calendar.DATE);
	}

	private int getHour(Calendar calendar) {
		return calendar.get(Calendar.HOUR_OF_DAY);
	}

	private int getMinute(Calendar calendar) {
		return calendar.get(Calendar.MINUTE);
	}

	private int getSecond(Calendar calendar) {
		return calendar.get(Calendar.SECOND);
	}

	public static Calendar getCalendar() {
		return Calendar.getInstance();
	}

	public static Calendar getCalendar(Date time) {
		Calendar c = Calendar.getInstance();
		c.setTime(time);
		return c;
	}

	public static Date getNow() {
		return new Date();
	}
	
	public static Timestamp getNowTimestamp() {
		return new Timestamp(getNow().getTime());
	}
	
	/**
	 * fullFormat: yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String getNowStringInFullFormat() {
		return getDateStringInFullFormat(getNow());
	}
	
	/**
	 * fullFormat: yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String getDateStringInFullFormat(Date date) {
		return fullFormat.format(date);
	}
	
	/**
	 * fullFormat: yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static Date getDateFromFullFormat(String dateString) {
		try {
			return fullFormat.parse(dateString);
		} catch (ParseException e) {
			return null;
		}
	}
	
	/**
	 * yyyy-MM-dd HH:mm:ss
	 * @param date
	 * @return
	 */
	public static String formatFull(Date date) {
		return format(date, "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * yyyy-MM-dd
	 * @param date
	 * @return
	 */
	public static String formatShort(Date date) {
		return format(date, "yyyy-MM-dd");
	}
	
	public static String format(Date date, String pattern) {
		if (null == date) {
			return "";
		}
		if (null == pattern || pattern.trim().length() == 0) {
			pattern = "yyyy-MM-dd HH:mm:ss";
		}
		return new SimpleDateFormat(pattern).format(date);
	}

	/**
	 * 将指定日期增加一些天数，可以为正数或者负数
	 *
	 * @param date
	 * @param day
	 * @return
	 */
	public static Date add(Date date, int day) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DAY_OF_MONTH, day);
		return c.getTime();
	}

	public static class CalendarBuilder {
		private Calendar c;

		/**
		 * 配合DateUtils.getCalendar()方法使用
		 * @param c
		 */
		private CalendarBuilder(Calendar c) {
			this.c = c;
		}

		/**
		 * 设置日期字段，
		 * @param field 字段名。@see Calendar#set
		 * @param value 字段值。@see Calendar#set
		 * @return
		 * @see Calendar#set(int, int)
		 */
		public CalendarBuilder set(int field, int value) {
			this.c.set(field, value);
			return this;
		}

		/**
		 * 加天数
		 * @param delta 要增加的天数。如果为负数则表示减
		 * @return
		 */
		public CalendarBuilder addDay(int delta) {
			this.c.add(Calendar.DAY_OF_MONTH, delta);
			return this;
		}

		public static CalendarBuilder create(Calendar c) {
			return new CalendarBuilder(c);
		}

		public Calendar build() {
			return c;
		}
	}
}
