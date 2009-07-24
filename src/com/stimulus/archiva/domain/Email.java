
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
import java.text.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import org.apache.commons.logging.*;
import javax.mail.*;
import javax.mail.internet.*;
import org.apache.commons.logging.*;
import com.stimulus.util.*;
import com.stimulus.archiva.domain.fields.*;

public class Email extends MimeMessage implements Serializable  {
  
  private static final long serialVersionUID = 742813048326535L;
  protected static Log logger = LogFactory.getLog(EmailField.class.getName());
  protected static Session session;
  protected byte[] compressedOriginal;
  protected int size;
  protected int volumeIndex;
  protected boolean selected = false;
  public enum DisplayMode { ALL, EMAIL_ONLY, NAME_ONLY };
  protected LinkedHashMap<String,EmailFieldValue> applicationFields = new LinkedHashMap<String,EmailFieldValue>();
  protected EmailID emailId;
  
  static
  {
	  Properties system_properties = System.getProperties();
	  session = Session.getDefaultInstance(system_properties,null);
	  setSession(session);
  };
  
  public static Session getSession() { return session; }
  
  public static void setSession(Session newSession) {
	 session = newSession;
  }
  
  public Email() {
	  super(session);
  }
  public Email(EmailID emailId, InputStream is) throws MessagingException {
	  super(session,is);
	  this.emailId = emailId;
	  compileApplicationFields();
  }
  
/*
  public Email(InputStream is, boolean headersOnly) throws MessagingException
  {
  	 super(session,is);
  	 try { compileApplicationFields(); } catch (Exception e) {}
 	 if (headersOnly) clearContent();

  }

  public Email(InputStream is, boolean headersOnly, EmailID emailId) throws MessagingException {
  	super(session,is);
  	try { compileApplicationFields(); } catch (Exception e) {}
  	if (headersOnly) clearContent();
  	this.emailId = emailId;
  }

  public Email() throws MessagingException
  {
  	super(session);
  }*/
  
  private void clearContent() {
  	 size = content.length;
  	 content=null;
  }

  public LinkedHashMap<String,EmailFieldValue> getFields() {
	  return applicationFields;
  }
  
  public static String[] getAvailableFields() {
	  return new String [] {"to","bcc","cc","from","size","flag","priority","subject","deliveredto","attach"};
  }
  
  public void compileApplicationFields() throws MessagingException {
	  applicationFields.clear();
	  
	 // if (getFromAddress(DisplayMode.ALL).length()<1)
	//	  throw new MessagingException("badly formed message");
	  putField("from", getFromAddress(DisplayMode.ALL));
	  putField("to", getRecipientAddresses(Message.RecipientType.TO,DisplayMode.ALL));
	  putField("bcc", getRecipientAddresses(Message.RecipientType.BCC,DisplayMode.ALL));
	  putField("cc", getRecipientAddresses(Message.RecipientType.CC,DisplayMode.ALL));
	  putField("flag", getFlagsStr());
	  putField("priority",Integer.toString(getPriorityID()));
	  putField("sensitivity",getSensitivity());
	  String subject = getSubject(); 
	  if (subject.length()>0)
		  putField("subject",subject);
	  else
		  putField("subject","<no subject>");
	  addHeaderToField("deliveredto","Delivered-To");
	  putField("attach", hasAttachment() ? "1" : "0");  
	
	  updateOriginalSize();
	  
	  try {
		  Date sentDate = getSentDate();
		  if (sentDate!=null)
			  putField("sentdate","d"+DateUtil.convertDatetoString(sentDate));
	  } catch (Exception e) {
		  logger.error("failed to set sent date on message. using current date.");
	  }  
	
	  try {
		  Date archiveDate = getArchiveDate();
		  if (archiveDate!=null)
			  putField("archivedate","d"+DateUtil.convertDatetoString(archiveDate)); // fix this
	  } catch (Exception e) {
		  logger.error("failed to set archive date on message. using current date.");
	  }  
	  
	  try {
		  Date receivedDate = getReceivedDate();
		  if (receivedDate!=null)
			  putField("receiveddate","d"+DateUtil.convertDatetoString(receivedDate)); // fix this
	  } catch (Exception e) {
		  logger.error("failed to set received date on message. using current date.");
	  }  

	/*  try {
		  Date receiveDate = getReceivedDate();
		  if (receiveDate!=null)
			putField("receivedate","d"+format.format(receiveDate)); // fix this
	  } catch (Exception e) {
		  logger.error("failed to set archive date on message. using current date.");
	  }  */
	
  }
 
