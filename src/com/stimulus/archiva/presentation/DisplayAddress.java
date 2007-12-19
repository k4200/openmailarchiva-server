package com.stimulus.archiva.presentation;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.stimulus.archiva.domain.fields.EmailFieldValue;
import com.stimulus.util.EscapeUtil;

public class DisplayAddress  extends DisplayField implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8902129813234053670L;
	protected static Logger logger = Logger.getLogger(DisplayAddress.class);
	static Pattern pattern = Pattern.compile("(.*)<(.*)>");
	boolean raw = false;
	
	public DisplayAddress(EmailFieldValue efv, boolean raw) {
		super(efv);
		this.raw = raw;
	}
	
	
	public String getDisplay() {
		if (!raw) {
			StringBuffer display = new StringBuffer();
			String value = efv.getValue();
			if (value!=null && value.length()>0) {
				String[] addresses = value.split(",");
				for (int i=0;i<addresses.length;i++) {
			    	    Matcher matcher = pattern.matcher(addresses[i]);
			    	    if (matcher.find() && matcher.group(1).trim().length()>0) {
			    	    	display.append(matcher.group(1).trim());
			    	    	display.append(", ");
			    	    } else {
			    	    	display.append(addresses[i].trim());
			    	    	display.append(", ");
			    	    }
				}
			} else return "";
			String result = display.toString().replaceAll("<","").replaceAll(">","");
			if ( result.endsWith(", "))
				 result = result.substring(0,result.length()-2);
			return  EscapeUtil.forHTML(result);
		} else return EscapeUtil.forHTML(efv.getValue());
	}
	
	public String getTip() {
		return EscapeUtil.forHTML(efv.getValue());
	}

}
