package com.stimulus.util;

import java.util.*;
import java.text.*;

public class DateUtil {
	
	 private static SynchronizedDateFormat format = new SynchronizedDateFormat("yyyyMMddHHmmss");
	 
	 public static Date convertStringToDate(String dateStr) throws ParseException {
		 
		 if (dateStr==null)
			 throw new ParseException("too few character in date string",0);

		 return format.parse(dateStr.substring(0,dateStr.length()));

	 }
	 
	 
	 public static String convertDatetoString(Date date) {
	  	  return format.format(date);
	  }
	 
	 public static DateFormat getShortDateFormat() { 
		 return getShortDateFormat(Locale.getDefault());
	 }
	 
	 public static DateFormat getShortDateFormat(Locale locale) {
		 if (locale.getCountry().equals("ZA"))
			 return new SimpleDateFormat("dd/MM/yyyy h:mm a");
		 else
			 return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT,locale);
	 }
	 
	 public static String getZoneOffset(Date date, Locale locale) {
    	 String dateStr;
         Calendar cal = Calendar.getInstance(locale);
 		 cal.setTime(date);
 		 int offset = cal.get(Calendar.ZONE_OFFSET)/(60*60*1000);
 		 if (offset>-1)
 			 dateStr =" +"+offset+"h";
 		 else
 			dateStr =" -"+offset+"h";
 		 return dateStr;
    }
	 
	 public static String getShortDate(Date date, Locale locale) {
		 DateFormat formatter = null;
		 formatter = DateUtil.getShortDateFormat(locale);
		 return formatter.format(date) + DateUtil.getZoneOffset(date,locale);
	 }
}
