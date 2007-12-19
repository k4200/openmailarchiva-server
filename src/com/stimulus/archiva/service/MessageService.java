
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
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.Principal;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;

import com.stimulus.archiva.domain.ArchiveRules;
import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.domain.Email;
import com.stimulus.archiva.domain.EmailID;
import com.stimulus.archiva.domain.Volume;
import com.stimulus.archiva.domain.Volumes;
import com.stimulus.archiva.exception.ArchivaException;
import com.stimulus.archiva.exception.ArchiveException;
import com.stimulus.archiva.exception.ChainedRuntimeException;
import com.stimulus.archiva.exception.ConfigurationException;
import com.stimulus.archiva.exception.MaxMessageSizeException;
import com.stimulus.archiva.exception.MessageStoreException;
import com.stimulus.archiva.exception.ProcessException;
import com.stimulus.archiva.extraction.MessageExtraction;
import com.stimulus.archiva.incoming.MilterServer;
import com.stimulus.archiva.incoming.SMTPServer;
import com.stimulus.archiva.incoming.StoreMessageCallback;
import com.stimulus.archiva.index.MessageIndex;
import com.stimulus.archiva.search.MessageSearch;
import com.stimulus.archiva.security.realm.MailArchivaPrincipal;
import com.stimulus.archiva.store.MessageStore;

public class MessageService implements Serializable {

  protected static Logger logger = Logger.getLogger(MessageService.class);
  protected static Hashtable<String,MessageExtraction> extractedMessages = new Hashtable<String,MessageExtraction>();
  protected static final Logger audit = Logger.getLogger("com.stimulus.archiva.audit");
  protected static MessageSearch messageSearch = null; 
  protected static MessageStore messageStore = null;
  protected static MessageIndex messageIndex = null;
  protected static SMTPServer smtpServer = null;
  protected static MilterServer milter = null;
  
  protected static Thread purger = null;
  //protected static int lastDayPurgeExecuted = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
  protected static int lastDayPurgeExecuted =0;
  public static enum MessageState { COMPRESSED, UNCOMPRESSED };
  
  static {
      initMessageStore();
      messageSearch = new MessageSearch(messageStore);
      messageIndex = new MessageIndex(messageStore);
      smtpServer = new SMTPServer(new MessageService.WriteMessageCallback());
      smtpServer.start();
      milter = new MilterServer(new MessageService.WriteMessageCallback());
      milter.start();
  }
  
  protected static synchronized void initMessageStore() {
      try {
	      if (messageStore==null)
	          messageStore = new MessageStore();
      } catch (MessageStoreException ms) {
          throw new ChainedRuntimeException(ms.toString(),ms,logger);
      }
  }
  public static synchronized MessageStore getMessageStore() {
      if (messageStore==null)
          initMessageStore();
      return messageStore;
  }
  
  public static void restartIncomingListeners() {
	  smtpServer.shutdown();
	  milter.shutdown();
	  smtpServer = new SMTPServer(new MessageService.WriteMessageCallback());
	  smtpServer.start();
	  milter = new MilterServer(new MessageService.WriteMessageCallback());
  }
  
  public static MessageIndex getMessageIndex() { return messageIndex; }
  
  public static void initCipherKeys() throws ArchivaException {
      messageStore.initKeys();
  }
  
  public static Email getMessageByID(String volumeName, String uniqueId, boolean headersOnly) throws ArchivaException {
    if (volumeName == null || uniqueId == null)
          throw new ArchivaException("assertion failure: null emailId",logger);

    logger.debug("getMessageByID() {volumeName="+volumeName+",uniqueId='"+uniqueId+"'}");
    Volume volume = ConfigurationService.getConfig().getVolumes().getNewVolume(volumeName);
    EmailID emailID = EmailID.getEmailID(volume, uniqueId);
  	return messageStore.retrieveMessage(emailID);
  }


  
  public static boolean createVolumeDirectories(Volume v) {
	  if (v.getStatus()==Volume.Status.EJECTED)
		  return true;
	  try { 
		  logger.debug("creating message store directory (if necessary) {"+v+"}");
	      MessageStore.createMessageStoreDir(v);
	      logger.debug("creating index directory (if necessary) {"+v+"}");
	      messageIndex.createIndexDir(v);
	  } catch (ArchivaException ae) {
		  logger.debug("failed to create volume directories."+ae.getMessage(),ae);
		  return false;
	  }
	  return true;
  }