  protected void updateOriginalSize() { // bytes
	  double sz = -1;
	  try {
		  String size[] = getHeader("X-MailArchiva-Message-Size");
		  if (size!=null && size.length>0)  {
			  logger.debug("getOriginalSize() found size {size='"+size[0]+"'}");
			  try {
				  sz = Double.parseDouble(size[0]);
			  } catch (NumberFormatException fe) {
				  sz = Integer.parseInt(size[0]); 
			  } catch (Exception e) {}
		  } 
		  this.removeHeader("X-MailArchiva-Message-Size");
	  } catch (Exception e) {}
		  
	  try {
		if (sz==-1)
			sz = getSize(); 
	  } catch (Exception me) {
		  sz = 0;
	  }
	  DecimalFormat df = new DecimalFormat("0.##");
	  putField("size",df.format(sz / 1024.0));
  }
  
  protected void addHeaderToField(String specialHeaderName, String headerName) {
	  try {
		  String[] headerValue = getHeader(headerName);
		  if (headerValue!=null && headerValue.length>0) {
			  StringBuffer buffer = new StringBuffer();
			  for (int i=0; i<headerValue.length; i++) {
				  buffer.append(headerValue[i]);
				  buffer.append(",");
			  }
			  String header = buffer.toString();
			  if (header.endsWith(Character.toString(',')))
		            header = header.substring(0,header.length()-1);
			  
			  putField(specialHeaderName,header);
		  }
	  } catch (Exception e) { }
  }
  
	
  protected String getAddresses(Address[] recipients, DisplayMode displayMode) {
	  if (recipients == null)
		  return "";
	  StringBuffer result = new StringBuffer();

	  
	  for (int i=0;i<recipients.length; i++) {
		  if (recipients[i] instanceof InternetAddress) {
			  InternetAddress address = (InternetAddress)recipients[i];
			  switch (displayMode) {
			  	case ALL: if (address.getPersonal() != null) 
			  					result.append(address.getPersonal());
	  					  result.append(" <");
	  					  result.append(DecodingUtil.decodeWord(address.getAddress()));
	  					  result.append(">");
	  					  break;
			  	case EMAIL_ONLY: result.append(DecodingUtil.decodeWord(address.getAddress())); break;
			  	case NAME_ONLY: if (address.getPersonal() != null) 
			  							result.append(DecodingUtil.decodeWord(address.getPersonal()));
			  						else 
			  							result.append(DecodingUtil.decodeWord(address.getAddress())); 
			  					break;
			  }
		  } else if (recipients[i] instanceof NewsAddress) {
			  result.append("newsgroup:");
			  result.append(((NewsAddress)recipients[i]).getNewsgroup());
		
		  } else
			  result.append(DecodingUtil.decodeWord(recipients[i].toString()));
		  if (i<recipients.length-1) 
			  result.append(", ");
	  }
	  return result.toString().trim();
  }
  
  protected String getRecipientAddresses(Message.RecipientType recipientType, DisplayMode displayMode) {
	  Address[] recipients = null;
	  try {
		  recipients = getRecipients(recipientType);
	  } catch (Exception e) { return ""; }
	  return getAddresses(recipients,displayMode);
  }
 
  protected String getFromAddress(DisplayMode displayMode) {
	  Address[] from = null;
	  try {
		  from = getFrom();
	  } catch (Exception e) { return ""; }
	  return getAddresses(from, displayMode);
  }
 

