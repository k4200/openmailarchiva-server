package com.stimulus.util;

import java.text.ParseException;
import java.util.Date;

public class DateUtil {
	
	 private static SynchronizedDateFormat format = new SynchronizedDateFormat("yyyyMMddHHmmss");
	 
	 public static Date convertStringToDate(String dateStr) throws ParseException {
		 
		 if (dateStr==null)
			 throw new ParseException("too few character in date string",0);
		 
	
		 while (dateStr.length()<13) {
			 dateStr += "0";
		 }

		 return format.parse(dateStr.substring(0,13));

	 }
	 
	 
	 public static String convertDatetoString(Date date) {
	  	  return format.format(date);
	  }
	  
}
