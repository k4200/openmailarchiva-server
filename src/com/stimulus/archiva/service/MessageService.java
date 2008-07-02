
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


package com.stimulus.archiva.service;

import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.extraction.*;
import com.stimulus.archiva.search.*;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.zip.GZIPInputStream;
import java.io.*;
import com.stimulus.archiva.index.MessageIndex;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import com.stimulus.archiva.exception.*;
import com.stimulus.archiva.exception.ArchiveException.RecoveryDirective;

import javax.mail.event.*;
import javax.mail.*;

public class MessageService implements Serializable {
  
  private static final long serialVersionUID = -11293874311212271L;
  protected static final Logger logger = Logger.getLogger(MessageService.class);
  protected static Hashtable<String,MessageExtraction> extractedMessages = new Hashtable<String,MessageExtraction>();
  protected static final Logger audit = Logger.getLogger("com.stimulus.archiva.audit");
  public static enum MessageState { COMPRESSED, UNCOMPRESSED };
  protected static WriteMessageCallback callback = new WriteMessageCallback();
  protected static int MAX_QUEUED_TASKS = 100;
  
  public static void init() throws ArchivaException {
      Config.getConfig().getArchiver().init();
  }
  
  public static Email getMessageByID(String volumeName, String uniqueId, boolean headersOnly) throws ArchivaException {
    if (volumeName == null || uniqueId == null)
          throw new ArchivaException("assertion failure: null emailId",logger);

    logger.debug("getMessageByID() {volumeName="+volumeName+",uniqueId='"+uniqueId+"'}");
    Volume volume = Config.getConfig().getVolumes().getNewVolume(volumeName);
    EmailID emailID = EmailID.getEmailID(volume, uniqueId);
  	return Config.getConfig().getArchiver().retrieveMessage(emailID);
  }

  public static FetchMessageCallback getFetchMessageCallback() {
	  return callback;
  }
  
  public static boolean prepareVolume(Volume v) {
	  if (v.getStatus()==Volume.Status.EJECTED || v.getStatus()==Volume.Status.REMOTE )
		  return true;
	  try { 
		  logger.debug("preparing store for future write (if necessary) {"+v+"}");
		  Config.getConfig().getArchiver().prepareStore(v);
	      logger.debug("preparing index for future write (if necessary) {"+v+"}");
	      Config.getConfig().getIndex().prepareIndex(v);
	  } catch (ArchivaException ae) {
		  logger.debug("failed to prepare volume. cause:"+ae.getMessage(),ae);
		  return false;
	  }
	  return true;
  }

  
  protected static void closeVolume(Volumes vols, Volume volume) {
	  logger.info("closing active volume due to io error. {"+volume+"}");
 		try {
 			volume.setStatus(Volume.Status.CLOSED);
 			volume.save();
 		} catch (ConfigurationException ce) {
 			logger.debug("exception occurred while closing volume {"+volume+"}");	
 		}
 		logger.debug("closing volume due to io error {"+volume+"}");	
  }
  
  protected static void assignEmailID(Email message,Volumes vols) throws ArchivaException {

      vols.readyActiveVolume();
      Volume activeVolume = vols.getActiveVolume();
      if (activeVolume==null)
    	  throw new ArchivaException("failed to archive message. there are no volumes available with sufficient diskspace. please configure one.",logger);
      activeVolume.ensureDiskSpaceCheck();
      EmailID emailID = EmailID.getEmailID(activeVolume, message);
      message.setEmailID(emailID);  
  }
  
  protected static void archive(Principal principal, Email message,boolean retry) throws Exception {
	  
	    if (Config.getShutdown()) {
	    	throw new ArchiveException("mailarchiva is shutdown",logger,Level.DEBUG,ArchiveException.RecoveryDirective.RETRYLATER);
	    }
	 
	  	Volumes vols = Config.getConfig().getVolumes();
	  	assignEmailID(message,vols);
	  	
	    try {
	         if (Config.getConfig().getArchiver().insertMessage(message.getEmailID(),message)) {
		         vols.touchActiveVolume(message);
		         try {
		        	 Config.getConfig().getIndex().indexMessage(message);
		         } catch (OutOfMemoryError ofme) {
		  			 logger.debug("failed index message: out of memory",ofme);
		  		 } catch (Throwable t) {
		  			 logger.debug("failed index message:"+t.getMessage(),t);
		  		 }
	         }
         } catch (Exception e) {   
    		if (e.getCause() instanceof IOException && e.getMessage().contains("space")) {
    			logger.error("must close volume (out of disk space)",e);
    			closeVolume(vols, message.getEmailID().getVolume());
    			if (!retry) archive(principal,message,true); // retry
    		}
    		audit.error("fail archive email {"+message+", "+principal+"}");
    		throw e;
         }
         audit.info("archive email {"+message+", "+principal+"}");
	  	 logger.debug("archive email {"+message+", "+principal+"}");
  }
  
  

