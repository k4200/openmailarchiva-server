
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
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.apache.struts.upload.FormFile;
import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.domain.fields.EmailField;
import com.stimulus.archiva.domain.fields.EmailFieldValue;
import com.stimulus.archiva.domain.fields.EmailFields;
import com.stimulus.archiva.service.*;
import com.stimulus.archiva.extraction.*;
import java.util.concurrent.locks.*;
import com.stimulus.struts.*;
import com.stimulus.archiva.exception.*;
import com.stimulus.util.*;
import java.io.*;
public class MessageBean extends BaseBean implements Serializable {

  private static final long serialVersionUID = 1624887450703706628L;
  protected static final Logger logger = Logger.getLogger(MessageBean.class.getName());
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
  protected int					resultsIndex = 0;
  
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
		  	logger.info("view email {"+email+", "+getMailArchivaPrincipal()+"}");
	  		audit.info("view email {"+email+", "+getMailArchivaPrincipal()+"}");

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

  
	 public List<EmailField> getFields() {
		 ArrayList<EmailField>  list = new ArrayList<EmailField>();
		 EmailFields emailFields = Config.getConfig().getEmailFields();
		 Iterator i = emailFields.getAvailableFields().values().iterator();
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
		 for (EmailFieldValue efv : email.getFields().values()) {
			 
//			 we dont allow end-users to view bcc or delivered-to flags
			  if (efv.getField().getName().equals("bcc") && getMailArchivaPrincipal().getRole().equals("user"))
				  continue;
			  if (efv.getField().getName().equals("deliveredto") && getMailArchivaPrincipal().getRole().equals("user"))
				  continue;
			  if (efv.getField().getName().equals("recipient") && getMailArchivaPrincipal().getRole().equals("user"))
				  continue;
			  
			 if (efv.getField().getViewEmail()==EmailField.AllowViewMail.VIEWMAIL) {
				 list.add(DisplayField.getDisplayField(efv, getLocale(),true));
			 }
		 }
		 return list;
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
    
    public String nextMessage() {
    	Search search = getSearch();
    	List<Search.Result> results = search.getResults();
    	resultsIndex += 1;
    	if (resultsIndex>=results.size())
    		resultsIndex = 0;
    	try {
    		setVolumeID(results.get(resultsIndex).getEmailId().getVolume().getID());
    		setMessageID(results.get(resultsIndex).getEmailId().getUniqueID());
    	} catch (MessageSearchException mse) {
    		logger.error("failed to set volume ID or message ID:"+mse.getMessage());
    	}
    	return viewmail();
    }
    
    public String previousMessage() { 
    	Search search = getSearch();
    	List<Search.Result> results = search.getResults();
    	resultsIndex -= 1;
    	if (resultsIndex<0)
    		resultsIndex = results.size()-1;
    	try {
    		setVolumeID(results.get(resultsIndex).getEmailId().getVolume().getID());
    		setMessageID(results.get(resultsIndex).getEmailId().getUniqueID());
    	} catch (MessageSearchException mse) {
    		logger.error("failed to set volume ID or message ID:"+mse.getMessage());
    	}
    	return viewmail();
    }
    
    private Search getSearch() {
  	  SearchBean searchBean = (SearchBean)ActionContext.getActionContext().getSessionMap().get("searchBean");
  	  return searchBean.getSearch();
    }
    
    public String viewaction() throws ArchivaException {
      SubmitButton button = getSubmitButton();
      
      if (button==null | button.action==null)
          return "success";
      
    	logger.debug("viewaction() {action ='"+button.action+"', value='"+button.value+"'}");
        
    	if (button.action.equals("next")) {
	  		return nextMessage();
    	} else if (button.action.equals("previous")) {
	  		return previousMessage();
    	}
	  		
    	return "success";
    }
    
    public void setResultsIndex(int index) {
    	this.resultsIndex = index;
    }
    
    public int getResultsIndex() { 
    	return resultsIndex;
    }
    
    public int getResultsSize() {
    	Search search = getSearch();
    	return search.getResultSize();
    }

}


