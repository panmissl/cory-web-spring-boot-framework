package com.cory.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatUtils extends org.apache.commons.lang3.time.DateFormatUtils {
	private DateFormatUtils(){}
	
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
