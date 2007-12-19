package com.stimulus.archiva.presentation;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.stimulus.archiva.domain.fields.EmailFieldValue;
import com.stimulus.util.DateUtil;

public class DisplayDate extends DisplayField implements Serializable {

	protected static Logger logger =  Logger.getLogger(DomainBean.class.getName());
	private static final long serialVersionUID = 5168479608715082055L;
	Locale locale;

	public DisplayDate(EmailFieldValue efv, Locale locale) {
		super(efv);
		this.locale = locale;
	}
	
	public String getDisplay() {
		if (efv.getValue()==null || efv.getValue().length()<1)
			return "";
		DateFormat formatter = null;
		
		formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT,locale);
		String dateStr = efv.getValue().substring(1,efv.getValue().length()-1);
		Date sent;
		try {
			sent = DateUtil.convertStringToDate(dateStr);
		} catch (Exception e) {
			logger.error("failed to convert string to date. "+e.getMessage(),e);
			sent = new Date();
		}
		return formatter.format(sent) + getZoneOffset(sent);
	}
	
	 public String getZoneOffset(Date date) {
    	 String dateStr;
         Calendar cal = Calendar.getInstance(locale);
 		 cal.setTime(date);
 		 int offset = cal.get(Calendar.ZONE_OFFSET)/(60*60*1000);
 		 if (offset>-1)
 			 dateStr =" (+"+offset+"h)";
 		 else
 			dateStr =" (-"+offset+"h)";
 		 return dateStr;
    }
	

}