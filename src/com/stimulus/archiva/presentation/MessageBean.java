
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.struts.upload.FormFile;

import com.stimulus.archiva.domain.Email;
import com.stimulus.archiva.domain.fields.EmailField;
import com.stimulus.archiva.domain.fields.EmailFieldValue;
import com.stimulus.archiva.exception.ArchivaException;
import com.stimulus.archiva.extraction.MessageExtraction;
import com.stimulus.archiva.service.MessageService;
import com.stimulus.struts.ActionContext;
import com.stimulus.struts.BaseBean;
import com.stimulus.util.EscapeUtil;

public class MessageBean extends BaseBean implements Serializable {

  private static final long serialVersionUID = 1624887450703706628L;
  protected static Logger logger = Logger.getLogger(MessageBean.class.getName());
  protected static MessageService messageService = null;

  protected static final Logger audit = Logger.getLogger("com.stimulus.archiva.audit");

  protected FormFile 			file;
  protected String 				fileName;
  protected String 				filePath;
  protected Email 				email = null;
  protected String 				messageId = null;
  protected int					volumeIndex = 0;
  protected String 				viewMessageURL = null;
  protected String      		attachment = null;
  protected MessageExtraction 	messageExtraction = null;
  protected final Lock 			lock = new ReentrantLock();
  protected final Condition 	extracted  = lock.newCondition(); 
  protected String				volumeID = null;
  
  /* Constructors */

  public MessageBean() {
  }

  public void setVolumeID(String volumeID) {
	  logger.debug("setVolumeID(){volumeID='"+volumeID+"'}");
	  this.volumeID = volumeID;
	  
  }
  public void setMessageID(String messageId) {
  	logger.debug("setMessageID(){messageID='"+messageId+"'}");
  	this.messageId = messageId;
  }

  public void setVolumeIndex(int volumeIndex) {
  	logger.debug("volumeIndex() {volumeindex='"+volumeIndex+"'}");
  	this.volumeIndex = volumeIndex;
  }


  /* JavaBeans Properties */

  public String getFileName() { logger.debug("getFileName() {filename='"+fileName+"'}"); return fileName; }
  public String getFilePath() { logger.debug("getFileName() {file path='"+filePath+"'}"); return filePath; }

  public void setFileName(String fileName) { logger.debug("setFileName() {file name='"+fileName+"'}"); this.fileName = fileName; }
  public void setFilePath(String filePath) { logger.debug("setFileName() {file name='"+filePath+"'}"); this.filePath = filePath; }

  public void setFile(FormFile file) { logger.debug("setFile()"); this.file = file; }
  public FormFile getFile() { logger.debug("getFile()"); return file; }


  public String viewattachment() {
	  logger.debug("viewattachment()");
	  if (attachment==null) {
	  		logger.error("There was an error retrieving your message for viewing. cause: message id is null");
	  		return "reload";
	  }
	  Email email = null;
	  
	
	  try { 
		  email = new Email(null,messageExtraction.getAttachment(attachment).getInputStream());
	  	 logger.debug("retrieved message attachment successfully {"+email+"}");	
	  } catch(Exception me) {
	  		String errorStr = "failed to retrieve the requested message attachment.";
			ActionContext.getActionContext().setSimpleMessage(errorStr);
			logger.error(errorStr,me);
			return "reload";
	  }
	  viewMessage(email,false);
	  return "success";
	  
  }
  public String viewmail()
  {

  	logger.debug("viewmail()");
  	if (messageId==null) {
  		logger.error("There was an error retrieving your message for viewing. cause: message id is null");
  		return "reload";
  	}
  	Email email = null;
  	try {
  
  		email = MessageService.getMessageByID(volumeID,messageId,false);
  		logger.debug("retrieved message successfully {"+email+"}");
  	} catch(Exception me) {
  		String errorStr = "Failed to retrieve the requested message. "+me.getMessage();
		ActionContext.getActionContext().setSimpleMessage(errorStr);
		logger.error(errorStr);
		return "reload";
  	}
  	viewMessage(email,true);
  	
  	return "success";
  }

  protected void viewMessage(Email email, boolean isOriginalMessage) {
	  try {
		  	this.email = email;
	  		logger.info("user viewing message {"+email+"}");
	  		String remoteIP="unknown"; // FIX
	  		String userName="unknown";
	  		audit.info("view email {"+email+",ip='"+remoteIP+"',uname='"+userName+"'}");

	  		//deleteExtractedMessage();
	  		HttpServletRequest hsr = ActionContext.getActionContext().getRequest();
	  		String baseURL = hsr.getRequestURL().substring(0,hsr.getRequestURL().lastIndexOf(hsr.getServletPath()));
	  		messageExtraction = null; // can take a while to extract message
	  		lock.lock();
	  	    try {
	  	    	messageExtraction = MessageService.extractMessage(email, baseURL,isOriginalMessage);
	  	    	extracted.signal();
	  	    } finally {
	  	       lock.unlock();
	  	    }
	  	} catch (Exception e) {
	  		
	  	    logger.error("failed to extract message",e);
	  	}
  }
  
