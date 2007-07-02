
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
package com.stimulus.archiva.domain;


import java.util.*;
import java.io.*;
import javax.mail.*;
import javax.mail.internet.*;
import org.apache.log4j.*;
import org.apache.lucene.document.Document;
import com.stimulus.util.*;
public class Email extends MimeMessage {
  
  private static final long serialVersionUID = 742813048326535L;
  protected static Logger logger = Logger.getLogger(Email.class.getName());
  protected static Session session;
  protected byte[] compressedOriginal;
  protected int size;
  protected int volumeIndex;
  protected boolean selected = false;
  protected EmailID emailId = null;
  protected boolean hasAttachment = false;
  public enum DisplayMode { ALL, EMAIL_ONLY, NAME_ONLY };
  
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
  	 try { initAttachment(); } catch (Exception e) {}
 	 if (headersOnly) clearContent();

  }

  public Email(InputStream is, boolean headersOnly, EmailID emailId) throws MessagingException {
  	super(session,is);
  	try { initAttachment(); } catch (Exception e) {}
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

  public String getAddresses(Address[] recipients, DisplayMode displayMode) {
	  if (recipients == null)
		  return "";
	  String result = "";
	  for (int i=0;i<recipients.length; i++) {
		  if (recipients[i] instanceof InternetAddress) {
			  InternetAddress address = (InternetAddress)recipients[i];
			  switch (displayMode) {
			  	case ALL: if (address.getPersonal() != null)
			  					result += DecodingUtil.decodeWord(address.getPersonal())
			  						    + " <"+DecodingUtil.decodeWord(address.getAddress())+">"; 
			  				else
			  					result += " <"+DecodingUtil.decodeWord(address.getAddress())+">";
			  				break;
			  	case EMAIL_ONLY: result += DecodingUtil.decodeWord(address.getAddress()); break;
			  	case NAME_ONLY: if (address.getPersonal() != null) 
			  							result += DecodingUtil.decodeWord(address.getPersonal());
			  						else 
			  							result += DecodingUtil.decodeWord(address.getAddress()); 
			  					break;
			  }
		  } else if (recipients[i] instanceof NewsAddress) {
			  result += "newsgroup:" + ((NewsAddress)recipients[i]).getNewsgroup();
		  } else
			  result += DecodingUtil.decodeWord(recipients[i].toString());
		  if (i<recipients.length-1) 
			  result += ", ";
	  }
	  return result.trim();
  }
  
  public String getRecipientAddresses(Message.RecipientType recipientType, DisplayMode displayMode) {
	  Address[] recipients = null;
	  try {
		  recipients = getRecipients(recipientType);
	  } catch (Exception e) { return ""; }
	  return getAddresses(recipients,displayMode);
  }
  public String getToAddresses(DisplayMode displayMode) {
    return getRecipientAddresses(Message.RecipientType.TO,displayMode);
  }

  public String getBCCAddresses(DisplayMode displayMode) {
    return getRecipientAddresses(Message.RecipientType.BCC,displayMode);
  }

  public String getCCAddresses(DisplayMode displayMode) {
    return getRecipientAddresses(Message.RecipientType.CC,displayMode);
  }

  public String getFromAddress(DisplayMode displayMode) {
	  Address[] from = null;
	  try {
		  from = getFrom();
	  } catch (Exception e) { return ""; }
	  return getAddresses(from, displayMode);
  }
 
  public int getSize( ){
      if (content!=null)
    		return content.length;
    	else
    		return size;
  }

  public int getPriorityID() {
      try {
          String priority[] = getHeader("X-Priority");
          if (priority!=null && priority.length>0) {
              if (priority[0].indexOf("1")!=-1 ||
                  priority[0].indexOf("highest")!=-1)
                   return 1;
              if (priority[0].indexOf("2")!=-1 ||
                  priority[0].indexOf("high")!=-1)
                   return 2;
              if (priority[0].indexOf("3")!=-1 ||
                  priority[0].indexOf("normal")!=-1)
                   return 3;
              if (priority[0].indexOf("4")!=-1 ||
                  priority[0].indexOf("low")!=-1)
                   return 4;
              if (priority[0].indexOf("5")!=-1 ||
                  priority[0].indexOf("lowest")!=-1)
                   return 5;
          }
      } catch (javax.mail.MessagingException me) {
          logger.debug("failed to retreive priority from message {"+toString()+"}",me);
      }
      return 3;
  }
  
  public String getPriority() {
      int priority = getPriorityID();
      switch(priority) {
          case 1: return "highest";
          case 2: return "high";
          case 3: return "normal";
          case 4: return "low";
          case 5: return "lowest";
          default: return "normal";
      }
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
  	return is;
  }

  public void setEmailID(EmailID emailId) {
      this.emailId = emailId;
  }

  public EmailID getEmailID() {
      return emailId;
  }
  
  public String getSubject() {
      try {
		  if (super.getSubject()==null || super.getSubject().length()<1)
			  return "";
		  else
			  return super.getSubject();
      } catch (Exception e) { return ""; }
  }
  
  protected void initAttachment()throws MessagingException,IOException
  {
	  try {
		  hasAttachment = handlePart(getContent());
	  } catch (Exception e) {
		  logger.error("failed to determine if message has attachment {"+toString()+"}",e);
		  hasAttachment = false;
	  }
	  logger.debug("message attachment discovery {attach='"+hasAttachment+"',"+toString());
  }

	  public static boolean handlePart(Object part) throws MessagingException, IOException {
		
			if (part instanceof Multipart) {
				Multipart multipart = (Multipart)part;
				for (int i=0, n=multipart.getCount(); i<n; i++) 
					if (handlePart(multipart.getBodyPart(i))) 
						return true;
			} else if (part instanceof MimeMessage)
				handlePart(((MimeMessage)part).getContent());
			else if (part instanceof Part) {
				String disposition = ((Part)part).getDisposition();
				if (disposition != null) { // When just body
				  if (disposition.equalsIgnoreCase(Part.ATTACHMENT)) 
					 return true;
				}
			}
			return false;
	  }
			
			
			
			
  public boolean hasAttachment() {
	  return hasAttachment;
   }
	  

  public String toString() {
      String subject = "";
      try { subject=getSubject(); } catch (Exception e) {}
      return emailId+",from='"+getFromAddress(Email.DisplayMode.EMAIL_ONLY)+"',subject='"+subject+"',to='"+getToAddresses(Email.DisplayMode.EMAIL_ONLY)+"'";
  }
  
  public List<String> getFlagList() {
	  List<String> flagList = new LinkedList<String>();
	  try {
          Flags flags = getFlags();
          Flags.Flag[] sf = flags.getSystemFlags();
          for (int i = 0; i < sf.length; i++) {
    
         	if (sf[i] == Flags.Flag.DELETED)
         		flagList.add("deleted");
         	else if (sf[i] == Flags.Flag.SEEN)
         		flagList.add("seen");
         	else if (sf[i] == Flags.Flag.ANSWERED)
         		flagList.add("answered");
         	else if (sf[i] == Flags.Flag.FLAGGED)
         		flagList.add("flagged");
         	else if (sf[i] == Flags.Flag.RECENT)
         		flagList.add("recent");
         	else if (sf[i] == Flags.Flag.DRAFT)
         		flagList.add("draft");
          }
          
      } catch (MessagingException me) {
          logger.debug("failed to retreive priority from message {"+toString()+"}");
      }
      return flagList;
   }

  
  
 
}