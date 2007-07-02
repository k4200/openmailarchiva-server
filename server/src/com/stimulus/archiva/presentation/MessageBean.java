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
package com.stimulus.archiva.presentation;

import org.apache.log4j.Logger;
import org.apache.struts.upload.FormFile;
import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.service.*;
import com.stimulus.struts.*;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import com.stimulus.archiva.exception.*;

public class MessageBean extends BaseBean {

  protected static final Logger logger = Logger.getLogger(MessageBean.class.getName());
  protected static MessageService messageService = null;

  protected static final Logger audit = Logger.getLogger("com.stimulus.archiva.audit");

  protected FormFile 	file;
  protected String 		fileName;
  protected String 		filePath;
  protected Email 		message = null;
  protected String 		messageId = null;
  protected int			volumeIndex = 0;
  protected String 		viewMessageURL = null;
  protected String      attachment = null;

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

 public Email getMessage() {
  	try
	{
  		logger.debug("getMessage() {"+message+"}");
	} catch (Exception e) { }
    return message;
  }


  public String viewmail()
  {
  	logger.debug("viewmail()");
  	if (messageId==null) {
  		logger.error("There was an error retrieving your message for viewing. cause: message id is null");
  		return "failure";
  	}
  	try {
  		message = MessageService.getMessageByID(new EmailID(messageId),false);
  		logger.debug("retrieved message successfully {"+message+"}");
  	} catch(Exception me) {
  		String errorStr = "Failed to retrieve the requested message. cause:"+me.getMessage();
		ActionContext.getActionContext().setSimpleMessage(errorStr);
		logger.error(errorStr);
		return "failure";
  	}
  	try {
  		logger.info("user viewing message {"+message+"}");
  		String remoteIP="unknown"; // FIX
  		String userName="unknown";
  		audit.info("view email {"+message+",ip='"+remoteIP+"',uname='"+userName+"'}");

  		deleteExtractedMessage();
  		HttpServletRequest hsr = ActionContext.getActionContext().getRequest();
  		String baseURL = hsr.getRequestURL().substring(0,hsr.getRequestURL().lastIndexOf(hsr.getServletPath()));
  		viewMessageURL = MessageService.extractMessage(message,baseURL);


  	} catch (Exception e) {
  		
  	    logger.error("failed to extract message",e);
  	}
  	return "success";
  }

  public List getAttachments() {
    List attachments =  null;

  	try {
  		if (viewMessageURL!=null)
  			attachments = MessageService.getAttachments(viewMessageURL);
  	} catch (ArchivaException me) {
  		logger.error("failed to retrieve attachments {tempURL='"+viewMessageURL+"'}",me);
  	}
  	return attachments;
  }

  public String getOriginalMessageFilePath() throws ArchivaException {
      String originalMessageFileName = "";
		if (viewMessageURL!=null)
		    originalMessageFileName = MessageService.getOriginalMessageFilePath(viewMessageURL);
	return originalMessageFileName;
  }
  
  public String getOriginalMessageFileName() throws ArchivaException {
      String originalMessageFileName = "";
		if (viewMessageURL!=null)
		    originalMessageFileName = MessageService.getOriginalMessageFileName(viewMessageURL);
	return originalMessageFileName;
  }

  public String getOriginalMessageURL() throws ArchivaException {
      String originalMessageURL = "";
		if (viewMessageURL!=null)
		    originalMessageURL = MessageService.getOriginalMessageURL(viewMessageURL);
	return originalMessageURL;
  }
  

  public String getView() {
	  	return viewMessageURL;
  }
  
  public void setAttachment(String attachment) {
      this.attachment = attachment;
  }

  public String getAttachment() {
      return attachment;
  }

  private void deleteExtractedMessage() {
  	try {
	  	if (viewMessageURL!=null)
	  		MessageService.deleteExtractedMessage(viewMessageURL);
  	} catch (ArchivaException me) {
  		logger.error("failed to delete extracted message {tempURL='"+viewMessageURL+"'}",me);
  	}
  }
  protected void finalize() {
  	try {
	  	if (viewMessageURL!=null)
	  		MessageService.deleteExtractedMessage(viewMessageURL);
  	} catch (ArchivaException me) {
  		logger.error("failed to delete extracted message {tempURL='"+viewMessageURL+"'}",me);
  	}
  }


}
