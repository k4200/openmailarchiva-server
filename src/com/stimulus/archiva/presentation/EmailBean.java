
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

import java.text.DateFormat;
import java.util.*;

import org.apache.log4j.Logger;
import com.stimulus.archiva.domain.*;
import com.stimulus.struts.BaseBean;

public class EmailBean extends BaseBean {

		private static final long serialVersionUID = 6714503589623134666L;

		protected static Logger logger = Logger.getLogger(EmailBean.class.getName());
	  
		protected Email email;

	    public EmailBean(Email email) {
	        this.email = email;
	    }

	    public static List<EmailBean> getEmailBeans(List<Email> emails) {
			  List<EmailBean> emailBeans = new LinkedList<EmailBean>();
			  for (Email email: emails)
				  emailBeans.add(new EmailBean(email));
			  return emailBeans;
		}
	    
	    public String getSubject() {
	    	String subject = email.getSubject();
	    	if (subject==null || subject.trim().length()<2) 
	    		return "<no subject>";
	    	return subject;
	    }
	    
	    public String getReceivedDate() {
	    	String date = "";
	        try {
	             Date receivedDate = email.getReceivedDate();
	             DateFormat format = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, getLocale());
	           	 date = format.format(receivedDate);
	           	 date += " " + getZoneOffset(receivedDate);
	        } catch(Exception ex) {
	        	logger.debug("failed to retrieve received date from email {date="+date.toString()+"}");
	        }
	        return date;
	    }
	    
	    
	    public String getSentDate() {
	    	String date = "";
	        try {
	             Date sentDate = email.getSentDate();
	             DateFormat format = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, getLocale());
	           	 date = format.format(sentDate);
	           	 date += " " + getZoneOffset(sentDate);
	        } catch(Exception ex) {
	        	logger.error("failed to sent received date from email {"+date.toString()+"}");
	        }
	        return date;
	    }
	    
	    public String getZoneOffset(Date date) {
	    	 String dateStr;
             Calendar cal = Calendar.getInstance(getLocale());
	 		 cal.setTime(date);
	 		 int offset = cal.get(Calendar.ZONE_OFFSET)/(60*60*1000);
	 		 if (offset>-1)
	 			 dateStr ="(GMT + "+offset+"h)";
	 		 else
	 			dateStr ="(GMT - "+offset+"h)";
	 		 return dateStr;
	    }

	    public String getSize()
	    {
	    	  return Integer.toString(email.getSize() / 1024)+"k";
	    }
	    
	    public String getFlags() {
	    	String flags = "";
	    	List<String> flagList = email.getFlagList();
	    	for (String flag : flagList) {
	    		flags += getMessage("email.flag_"+flag);
	    		flags += ",";
	    	}
	    	if (flags.length()>0)
	    		flags = flags.trim().substring(0,flags.length()-1);
	    	return flags;
	    }
	      
	    public String getFromAddress() {
	    	return email.getFromAddress(Email.DisplayMode.ALL);
	    }
	    
	    public String getToAddresses() {
	    	return email.getToAddresses(Email.DisplayMode.ALL);
	    }
	    
	    public String getBCCAddresses() {
	    	return email.getBCCAddresses(Email.DisplayMode.ALL);
	    }
	    
	    public String getCCAddresses() {
	    	return email.getCCAddresses(Email.DisplayMode.ALL);
	    }
	    
	    public String getPriority() {
	    	return getMessage("email.priority_"+email.getPriority()); 
	    }
	    
	    public int getPriorityID() {
	    	return email.getPriorityID();
	    }
	    
	    public String getInternetHeaders() {
	    	String headers = "";
	        try {
	             headers = email.getInternetHeaders();
	        }
	        catch(Exception ex) {
	        	logger.error("failed to retrieve internet headers from email {"+email.toString()+"}");
	        }
	    	return headers;
	    }
	    
	    protected Email getEmail() {
	    	return email;
	    }
	    
	    public String toString() {
	    	return email.toString();
	    }
	    
	
}
