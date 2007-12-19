package com.stimulus.archiva.presentation;

import com.stimulus.archiva.domain.fields.EmailField;
import com.stimulus.archiva.domain.fields.EmailFieldValue;
import com.stimulus.util.EscapeUtil;

import java.io.Serializable;
import java.util.Locale;

public class DisplayField implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5144163717134902765L;
	EmailFieldValue efv;
	
	public DisplayField(EmailFieldValue efv) {
		this.efv = efv;
	}
	
	public String getDisplay() {
		if (efv.getValue()!=null)
			return EscapeUtil.forHTML(efv.getValue());
			//return stripGarbage(efv.getValue());
		else return "";
	}
	
	public String getValue() {
		if (efv.getValue()!=null)
			return EscapeUtil.forHTML(efv.getValue());
		else return "";
	}
	
	public EmailField getField() {
		return efv.getField();
	}
	
	public String getTip() {
		return getDisplay();
	}
	
	 public static String stripGarbage(String s) {
		 
		 String bad = "\\/&\"'";
		 StringBuffer result = new StringBuffer();
	     for ( int i = 0; i < s.length(); i++ ) {
	        if ( bad.indexOf(s.charAt(i)) < 0 )
	           result.append(s.charAt(i));
	     }
	    return result.toString();
    }
	 
	public static DisplayField getDisplayField(EmailFieldValue efv, Locale locale, boolean raw) {
		
		if (efv.getField().getName().equals("sentdate"))
		 	return new DisplayDate(efv,locale);
		else if (efv.getField().getName().equals("receiveddate"))
			return new DisplayDate(efv,locale);
		else if (efv.getField().getName().equals("archivedate"))
			return new DisplayDate(efv,locale);
		else if (efv.getField().getName().equals("size"))
			 return new DisplaySize(efv);
		 else if (efv.getField().getName().equals("to") ||
				  efv.getField().getName().equals("from") ||
				  efv.getField().getName().equals("bcc") ||
				  efv.getField().getName().equals("cc"))
			 return new DisplayAddress(efv,raw);
		 else if (efv.getField().getName().equals("from"))
			 return new DisplayAddress(efv,raw);
		 else if (efv.getField().getName().equals("priority"))
			 return new DisplayPriority(efv);
		 else 
			 return new DisplayField(efv);
	} 
	
	
	
}