  protected int getPriorityID() {
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
 
  
  public String getSensitivity() {
	  try {
          String sensitivities[] = getHeader("Sensitivity");
          if (sensitivities!=null && sensitivities.length>0) {
        	  return sensitivities[0];
          }
	  } catch (javax.mail.MessagingException me) {
          logger.debug("failed to retrieve sensitivity {"+toString()+"}",me);
      }
	  return null;
  }
  
  protected String getIHeaders(boolean journal,boolean showHidden) throws MessagingException, IOException {
	  StringBuffer headerOut = new StringBuffer();
      Enumeration headers = null;
      headers = getAllHeaders();
      
      while (headers.hasMoreElements()) {
          Header header = (Header)headers.nextElement();
		  headerOut.append(header.getName());
	      headerOut.append(": ");
	  	  headerOut.append(header.getValue());
	  	  headerOut.append("\n");
      }
      return headerOut.toString();
  }
  
  public String getInternetHeaders(boolean showHidden) throws MessagingException, IOException {
      return getIHeaders(true,showHidden);
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
  
  protected boolean hasAttachment() 
  {

	  try {
		  boolean result = hasAttachment(this);
		  logger.debug("hasAttachment() {result='"+result+"'}");
		  return result;
	  } catch (Exception e) {
		  logger.error("failed to determine if message has attachment {"+toString()+"}",e);
		 
	  }
	  logger.debug("hasAttachment() {result='false'}");
	  return false;
	  //logger.debug("message attachment discovery {attach='"+hasAttachment+"',"+toString());
  }

  
  protected boolean hasAttachment(Part p) throws MessagingException, IOException {
	 
	  if (p.getDisposition()!=null && Compare.equalsIgnoreCase(p.getDisposition(),Part.ATTACHMENT)) {
		  logger.debug("hasAttachment() attachment disposition.");
		  return true;
	  } 
	  if (p.getFileName()!=null) {
		  logger.debug("hasAttachment() filename specified.");
	    	return true;
	  }
	  
	  try {	 
			 if (p.isMimeType("multipart/*")) {
			     Multipart mp = (Multipart)p.getContent();
			     for (int i = 0; i < mp.getCount(); i++) {
			    	 logger.debug("hasAttachment() scanning multipart["+i+"]");
			    	 if (hasAttachment(mp.getBodyPart(i)))
			    		 return true;
			     }
			 } else if (p.isMimeType("message/rfc822")) {
				    	return hasAttachment((Part)p.getContent());
			 }
	  } catch (Exception e) {
			 logger.debug("exception occurred while detecting attachment",e);
	  }
	  return false;
  }
	
  public String toString() {
      StringBuffer outStr = new StringBuffer(); //= emailId.toString();
      
      if (getEmailId()!=null) {
    	  outStr.append(getEmailId().toString());
    	  outStr.append(",");
      }
      for (Iterator it=getFields().values().iterator(); it.hasNext(); ) {
 	        EmailFieldValue efv = (EmailFieldValue)it.next();
 	        outStr.append(efv);
 	        outStr.append(","); 	  
 	  }
      outStr.setLength(outStr.length()-1); 
      return outStr.toString();
  }
  public EmailID getEmailId() { return emailId; }
  
  protected String getFlagsStr() {
	  List<String> flags = getFlagList();
	  StringBuffer allFlags = new StringBuffer();
	  for (String flag : flags) {
		  allFlags.append(flag);
		  allFlags.append(" ");
	  }
	  return allFlags.toString().trim();
  }
  
  protected List<String> getFlagList() {
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
          logger.debug("failed to retreive flag from message {"+toString()+"}");
      } catch (NullPointerException re) {}
      return flagList;
   }

  
	  
	  protected void putField(String field, String value) {
		
			if (value==null || value.length()<1 || field==null) 
				return;
			field = field.trim().toLowerCase(Locale.ENGLISH);
			
			EmailFields emailFields = Config.getConfig().getEmailFields();
			if (emailFields.get(field)==null) // field not supported
				return;
			
			EmailFieldValue existingValue = (EmailFieldValue)applicationFields.get(field);
			if (existingValue==null) 
				applicationFields.put(field,new EmailFieldValue(emailFields.get(field),value));
			else {
				String oldValue = existingValue.getValue();
				if (oldValue!=null && oldValue.indexOf(value)==-1)
					applicationFields.put(field,new EmailFieldValue(emailFields.get(field),oldValue + ", " + value));   	
			}
		
	}
	
	 
   public Date getArchiveDate() {
	  try {
		   String[] header = getHeader("X-MailArchiva-Archive-Date");
		   if (header!=null && header.length>0) {
			   return DateUtil.convertStringToDate(header[0]);
		   } else if (emailId!=null && emailId.getUniqueID()!=null) { // legacy
			   String uniqueid = emailId.getUniqueID();
			   try {
				   return DateUtil.convertStringToDate(uniqueid);
			   } catch (Exception e) {}
			   
		   } 
	  } catch (Exception me) {
		  logger.debug("getArchiveDate(). unable to parse archive date ",me);
	  }
	  return new Date();
   }
   
   
   public Date getReceivedDate() {
	   MailDateFormat mdf = new MailDateFormat();
	  
	   try {
		   
		 /*  Enumeration e = getAllHeaders();
		   while (e.hasMoreElements()) {
			   Header h = (Header)e.nextElement();
			   System.out.println(h.getName()+":"+h.getValue());
		   }*/
		   String[] header = getHeader("Received");
		   if (header==null || header.length<1) {
			   return null;
		   }
		   for (int i=0; i<header.length; i++) {
			   int received = header[i].lastIndexOf(';');
			   if (received>=0) {
				   String dateStr = header[i].substring(received+1).trim();
				   Date date = mdf.parse(dateStr,new ParsePosition(0));
				   return date;
			   }
		   }
	   } catch (Exception re) {
		   logger.debug("getReceivedDate(). unable to parse received date", re);
	   }
	   return null;
   }	
}
