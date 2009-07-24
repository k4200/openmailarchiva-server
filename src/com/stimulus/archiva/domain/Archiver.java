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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import org.apache.commons.logging.*;
import javax.crypto.*;
import com.stimulus.archiva.exception.*;
import org.subethamail.smtp.util.Base64;
import com.stimulus.util.*;

public abstract class Archiver implements Props {
	
    protected static final String smartAttachmentStorageKey    	= "smart.attachment.storage";
    protected static final String smartAttachmentMinSizeKey    	= "smart.attachment.minimum.size";
    protected static final String passPhraseKey 				= "security.passhrase";
    protected static final String authMethodKey 				= "security.loginmethod";

    protected static final String maxMessageSizeKey				= "max.message.size";	
    protected static final String archiveThreadsKey				= "archive.threads";
    protected static final String processMalformedMessagesKey   = "archive.process.malformed.messages";
   
    protected static final String defaultPassPhrase="changeme";
    protected static final String defaultSmartAttachmentStorage = "yes";
    protected static final int defaultSmartAttachmentMinSize = 32768;
    protected static final String defaultMaxMessageSize		= "150"; // mb 
    protected static final String defaultArchiveThreads = "6";
    protected static final String defaultProcessMalformedMessages   = "no";
    
    protected int 		maxMessageSize = 150;
    protected boolean 	smartAttachmentStorage = false;
    protected int 		smartAttachmentMinSize = 0;

    protected String    passPhrase;
    
    protected int 		archiveThreads = 6;
    protected int 		runningArchiveThreads = 6;
    protected boolean   processMalformedMessages = false;
    
    protected static Log logger = LogFactory.getLog(Archiver.class);
    
    public int getMaxMessageSize() {
 	   return maxMessageSize;
    }

    public void setMaxMessageSize(int maxMessageSize) {
 	   this.maxMessageSize = maxMessageSize;
    }
    
    
    public boolean isSmartAttachmentStorage() {
  	  return smartAttachmentStorage;
    }
    
    public void setSmartAttachmentStorage(boolean smartAttachmentStorage) {
  	  this.smartAttachmentStorage = smartAttachmentStorage;
    }
  
    public int getSmartAttachmentMinSize() {
  	  return smartAttachmentMinSize;
    }
    
    public void setSmartAttachmentMinSize(int smartAttachmentMinSize) {
  	  this.smartAttachmentMinSize = smartAttachmentMinSize;
    }
    
    public void setArchiveThreads(int archiveThreads) {
    	this.archiveThreads = archiveThreads;
    }
    
    public int getArchiveThreads() { return archiveThreads; }
    
 
    public void setRunningArchiveThreads(int runningArchiveThreads) {
    	if (runningArchiveThreads<0)
    		this.runningArchiveThreads = archiveThreads;
    	else
    		this.runningArchiveThreads = runningArchiveThreads;
    }
    
    public int getRunningArchiveThreads() { return runningArchiveThreads; }
	
	public String getPassPhrase() { return passPhrase; }
	
	public boolean getProcessMalformedMessages() { return processMalformedMessages; }
	
	public void setProcessMalformedMessages(boolean processMalformedMessages) {
		this.processMalformedMessages = processMalformedMessages;
	}
	
	public void setPassPhrase(String passPhrase) {
		try {
		    if (passPhrase.trim().length()<1)
		        return;
		 
		    MessageDigest sha = MessageDigest.getInstance("SHA-1");
     		byte[] input = sha.digest(ByteUtil.mergeByteArrays(passPhrase.getBytes("UTF-8"),Config.getConfig().getSalt()));
     		this.passPhrase = Base64.encodeToString(input,false); 
			 
		} catch (Exception e) {
			logger.error("failed to setPassPhrase:"+e.getMessage(),e);
		}
	}
	
	public boolean isDefaultPassPhraseModified() {
		  if (passPhrase==null)
			  return false;
		  boolean modified = !Compare.equalsIgnoreCase(passPhrase, "changeme");
	    logger.debug("default password {modified='"+modified+"'}");
	    return modified;
	}
	

