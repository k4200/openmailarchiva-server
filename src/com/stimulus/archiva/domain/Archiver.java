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
import org.apache.log4j.Logger;
import javax.crypto.*;
import com.stimulus.archiva.exception.MessageStoreException;
import com.stimulus.archiva.exception.ProcessException;
import org.subethamail.smtp.util.Base64;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import com.stimulus.util.ByteUtil;
import com.stimulus.util.ConfigUtil;
import com.stimulus.util.Compare;

public abstract class Archiver implements Props {
	
    protected static final String smartAttachmentStorageKey    	= "smart.attachment.storage";
    protected static final String smartAttachmentMinSizeKey    	= "smart.attachment.minimum.size";
    protected static final String passPhraseKey 				= "security.passhrase";
    protected static final String authMethodKey 				= "security.loginmethod";

    protected static final String maxMessageSizeKey				= "max.message.size";	
    protected static final String archiveThreadsKey				= "archive.threads";
    
 
    protected static final String defaultPassPhrase="changeme";
    protected static final String defaultSmartAttachmentStorage = "yes";
    protected static final int defaultSmartAttachmentMinSize = 32768;
    protected static final String defaultMaxMessageSize		= "150"; // mb 
    protected static final String defaultArchiveThreads = "10";
    
    protected int 		maxMessageSize = 150;
    protected boolean 	smartAttachmentStorage = false;
    protected int 		smartAttachmentMinSize = 0;

    protected String    passPhrase;
    
    protected int 		archiveThreads = 6;
    
    protected static Logger logger = Logger.getLogger(Archiver.class);
    
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
    
 
	
	public String getPassPhrase() { return passPhrase; }
	
	
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
      
        if (passPhrase!=null) {
	        // if raw password is hash value, we know to see the passphrase
	        try {
	        		prop.setProperty(passPhraseKey,encryptPassword(getPassPhrase()));
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
    	String encryptedPassword = ConfigUtil.getString(prop.getProperty(passPhraseKey),defaultPassPhrase);
    	if (!encryptedPassword.endsWith("=")) {
    		passPhrase = encryptedPassword;
        } else {
        	try {
        		passPhrase = decryptPassword(encryptedPassword);
        	} catch (MessageStoreException mse) {
        		logger.error("failed to set pass phrase",mse);
        	}
        }
      	return true;
	}
	

   
    
    public abstract void init()throws MessageStoreException;
    
    public abstract boolean insertMessage(EmailID emailId, Email email) throws MessageStoreException;
    
    public abstract Email retrieveMessage(EmailID emailID) throws MessageStoreException;
 
    public abstract void backupMessage(Email message) throws MessageStoreException;
    
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
    
 	public static class ProcessMessage
    {
 		public Volume workingVolume;
 
 		
 		public ProcessMessage(Volume workingVolume) {
 			this.workingVolume = workingVolume;
 		}
 		
        public void process(Volume volume, Email email) throws ProcessException {
        	
        }
        
        public void error(Volume volume, Email email) throws ProcessException {
        	
        }
    
        public void setErrorMessage(String errorMessage) {
        	
        }
        
    }
 

 	 public interface RecoverMessage {
     	
     	public void start();
     	
     	public boolean recover(InputStream is, String filename);
     	
     	public void end(int failed, int success, int total);
     }
 
 	
 	public String encryptPassword(String password) throws MessageStoreException {
 		try {
 	    		int iterationCount = 17;  
 	    		KeySpec keySpec = new PBEKeySpec(Config.getEncKey().toCharArray(), Config.getConfig().getSalt(), iterationCount);
             	Key key = SecretKeyFactory.getInstance(Config.getConfig().getPBEAlgorithm()).generateSecret(keySpec);
             	AlgorithmParameterSpec  paramSpec = new PBEParameterSpec(Config.getConfig().getSalt(), iterationCount);
             	Cipher cipher = Cipher.getInstance(key.getAlgorithm());
             	cipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
             	byte[] outputBytes = cipher.doFinal(password.getBytes("UTF-8"));
             	return Base64.encodeToString(outputBytes,false);
          } catch (java.security.NoSuchAlgorithmException e)	{
              throw new MessageStoreException("failed to locate desired encryption algorithm {algorithm='"+Config.getConfig().getPBEAlgorithm()+"'",logger);
          } catch (Exception e) {
              throw new MessageStoreException(e.toString(),e,logger);
          }
 	}
 	  public String decryptPassword(String password)  throws MessageStoreException {
 		 try {
	    	int iterationCount = 17; 
	    	KeySpec keySpec = new PBEKeySpec(Config.getEncKey().toCharArray(), Config.getConfig().getSalt(), iterationCount);
          	Key key = SecretKeyFactory.getInstance(Config.getConfig().getPBEAlgorithm()).generateSecret(keySpec);
          	AlgorithmParameterSpec  paramSpec = new PBEParameterSpec(Config.getConfig().getSalt(), iterationCount);
          	Cipher cipher = Cipher.getInstance(key.getAlgorithm());
          	cipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
          	byte[] base64DecodedData = Base64.decodeFast(password);
          	byte[] outputBytes = cipher.doFinal(base64DecodedData);
          	return new String(outputBytes);
       } catch (java.security.NoSuchAlgorithmException e)	{
           throw new MessageStoreException("failed to locate desired encryption algorithm {algorithm='"+Config.getConfig().getPBEAlgorithm()+"'",logger);
       } catch (Exception e) {
           throw new MessageStoreException(e.toString(),e,logger);
       }  
 	  }
 	    

    
}
