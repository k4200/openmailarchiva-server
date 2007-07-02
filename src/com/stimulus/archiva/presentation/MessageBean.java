
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

import org.apache.log4j.Logger;
import org.apache.struts.actions.DownloadAction.FileStreamInfo;
import org.apache.struts.upload.FormFile;
import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.service.*;
import com.stimulus.archiva.extraction.*;

import com.stimulus.struts.*;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import com.stimulus.archiva.exception.*;
import java.io.*;

public class MessageBean extends BaseBean {

  private static final long serialVersionUID = 1624887450703706628L;
  protected static final Logger logger = Logger.getLogger(MessageBean.class.getName());
  protected static MessageService messageService = null;

  protected static final Logger audit = Logger.getLogger("com.stimulus.archiva.audit");

  protected FormFile 	file;
  protected String 		fileName;
  protected String 		filePath;
  protected EmailBean 	message = null;
  protected String 		messageId = null;
  protected int			volumeIndex = 0;
  protected String 		viewMessageURL = null;
  protected String      attachment = null;
  protected MessageExtraction messageExtraction = null;
  
  /* Constructors */

  public MessageBean() {
  }


  public void setMessageID(String messageId) {
  	logger.debug("setMessageID(){messageid='"+messageId+"'}");
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

  public EmailBean getMessage() {
  	try
	{
  		logger.debug("getMessage() {"+message+"}");
	} catch (Exception e) { }
    return message;
  }

  public String viewattachment() {
	  logger.debug("viewattachment()");
	  if (attachment==null) {
	  		logger.error("There was an error retrieving your message for viewing. cause: message id is null");
	  		return "reload";
	  }
	  Email email = null;
	  try { 
		  InputStream is = messageExtraction.getAttachment(attachment).getInputStream();
		  email = new Email(is, false);
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
  		email = MessageService.getMessageByID(new EmailID(messageId),false);
  		logger.debug("retrieved message successfully {"+email+"}");
  	} catch(Exception me) {
  		String errorStr = "Failed to retrieve the requested message. cause:"+me.getMessage();
		ActionContext.getActionContext().setSimpleMessage(errorStr);
		logger.error(errorStr);
		return "reload";
  	}
  	viewMessage(email,true);
  	return "success";
  }

  protected void viewMessage(Email email, boolean isOriginalMessage) {
	  try {
		  	message = new EmailBean(email);
	  		message.setServlet(getServlet());
	  		logger.info("user viewing message {"+message+"}");
	  		String remoteIP="unknown"; // FIX
	  		String userName="unknown";
	  		audit.info("view email {"+message+",ip='"+remoteIP+"',uname='"+userName+"'}");

	  		//deleteExtractedMessage();
	  		HttpServletRequest hsr = ActionContext.getActionContext().getRequest();
	  		String baseURL = hsr.getRequestURL().substring(0,hsr.getRequestURL().lastIndexOf(hsr.getServletPath()));
	  		String filePath;
	  		messageExtraction = MessageService.extractMessage(message.getEmail(), baseURL,isOriginalMessage);
	  		
	  	} catch (Exception e) {
	  		
	  	    logger.error("failed to extract message",e);
	  	}
  }
  
  public List getAttachments() {
    List attachments =  null;
  	attachments = messageExtraction.getAttachments();
  	return attachments;
  }

  public String getOriginalMessageFilePath() throws ArchivaException {
	 return messageExtraction.getFilePath();
  }
  
  public String getOriginalMessageFileName() throws ArchivaException {
	  return messageExtraction.getFileName();
  }

  public String getOriginalMessageFileSize() throws ArchivaException {
	  return messageExtraction.getFileSize();
  }
  public String getOriginalMessageURL() throws ArchivaException {
	  return messageExtraction.getFileURL();
  }
  
  public String getView() throws ArchivaException {
	  return messageExtraction.getViewURL();
  }
  
  public void setAttachment(String attachment) {
      this.attachment = attachment;
  }

  public String getAttachment() {
      return attachment;
  }
  
  public String getAttachmentFilePath() {
	  return messageExtraction.getAttachment(attachment).getFilePath();
  }
  
  public String getMessageId() {
	  return messageId;
  }
  
  public MessageExtraction getMessageExtraction() {
	  return messageExtraction;
  }
/*
  private void deleteExtractedMessage() {
  	try {
	  	if (viewMessageURL!=null)
	  		MessageService.deleteExtractedMessage(messageId);
  	} catch (ArchivaException me) {
  		logger.error("failed to delete extracted message {messageId='"+messageId+"'}",me);
  	}
  }*/
  /*
  protected void finalize() {
  	try {
	  	if (messageId!=null)
	  		MessageService.deleteExtractedMessage(messageId);
  	} catch (ArchivaException me) {
  		logger.error("failed to delete extracted message {messageId='"+messageId+"'}",me);
  	}
  }*/


}