  protected static void closeVolume(Volumes vols, Volume volume) {
	  logger.info("closing active volume due to io error. {"+volume+"}");
 		try {
 			volume.setStatus(Volume.Status.CLOSED);
   		vols.saveVolumeInfo(volume,true);
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
      EmailID emailID = EmailID.getEmailID(activeVolume, message);
      message.setEmailID(emailID);  
  }
  
  protected static void archive(Principal principal, Email message,boolean retry) throws Exception {
	  	Volumes vols = ConfigurationService.getConfig().getVolumes();
	  	assignEmailID(message,vols);
	    try {
	         if (messageStore.insertMessage(message.getEmailID(),message)) {
		         vols.touchActiveVolume();
		         messageIndex.indexMessage(message.getEmailID());
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
	
		  	    Config config = ConfigurationService.getConfig();
		  	    
		
		  	    /*
		  	  try {
		    		File faultyFile = new File("c:\\temp.tmp");
			    	FileOutputStream fos = new FileOutputStream(faultyFile);
			    	byte[] buf = new byte[1024];
				    int numRead = 0;
				    while ((numRead = compressedStream.read(buf)) >= 0) {
						fos.write(buf, 0, numRead);
				    }
				    fos.close();
		    	} catch (Exception e) {
		    		logger.error(e);
		    		return;
		    	}	
		    	return;*/
		    	
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
	  	    			messageStore.copyEmailToNoArchiveQueue(message);
		    		} catch (Exception e2) {
		    			throw new ArchiveException("failed to copy message to the no archive queue.",e2,logger,ArchiveException.RecoveryDirective.RETRYLATER);
		    		}
		  	    }
		  	    
		  	    if (!config.isDefaultPassPhraseModified()) {
		  	    	try {
	  	    			messageStore.copyEmailToNoArchiveQueue(message);
		    		} catch (Exception e2) {
		    			throw new ArchiveException("failed to copy message to the no archive queue.",e2,logger,ArchiveException.RecoveryDirective.RETRYLATER);
		    		}
		    		logger.error("failed to archive message. encryption password is not set.");
		  	    }
		  	    
		  	    
		  	    if (config.getArchiveRules().shouldArchive(message,ConfigurationService.getConfig().getDomains())==ArchiveRules.Action.ARCHIVE) {
		  	    	
		  	    	try {
		  	    		archive(principal,message,false);
		  	    	} catch (Exception e) { 
		  	    		logger.error("error occurred while archiving message. message will be reprocessed on server restart",e);
		  	    		try {
		  	    			messageStore.copyEmailToNoArchiveQueue(message);
			    		} catch (Exception e2) {
			    			throw new ArchiveException("failed to copy message to the no archive queue.",e2,logger,ArchiveException.RecoveryDirective.RETRYLATER);
			    		}
		  	    	}
		  	   
		  	    } else {
		  	      audit.info("skip email {"+message+", "+principal+"}");
		  	      logger.debug("skip email {"+message+", "+principal+"}");
		  	    }
		  	    
	   	 
	}
  
  
  public static void indexVolume(Principal principal, int volumeIndex, IndexStatus status) throws ArchivaException {
      if (status == null )
		    throw new ArchivaException("assertion failure: null status",logger);
      status.start();
      Volume volume = ConfigurationService.getConfig().getVolumes().getVolume(volumeIndex);
      audit.info("index volume {"+volume+", "+principal+"}");
	  logger.debug("index volume {"+volume+", "+principal+"}");
      messageIndex.deleteIndex(volume);
      List<Volume> volumes = new LinkedList<Volume>();
      volumes.add(volume);
      IndexMessage indexMessage = new MessageService.IndexMessage(ConfigurationService.getConfig(),volumes, true, true, false,status);
      try {
          messageStore.processMessages(indexMessage);
     } catch (ProcessException pe) {
         status.finish();
         status.setErrorMessage(pe.getMessage());
     }
      status.finish();
  }

  public static void indexAllVolumes(Principal principal, IndexStatus status) throws ArchivaException {
      if (status == null )
		    throw new ArchivaException("assertion failure: null status",logger); 
      status.start();
      audit.info("index all volumes {"+principal+"}");
	  logger.debug("index all volumes {"+principal+"}");
      List<Volume> volumes = ConfigurationService.getConfig().getVolumes().getVolumes();
      for (Volume v : volumes) 
    	  messageIndex.deleteIndex(v);
      IndexMessage indexMessage = new MessageService.IndexMessage(ConfigurationService.getConfig(),volumes, true, true, false,status);
	  try {
	      messageStore.processMessages(indexMessage);
	  } catch (ProcessException pe) {
	     status.finish();
	     status.setErrorMessage(pe.getMessage());
	  }
      status.finish();
  }

/* deliberately non recursive (so we avoid situations where the whole h/d is deleted) */
  
  public static void recoverNoArchiveMessages(Recovery recovery) {
	  if (recovery==null)
		  recovery = new Recovery();
      messageStore.restoreEmailsFromNoArchiveQueue(recovery);
  }

  public static int getNoWaitingMessagesInNoArchiveQueue() {
  	return messageStore.getNoWaitingMessagesInNoArchiveQueue();
  }
  
  public static void quarantineEmails() {
	   messageStore.quarantineEmails();
  }
  
  public static int getNoQuarantinedEmails() {
	  return messageStore.getNoQuarantinedEmails();
  }
  
  public static class Recovery implements MessageStore.RecoverEmail {
	  
	  public void start() {};
  	
	  public void end(int failed, int success, int total) {};
	 
	  public boolean recover(InputStream is, String filename) {
		  Volumes vols = ConfigurationService.getConfig().getVolumes();
		  EmailID emailID = EmailID.getEmailID(null,filename);
		  Email email = null;
		  try {
			 email = new Email(emailID,is);
			  assignEmailID(email,vols);
		      messageStore.insertMessage(email.getEmailID(), email);
			  vols.touchActiveVolume();
			  messageIndex.indexMessage(email.getEmailID()); 
			  audit.info("recovered email {"+email+"}");
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
  }
  
  public static class IndexMessage extends MessageStore.ProcessMessage {
      IndexStatus status = null;
      public IndexMessage(Config config, List volumes, boolean decompress, boolean decrypt, boolean headersOnly, IndexStatus status) {
          super(config, volumes, decompress, decrypt);
          this.status = status;
      }
      public void process(Config config, Volume volume, Email email, long completeSize, long totalSize, long completeFileCount, long totalFileCount) throws ProcessException {
          if (status!=null)
              status.update(completeSize,totalSize,completeFileCount,totalFileCount);

              try {
                  logger.debug("processing email {"+email+"}");
              messageIndex.indexMessage(email.getEmailID());
              } catch (Exception e0) {}

      }

  }

 

  public interface IndexStatus {

      public void start();

      public void finish();

      public void update(long completeSize, long totalSize, long completeFileCount, long totalFileCount);

      public void setErrorMessage(String error);

  }
  
 

  public static MessageExtraction extractMessage(Email message, String baseURL, boolean isOriginalMessage) throws ArchivaException {
	    if (message == null || baseURL == null )
		    throw new ArchivaException("assertion failure: null message or baseURL",logger);
	
	    logger.debug("extractMessage() {"+message+"}");
	    InputStream is = null;
		  
		if (isOriginalMessage) {
		    try {
		        is = messageStore.getMessageInputStream(message.getEmailID(),true,true);
		    } catch (IOException io) {
		        logger.error("failed to retrieve raw message contents. cause:",io);
		        is = null;
		    }
		}
		MessageExtraction messageExtract = new MessageExtraction(message, is, baseURL);
		logger.debug("message extracted successfully {extractionURL='"+messageExtract.getViewURL()+"'}" );
		
		return messageExtract;
  }
/*
  public static void deleteExtractedMessage(String uniqueID) throws ArchivaException {
      if (uniqueID == null)
		    throw new ArchivaException("assertion failure: null uniqueID",logger);

    MessageExtraction me = (MessageExtraction)extractedMessages.get(uniqueID);
  	me.deleteExtractedMessage();
  	extractedMessages.remove(me);
  }
*/
 
  
  public static class WriteMessageCallback implements StoreMessageCallback {
	  
	  public void store(InputStream is, String remoteIP) throws ArchiveException {
				String userName = "smtpservice";
		 		MailArchivaPrincipal mp = new MailArchivaPrincipal(userName,userName,null,remoteIP);
		 		
		 		logger.debug("storeMessage (via smtp service)");
		 		logger.info("message received for archival (via smtp service)) {username='"+userName+"', client ip='"+remoteIP+"'}");
		 	  	try {
		 	  	    storeMessage(mp,is,MessageState.UNCOMPRESSED);
		 	  	} catch (ArchiveException me) {
		 	  	    logger.debug("failed to store message. Cause:",me);
		 	  	    int esc = Integer.MAX_VALUE;
		 	  	    logger.debug("consuming stream");
		 	  	    try {
		 	  	    	while (is.read()!=-1 && esc>0) { esc--; }
		 	  	    } catch (Exception e) {}
		 	  	    logger.debug("consumed stream. now throwing exception");
		 	  	    throw me;
		 	  	}
	  }
  }
  public abstract class ProcessMessageCallback {

	boolean headersOnly;
	boolean decompress;

	public ProcessMessageCallback(boolean headersOnly, boolean decompress) {
		this.headersOnly = headersOnly;
		this.decompress = decompress;
	}

	public abstract void process(Email message, long completeSize, long totalSize, long completeFileCount, long totalFileCount );
	public boolean getHeadersOnly() { return headersOnly; };
	public boolean getDecompress() { return decompress; }
  }
  
  
 
}