  public static void storeMessage(Principal principal, InputStream compressedStream, MessageState compressed) throws ArchiveException
  {
	  			
	  			
	      		if (compressedStream == null)
	      		    throw new ArchiveException("assertion failure: null message,username or remoteIP",logger,ArchiveException.RecoveryDirective.RETRYLATER);
	
	      		logger.debug("message received for archival (via smtp service) {"+principal+"'}");
	
		  	    Config config = Config.getConfig();
		  	 
		  	    Email message =  null;
		  	    
		  	    try {
		  	    	
		  	    	if (compressed==MessageState.COMPRESSED) 
		  	    		message = new Email(null,new GZIPInputStream(compressedStream));
		  	    	else
		  	    		message = new Email(null,compressedStream);
		
		  	    } catch (Exception io) {
		  	    	if (io.getCause() instanceof MaxMessageSizeException) {
		  	    		logger.error("cannot process message. max message size is exceeded.",io.getCause());
		  	    		throw new ArchiveException("cannot process message. max message size is exceeded.",(MaxMessageSizeException)io.getCause(),logger,ArchiveException.RecoveryDirective.REJECT);
		  	    	}
		  	        logger.error("archive message is corrupted. unable to parse it.",io);
		  	       try {
	  	    			config.getArchiver().backupMessage(message);
		    		} catch (Exception e2) {
		    			logger.debug(e2);
		    			logger.warn("messages cannot be written to the no archive queue. this likely to be a permissions/disk space issue.");
		    			throw new ArchiveException("failed to copy message to the no archive queue. message is corrupted.",e2,logger,Level.DEBUG,ArchiveException.RecoveryDirective.REJECT);
		    		}
		  	    }
		  	   
		  	    if (message==null) {
		  	    	logger.error("message was not received entirely.");
		  	    	return;
		  	    }
		  	    
		  	    if (!config.getArchiver().isDefaultPassPhraseModified()) {
		  	    	try {
		  	    		config.getArchiver().backupMessage(message);
		    		} catch (Exception e2) {
		    			logger.debug(e2);
		    			logger.warn("messages cannot be written to the no archive queue. this likely to be a permissions/disk space issue.");
		    			throw new ArchiveException("failed to copy message to the no archive queue.",e2,logger,ArchiveException.RecoveryDirective.RETRYLATER);
		    		}
		    		logger.error("failed to archive message. encryption password is not set.");
		  	    }
		  	 
		  	    if (config.getArchiveFilter().shouldArchive(message,Config.getConfig().getDomains())==ArchiveFilter.Action.ARCHIVE) {
		  	    	
		  	    	try {
		  	    		archive(principal,message,false);
		  	    	} catch (Exception e) { 
		  	    		logger.error("error occurred while archiving message. message will be reprocessed on server restart",e);
		  	    		try {
		  	    			config.getArchiver().backupMessage(message);
			    		} catch (MessageStoreException e2) {
			    			if (e2.getCause() instanceof javax.mail.MessagingException) {
			    				throw new ArchiveException("failed to copy message to the no archive queue.",e2,logger,ArchiveException.RecoveryDirective.REJECT);
			    			} else {
			    				logger.warn("messages cannot be written to the no archive queue. this likely to be a permissions/disk space issue.");
			    				throw new ArchiveException("failed to copy message to the no archive queue.",e2,logger,ArchiveException.RecoveryDirective.RETRYLATER);
			    			}
			    		}
		  	    	}
		  	   
		  	    } else {
		  	      audit.info("skip email {"+message+", "+principal+"}");
		  	      logger.debug("skip email {"+message+", "+principal+"}");
		  	    }
		  	    
	   	 
	}
  
  
  public static void indexVolume(Principal principal, int volumeIndex) throws ArchivaException {
    new IndexThread(principal,volumeIndex).start();
	  
  }
  
  public static class IndexThread extends Thread {
	  
	  Principal principal;
	  int volumeIndex;
	  
	  public IndexThread(Principal principal, int volumeIndex) {
		  this.principal = principal;
		  this.volumeIndex = volumeIndex;
	  }
	  
