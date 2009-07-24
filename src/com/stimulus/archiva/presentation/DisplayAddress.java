/* Copyright (C) 2005-2007 Jamie Angus Band 
 * MailArchiva Open Source Edition Copyright (c) 2005-2007 Jamie Angus Band
 * This program is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version
 * 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see http://www.gnu.org/licenses or write to the Free Software Foundation,Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

package com.stimulus.archiva.presentation;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.*;
import com.stimulus.archiva.domain.fields.EmailFieldValue;
import com.stimulus.util.EscapeUtil;

public class DisplayAddress  extends DisplayField implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8902129813234053670L;
	protected static Log logger = LogFactory.getLog(DisplayAddress.class.getName());
	static Pattern pattern = Pattern.compile("(.*)<(.*)>");
	boolean raw = false;
	
	public DisplayAddress(EmailFieldValue efv, boolean raw) {
		super(efv);
		this.raw = raw;
	}
	
	
	@Override
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
	
	@Override
	public String getTip() {
		return EscapeUtil.forHTML(efv.getValue());
	}

}
