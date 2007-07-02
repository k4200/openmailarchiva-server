/* Copyright (C) 2005 Jamie Angus Band 
 * This software program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.stimulus.archiva.domain;


import java.util.*;
import java.io.*;
import javax.mail.*;
import javax.mail.internet.*;
import org.apache.log4j.*;


public class Email extends MimeMessage implements Serializable {

  protected static Logger logger = Logger.getLogger(Email.class.getName());
  protected static Session session;
  protected byte[] compressedOriginal;
  protected int size;
  protected int volumeIndex;
  protected boolean selected = false;
  protected EmailID emailId = null;

  static
  {
	try {
  		Properties system_properties = System.getProperties();
  		Session session = Session.getDefaultInstance(system_properties,null);
  		session.setDebug(true);
	} catch (Exception e) {
		logger.fatal("failed to establish a mail session. Cause:"+e.toString());
  	}
  };

  public Email(InputStream is, boolean headersOnly) throws MessagingException
  {
  	 super(session,is);
 	 if (headersOnly) clearContent();

  }

  public Email(InputStream is, boolean headersOnly, EmailID emailId) throws MessagingException {
  	super(session,is);
  	if (headersOnly) clearContent();
  	this.emailId = emailId;
  }

  public Email() throws MessagingException
  {
  	super(session);
  }

  private void clearContent() {
  	 size = content.length;
  	 content=null;
  }

  public String getAddresses(Message.RecipientType recipientType) {
      String address = "";
      try {
  	    address = getHeader(getHeader(recipientType),",");

  	} catch (Exception me) {
  	    me.printStackTrace();
  		logger.error("failed to collate email address info. Cause:"+me.toString());
	}
  	if (address!=null)
  	    return stripGarbage(address.toLowerCase());
  	else
  	    return "";
  }

  public String[] getAddressList(Message.RecipientType recipientType) {
      String[] addresses = null;
      try {
  	    addresses = getHeader(getHeader(recipientType));

  	    if (addresses!=null) {
  	        for (int i = 0; i<addresses.length;i++)
  	            addresses[i] = stripGarbage(addresses[i]).trim();
  	    }
  	} catch (Exception me) {
  	    me.printStackTrace();
  		logger.error("failed to collate email address info. Cause:"+me.toString());
	}
  	return addresses;
  }

  public String[] getAllRecipientsList() {
      String[] to = getAddressList(RecipientType.TO);
      String[] cc = getAddressList(RecipientType.CC);
      String[] bcc = getAddressList(RecipientType.BCC);
      String[] news = getAddressList(RecipientType.NEWSGROUPS);

      ArrayList recipientList = new ArrayList();
      if (to!=null)
      for (int i=0;i<to.length;i++)
          recipientList.add(to[i]);
      if (cc!=null)
      for (int i=0;i<cc.length;i++)
          recipientList.add(cc[i]);

      if (bcc!=null)
      for (int i=0;i<bcc.length;i++)
          recipientList.add(bcc[i]);

      if (news!=null)
      for (int i=0;i<news.length;i++)
          recipientList.add(news[i]);

      return (String[])recipientList.toArray(to);

  }

  public String[] getFromList() {
     String[] addresses = null;
      try {
  	    addresses = getHeader("From");

  	    if (addresses!=null) {
  	        for (int i = 0; i<addresses.length;i++)
  	            addresses[i] = stripGarbage(addresses[i]).trim();
  	    }

  	} catch (Exception me) {
  	    me.printStackTrace();
  		logger.error("failed to collate email address info. Cause:"+me.toString());
	}
  	 return addresses;
  }



  private String getHeader(Message.RecipientType type)
       throws MessagingException
     {
       if (type==Message.RecipientType.TO)
         return "To";
       if (type==Message.RecipientType.CC)
         return "Cc";
       if (type==Message.RecipientType.BCC)
         return "Bcc";
       if (type==RecipientType.NEWSGROUPS)
         return "Newsgroups";
       throw new MessagingException("Invalid recipient type");
     }

  public String getToAddresses() {
    return getAddresses(Message.RecipientType.TO);
  }

  public String getBCCAddresses() {
    return getAddresses(Message.RecipientType.BCC);
  }

  public String getCCAddresses() {
    return getAddresses(Message.RecipientType.CC);
  }

  public String getFromAddress() {

    String fromName = "";
    try {
        String[] fromList = getFromList();
        if (fromList!=null)
            fromName = fromList[0];
    }
    catch(Exception ex) {
    }
    return fromName;
  }

  public String getFriendlySentDate() {
    String date = "";
    try {
         date = getSentDate().toString();
    }
    catch(Exception ex) {
    }
    return date;
  }

  public String getFriendlySize()
  {
  	if (content!=null)
  		return Integer.toString(content.length / 1024)+"k";
  	else
  		return Integer.toString(size / 1024)+"k";
  }

  public int getSize( ){
      if (content!=null)
    		return content.length;
    	else
    		return size;
  }

  public String getFriendlyFlags() throws MessagingException {

      String flagStr = "";
      Flags flags = getFlags();
      Flags.Flag[] sf = flags.getSystemFlags();
      for (int i = 0; i < sf.length; i++) {

     	if (sf[i] == Flags.Flag.DELETED)
             flagStr += "deleted, ";
     	else if (sf[i] == Flags.Flag.SEEN)
     	     flagStr += "seen, ";
     	else if (sf[i] == Flags.Flag.ANSWERED)
     	     flagStr += "answered, ";
     	else if (sf[i] == Flags.Flag.FLAGGED)
	    	 flagStr += "flagged, ";
     	else if (sf[i] == Flags.Flag.RECENT)
	    	 flagStr += "recent, ";
     	else if (sf[i] == Flags.Flag.DRAFT)
	    	 flagStr += "draft, ";
      }
      if (flagStr.length()<1)
          return "";

      flagStr = flagStr.trim().substring(0,flagStr.length()-1);
      return flagStr;
   }

  public String getPriority() throws MessagingException {

      String priority[] = getHeader("X-Priority");
      if (priority!=null && priority.length>0) {
          String p = priority[0].toLowerCase();
          if (priority[0].indexOf("1")!=-1 ||
              priority[0].indexOf("highest")!=-1)
              return "highest";
          if (priority[0].indexOf("2")!=-1 ||
              priority[0].indexOf("high")!=-1)
              return "highest";
          if (priority[0].indexOf("4")!=-1 ||
              priority[0].indexOf("low")!=-1)
              return "low";
          if (priority[0].indexOf("5")!=-1 ||
              priority[0].indexOf("lowest")!=-1)
              return "lowest";
      }
      return "";
  }


  public String getInternetHeaders() throws MessagingException {
      String headerOut = "";
      Enumeration headers = getAllHeaders();
      while (headers.hasMoreElements()) {
          Header header = (Header)headers.nextElement();
          if (!header.getName().equalsIgnoreCase("MIME-Version") &&
              !header.getName().equalsIgnoreCase("X-mailer"))
              	headerOut += header.getName()+": "+header.getValue()+"<br>";
      }
      return headerOut;
  }

  public static String stripGarbage(String s) {
    String good = " abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789<>@,;./=:";
    String result = "";
    for ( int i = 0; i < s.length(); i++ ) {
        if ( good.indexOf(s.charAt(i)) >= 0 )
           result += s.charAt(i);
        }
    return result;
   }

  public InputStream getPlainTextInputStream() {
  	InputStream is = null;
  	StringBuffer textBuffer = new StringBuffer();
  	return is;
  }

  public void setEmailID(EmailID emailId) {
      this.emailId = emailId;
  }

  public EmailID getEmailID() {
      return emailId;
  }
  
  public String getSubject() throws MessagingException {
      if (super.getSubject()==null)
          return "<no subject>";
      if (super.getSubject().length()<1)
          return "<no subject>";
      return super.getSubject();
  }

  public String toString() {
      String subject = "";
      try { subject=getSubject(); } catch (Exception e) {}
      return emailId+",from='"+getFromAddress()+"',subject='"+subject+"',to='"+getToAddresses()+"'";
  }
}