	  public void run() {
		  Config config = Config.getConfig();
	      Volume volume = config.getVolumes().getVolume(volumeIndex);
	      audit.info("index volume {"+volume+", "+principal+"}");
		  logger.debug("index volume {"+volume+", "+principal+"}");
		  MessageIndex index = (MessageIndex)config.getIndex();
	      try {
	    	  index.deleteIndex(volume);
	    	  config.getArchiver().processMessages(new IndexMessage(volume));
	     } catch (Exception e) {
	    	 logger.error("failed to index volume {"+volume+"}:"+e.getMessage(),e);
	     }
	  }
  }

/* deliberately non recursive (so we avoid situations where the whole h/d is deleted) */
  
  public static void recoverNoArchiveMessages(Recovery recovery) {
	  if (recovery==null)
		  recovery = new Recovery();
	  try {
		  Config.getConfig().getArchiver().recoverMessages(recovery);
	  } catch (MessageStoreException mse) {
		  
	  }
  }

  public static int getNoMessagesForRecovery() {
  	return Config.getConfig().getArchiver().getNoMessagesForRecovery();
  }
  
  public static void quarantineMessages() {
	  Config.getConfig().getArchiver().quarantineMessages();
  }
  
  public static int getNoQuarantinedMessages() {
	  return Config.getConfig().getArchiver().getNoQuarantinedMessages();
  }
  
  public static class Recovery implements com.stimulus.archiva.domain.Archiver.RecoverMessage {
	  ExecutorService pool = Executors.newFixedThreadPool(Config.getConfig().getArchiver().getArchiveThreads());
	  public void start() {};
  	
	  public void end(int failed, int success, int total) {};
	 
	  public void submitToPool(ArchiveEmail email) {
			 ThreadPoolExecutor poolexec = (ThreadPoolExecutor)pool;
			 while (poolexec.getQueue().size()>=MAX_QUEUED_TASKS) {
					 try { Thread.sleep(200); } catch (Exception e) {};
			}
			pool.submit(email);
	  }
	  
	  public boolean recover(InputStream is, String filename) {
		  Config config = Config.getConfig();
		  Volumes vols = config.getVolumes();
		  EmailID emailID = EmailID.getEmailID(null,filename);
		  Email email = null;
		  try {
			  email = new Email(emailID,is);
			  assignEmailID(email,vols);
			  if (config.getArchiveFilter().shouldArchive(email,Config.getConfig().getDomains())==ArchiveFilter.Action.ARCHIVE) {
				  submitToPool(new ArchiveEmail(vols.getActiveVolume(),email));
			  }
		  } catch (Exception e) {
			  email = new Email();
			  email.setEmailID(emailID);
			  update(email,false,e.getMessage());
			  logger.error("failed to recover email. {"+email+"}",e);
			  return false;
		  }
		  update(email,true,"ok");
		  return true;
	  }  
	  
	  public void update(Email email, boolean success, String output) {}
	  
	  public static class ArchiveEmail implements Runnable {
		  
		  Volume volume;
		  Email email;
		  
		  public ArchiveEmail(Volume volume, Email email) {
			  this.volume = volume;
			  this.email = email;
		  }
		  public void run() {
			  try {
				  volume.touchModified(email);
				  Config.getConfig().getArchiver().insertMessage(email.getEmailID(), email);
				  Config.getConfig().getIndex().indexMessage(email);
	        	  audit.info("recovered email {"+email+"}");
	          } catch (Exception e0) { 
			  } catch (OutOfMemoryError ofme) {
		  			 logger.debug("failed archive message: out of memory",ofme);
			  }
		  }
		  
	  }
	  
  }
  
  public static class IndexMessage extends com.stimulus.archiva.domain.Archiver.ProcessMessage {
	  ExecutorService pool = Executors.newFixedThreadPool(Config.getConfig().getArchiver().getArchiveThreads());
	  
	  public IndexMessage(Volume volume) {
		  super(volume);
	  }
	  
	  public void submitToPool(IndexEmail email) {
			 ThreadPoolExecutor poolexec = (ThreadPoolExecutor)pool;
			 while (poolexec.getQueue().size()>=MAX_QUEUED_TASKS) {
					 try { Thread.sleep(200); } catch (Exception e) {};
			}
			pool.submit(email);
	  }
	  
	  
	  public void process(Volume volume, Email email) throws ProcessException {
		  logger.debug("processing email {"+email+","+volume+"}");
		  submitToPool(new IndexEmail(volume,email));
            
      }
	  
	  public static class IndexEmail implements Runnable {
		  