	public void saveSettings(String prefix, Settings prop, String suffix) {
		logger.debug("saving archiver settings");
		prop.setProperty(smartAttachmentStorageKey, ConfigUtil.getYesNo(isSmartAttachmentStorage()));
	  	prop.setProperty(smartAttachmentMinSizeKey, Integer.toString(getSmartAttachmentMinSize()));
      
        prop.setProperty(maxMessageSizeKey,Integer.toString(maxMessageSize));
        prop.setProperty(archiveThreadsKey,Integer.toString(archiveThreads));
        prop.setProperty(processMalformedMessagesKey, ConfigUtil.getYesNo(processMalformedMessages));
      
        if (passPhrase!=null) {
	        // if raw password is hash value, we know to see the passphrase
	        try {
	        		prop.setProperty(passPhraseKey,Crypto.encryptPassword(getPassPhrase()));
        	} catch (MessageStoreException mse) {
        		logger.error("failed to set pass phrase",mse);
        	}
	       
        }
		
	}
	
	public boolean loadSettings(String prefix, Settings prop, String suffix) {
		logger.debug("loading archiver settings");
		setSmartAttachmentStorage(ConfigUtil.getBoolean(prop.getProperty(smartAttachmentStorageKey),defaultSmartAttachmentStorage));
	    setSmartAttachmentMinSize(ConfigUtil.getInteger(prop.getProperty(smartAttachmentMinSizeKey),Integer.toString(defaultSmartAttachmentMinSize)));

    	setMaxMessageSize(ConfigUtil.getInteger(prop.getProperty(maxMessageSizeKey),defaultMaxMessageSize));
    	setArchiveThreads(ConfigUtil.getInteger(prop.getProperty(archiveThreadsKey),defaultArchiveThreads));
    	setRunningArchiveThreads(ConfigUtil.getInteger(prop.getProperty(archiveThreadsKey),defaultArchiveThreads));
    	String encryptedPassword = ConfigUtil.getString(prop.getProperty(passPhraseKey),defaultPassPhrase);
    	if (!encryptedPassword.endsWith("=")) {
    		passPhrase = encryptedPassword;
        } else {
        	try {
        		passPhrase = Crypto.decryptPassword(encryptedPassword);
        	} catch (MessageStoreException mse) {
        		logger.error("failed to set pass phrase",mse);
        	}
        }
    	setProcessMalformedMessages(ConfigUtil.getBoolean(prop.getProperty(processMalformedMessagesKey),defaultProcessMalformedMessages));
      	return true;
	}
	

   
    
    public abstract void init()throws MessageStoreException;
    
    public abstract boolean insertMessage(Email email) throws MessageStoreException;
    
    public abstract Email retrieveMessage(EmailID emailID) throws MessageStoreException;
 
    public abstract void backupMessage(File file) throws MessageStoreException;
    
    public abstract void backupMessage(Email email) throws MessageStoreException;
    
    public abstract void backupMessage(EmailID emailID) throws MessageStoreException;
    
    	
 	public abstract void processMessages(ProcessMessage process) throws ProcessException;
 	
 	public abstract void recoverMessages(RecoverMessage recover) throws MessageStoreException;
 	
 	public abstract int getNoMessagesForRecovery();

 	public abstract void quarantineMessages();
 	
 	public abstract int getNoQuarantinedMessages();
 	
 	public abstract InputStream getMessageInputStream(EmailID emailId) throws IOException, MessageStoreException;
 
 	public abstract void prepareStore(Volume v) throws MessageStoreException;

    public abstract boolean isMessageExist(EmailID emailId); 
    
    public abstract  InputStream getRawMessageInputStream(File messageFile, boolean decompress, boolean decrypt)  throws IOException,MessageStoreException;
    
 	public static abstract class ProcessMessage
    {
 		public Volume workingVolume;
 
 		
 		public ProcessMessage(Volume workingVolume) {
 			this.workingVolume = workingVolume;
 		}
 		
        public abstract void process(EmailID emailID) throws ProcessException;
      
    }
 

 	 public interface RecoverMessage {
     	
     	public void start();
     	
     	public boolean recover(File file) throws DiskSpaceException;
     	
     	public void end(int failed, int success, int total);
     }
 
 

    
}