  public void waitForExtraction() {
	 lock.lock();
     try {
       while (messageExtraction == null) 
    	   extracted.await();
     } catch (Exception e) {} finally {
       lock.unlock();
     }
  }
  
  public List getAttachments() {
	waitForExtraction();
    List attachments =  null;
  	attachments = messageExtraction.getAttachments();
  	return attachments;
  }

  public boolean getHasAttachment() {
	  List attachments = getAttachments();
	  if (attachments!=null)
		  return attachments.size()>0;
	  else
		  return false;
  }
  public String getOriginalMessageFilePath() throws ArchivaException {
	 return getMessageExtraction().getFilePath();
  }
  
  public String getOriginalMessageFileName() throws ArchivaException {
	  return getMessageExtraction().getFileName();
  }

  public String getOriginalMessageFileSize() throws ArchivaException {
	  return getMessageExtraction().getFileSize();
  }
  public String getOriginalMessageURL() throws ArchivaException {
	  return getMessageExtraction().getFileURL();
  }
  
  public String getView() throws ArchivaException {
	  return getMessageExtraction().getViewURL();
  }
  
  public void setAttachment(String attachment) {
      this.attachment = attachment;
  }

  public String getAttachment() {
      return attachment;
  }
  
  public String getAttachmentFilePath() {
	  return getMessageExtraction().getAttachment(attachment).getFilePath();
  }
  
  public String getMessageId() {
	  return messageId;
  }
  
  public String getVolumeID() {
	  return volumeID;
  }
  
  
  public MessageExtraction getMessageExtraction() {
	  waitForExtraction();
	  return messageExtraction;
  }
  
	 public List<EmailField> getFields() {
		 ArrayList<EmailField>  list = new ArrayList<EmailField>();
		 Iterator i = EmailField.getAvailableFields().values().iterator();
		 while (i.hasNext()) {
			 EmailField ef = (EmailField)i.next();
			 
			 // we dont allow end-users to view bcc or delivered-to flags
			  if (ef.getName().equals("bcc") && getMailArchivaPrincipal().getRole().equals("user"))
				  continue;
			  if (ef.getName().equals("deliveredto") && getMailArchivaPrincipal().getRole().equals("user"))
				  continue;
			  
			  if (ef.getViewEmail()==EmailField.AllowViewMail.VIEWMAIL) {
				 list.add(ef);	
			  }
		 }
		 return list;
	 }
	 
	  
	 public List<DisplayField> getFieldValues() {
		 ArrayList<DisplayField>  list = new ArrayList<DisplayField>();
		 Iterator i = email.getFields().iterateValues();
		 while (i.hasNext()) {
			 EmailFieldValue efv = (EmailFieldValue)i.next();
			 
//			 we dont allow end-users to view bcc or delivered-to flags
			  if (efv.getField().getName().equals("bcc") && getMailArchivaPrincipal().getRole().equals("user"))
				  continue;
			  if (efv.getField().getName().equals("deliveredto") && getMailArchivaPrincipal().getRole().equals("user"))
				  continue;
			  
			 if (efv.getField().getViewEmail()==EmailField.AllowViewMail.VIEWMAIL) {
				 list.add(DisplayField.getDisplayField(efv, getLocale(),true));
			 }
		 }
		 return list;
	 }
    
   

	public String getJournalReport() {
		String report = "";
        try {
        	 boolean showHidden = !getMailArchivaPrincipal().getRole().equals("user");
             report= email.getJournalReport(showHidden);
        }
        catch(Exception ex) {
        	logger.error("failed to retrieve journal report from email {"+email.toString()+"}");
        }
    	return EscapeUtil.forHTML(report).replace("\n","<br>");
	}
	
	public boolean getJournalMessage() {
		try {
			return email.isJournalMessage();
		}
        catch(Exception ex) {
        	logger.error("failed to establish whether email is a journalled message {"+email.toString()+"}");
        	return false;
        }
	}
	
	
    public String getInternetHeaders() {
    	String headers = "";
        try {
        	 boolean showHidden = !getMailArchivaPrincipal().getRole().equals("user");
             headers = email.getInternetHeaders(showHidden);
        }
        catch(Exception ex) {
        	logger.error("failed to retrieve internet headers from email {"+email.toString()+"}");
        }
    	return EscapeUtil.forHTML(headers).replace("\n","<br>");
    
    }
    
    protected Email getEmail() {
    	return email;
    }
    
    
}