		  Volume volume;
		  Email email;
		  
		  public IndexEmail(Volume volume, Email email) {
			  this.volume = volume;
			  this.email = email;
		  }
		  public void run() {
			  try {
				  volume.touchModified(email);
	        	  Config.getConfig().getIndex().indexMessage(email);
	          } catch (Exception e0) {
			  } catch (OutOfMemoryError ofme) {
		  			 logger.debug("failed index message: out of memory",ofme);
			  }
		  }
		  
	  }
  }
  

  public static class TransmitMessageStatus extends TransportAdapter {
	  
	  protected String feedback = "";
	  protected int totalMessages = 0;
	  protected int processedMessages = 0;
	  protected boolean working = false;
	  
	  public String getFeedback() { return feedback; }
	  public int getTotalMessages() { return totalMessages; }
	  public int getProcessedMessages() { return processedMessages; }
	  public boolean getWorking() { return working; }
	  
	  public void setFeedback(String feedback) { this.feedback = feedback; }
	  public void setTotalMessages(int totalMessages) { this.totalMessages = totalMessages; }
	  public void setProcessedMessages(int processedMessages) { this.processedMessages = processedMessages; }
	  public void setWorking(boolean working) { this.working = working; }
	  
	  public void updateStatus(Message message, Address[] addrs, String status) {
		  if (addrs.length==0)
			  return;
		  
		  for (int i=0; i<addrs.length ; i++) {
			  processedMessages += 1;
			  try { 
				  feedback+= "'"+message.getSubject()+"' to:"+addrs[i]+" " + status;
			  } catch (Exception ex) {}  
		  }
		  if (feedback.endsWith(","))
				  feedback=feedback.substring(0, feedback.length()-1);
		  feedback+="<br>";
	  }
	  
	  public void messageDelivered(TransportEvent e) {
		  updateStatus(e.getMessage(),e.getValidSentAddresses(),"sent OK");
		  updateStatus(e.getMessage(),e.getInvalidAddresses(),"FAILED");
	  }
	  
	  public void messageNotDelivered(TransportEvent e) {
		  updateStatus(e.getMessage(),e.getValidSentAddresses(),"sent OK");
		  updateStatus(e.getMessage(),e.getInvalidAddresses(),"FAILED");
	  }
  }

  public static MessageExtraction extractMessage(Email message, String baseURL, boolean isOriginalMessage) throws ArchivaException {
	    if (message == null || baseURL == null )
		    throw new ArchivaException("assertion failure: null message or baseURL",logger);
	
	    logger.debug("extractMessage() {"+message+"}");
	    InputStream is = null;
		  
		if (isOriginalMessage) {
		    try {
		        is = Config.getConfig().getArchiver().getMessageInputStream(message.getEmailID());
		    } catch (MessageStoreException mse) {
		        logger.error("failed to retrieve raw message contents. cause:",mse);
		        is = null;
		    } catch (IOException io) {
		    	 logger.error("failed to retrieve raw message contents. cause:",io);
			     is = null;
		    }
		}
		MessageExtraction messageExtract = new MessageExtraction(message, is, baseURL);
		logger.debug("message extracted successfully {extractionURL='"+messageExtract.getViewURL()+"'}" );
		
		return messageExtract;
  }

  public static class WriteMessageCallback implements FetchMessageCallback {
	  
	  public void store(InputStream is, String remoteIP) throws ArchiveException {
		  		logger.debug("store (via smtp service)");
		  		String userName = "smtpservice";
		 		MailArchivaPrincipal mp = new MailArchivaPrincipal(userName,Roles.SYSTEM_ROLE.getName(),null,remoteIP);
		 		logger.info("message received for archival (via smtp service)) {username='"+userName+"', client ip='"+remoteIP+"'}");
		 	  	try {
		 	  		logger.debug("start store message");
		 	  	    storeMessage(mp,is,MessageState.UNCOMPRESSED);
		 	  	    logger.debug("end store message");
		 	  	} catch (ArchiveException me) {
		 	  	    logger.debug("failed to store message. Cause:",me);
		 	  	    
		 	  	     logger.debug("now throwing exception");
		 	  	    throw me;
		 	  	} finally {
		 	  		logger.debug("consuming stream");
		 	  		byte[] b = new byte[1024];
		 	  	    try {
		 	  	    	while (is.read(b)!=-1) {  }
		 	  	    } catch (Exception e) {}
		 	  	    logger.debug("consumed stream.");
		 	  	}
	  }
  }

 
}

