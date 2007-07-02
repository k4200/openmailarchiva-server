
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
import com.stimulus.archiva.store.*;
import com.stimulus.archiva.extraction.*;
import com.stimulus.archiva.search.*;
import com.stimulus.archiva.index.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.io.*;

import org.apache.log4j.Logger;
import com.stimulus.archiva.exception.*;

public class MessageService {

  protected static final Logger logger = Logger.getLogger(MessageService.class);
  protected static Hashtable<String,MessageExtraction> extractedMessages = new Hashtable<String,MessageExtraction>();
  protected static final Logger audit = Logger.getLogger("com.stimulus.archiva.audit");
  protected static MessageSearch messageSearch = null; 
  protected static MessageStore messageStore = null;
  protected static MessageIndex messageIndex = null;
 
  static {
      initMessageStore();
      messageSearch = new MessageSearch(messageStore);
      messageIndex = new MessageIndex(messageStore);
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
  
  public static MessageIndex getMessageIndex() { return messageIndex; }
  
  public static void initCipherKeys() throws ArchivaException {
      messageStore.initKeys();
  }
  public static Email getMessageByID(EmailID emailId, boolean headersOnly) throws ArchivaException {
    if (emailId == null)
          throw new ArchivaException("assertion failure: null emailId",logger);

    if (emailId.getVolume()==null) {
        // bit of a hack (but emailId is not guaranteed to have volume set)
        Volume volume = ConfigurationService.getConfig().getVolumes().getVolume(emailId.getUniqueID());
        emailId.setVolume(volume);
    }

    logger.debug("getMessageByUID() {"+emailId+"}");
  	return messageStore.retrieveMessage(emailId,true, true, headersOnly);
  }

  
  public static void createMessageStoreDirectories(Config config) throws ArchivaException {
      if (config == null)
          throw new ArchivaException("assertion failure: null config",logger);
      List volumes = config.getVolumes().getVolumes();
      Iterator i = volumes.iterator();
      while (i.hasNext()) {
          Volume v = (Volume)i.next();
          logger.debug("creating message store directory (if necessary) {"+v+"}");
          messageStore.createMessageStoreDir(v);
          logger.debug("creating index directory (if necessary) {"+v+"}");
          messageIndex.createIndexDir(v);
      }
  }


  public static synchronized void storeMessage(byte[] compressedMessage, String userName, String remoteIP) throws ArchivaException
  {
      		if (compressedMessage == null || userName == null || remoteIP == null)
      		    throw new ArchivaException("assertion failure: null message,username or remoteIP",logger);

      		logger.debug("message received for archival (via web service) {username='"+userName+"', client ip='"+remoteIP+"',message data length='"+compressedMessage.length+"'}");

	  	    Config config = ConfigurationService.getConfig();
	  	    
	  	    if (!config.isDefaultPassPhraseModified())
	  	    	throw new ArchivaException("failed to archive message. encryption password is not set.",logger);
	  	    Email message =  null;
	  	    try {
	  	        message = new Email(new GZIPInputStream(new ByteArrayInputStream(compressedMessage)),false);
	  	    } catch (Exception io) {
	  	        throw new MessageException("failed to archive message. unable to parse it. cause",io,logger);
	  	    }
	  	    if (config.getArchiveRules().shouldArchive(message,ConfigurationService.getConfig().getDomains())==ArchiveRules.Action.ARCHIVE) {
	  	    	Volumes vols = config.getVolumes();
	  	    	vols.readyActiveVolume();
	  	    	
	  	    	Volume activeVolume = vols.getActiveVolume();
		  	      if (activeVolume!=null) {
			  	    	EmailID emailID = new EmailID(activeVolume);
				  	    message.setEmailID(emailID);
			  	        
				  	    try {
			  	             messageStore.insertMessage(emailID,new ByteArrayInputStream(compressedMessage),false,true);
			  	             vols.touch(activeVolume);
			  	     
			  	             messageIndex.indexMessage(emailID);
			  	         } catch (Exception e) {
			  	        	 	audit.info("fail archive email {"+message+", ip='"+remoteIP+"', uname='"+userName+"'");
			  	         }
			  	        audit.info("archive email {"+message+", ip='"+remoteIP+"', uname='"+userName+"'");
				  		logger.debug("archive email {"+message+", ip='"+remoteIP+"', uname='"+userName+"'");
		  	       } else throw new ArchivaException("failed to archive message. there are no volumes available with sufficient diskspace",logger);
	  	    } else {
	  	      audit.info("skip email {"+message+", ip='"+remoteIP+"', uname='"+userName+"'");
	  	      logger.debug("skip email {"+message+", ip='"+remoteIP+"', uname='"+userName+"'");
	  	    }
	}

  public static void indexVolume(int volumeIndex, IndexStatus status) throws ArchivaException {
      if (status == null )
		    throw new ArchivaException("assertion failure: null status",logger);
      status.start();
      Volume volume = ConfigurationService.getConfig().getVolumes().getVolume(volumeIndex);
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

  public static void indexAllVolumes(IndexStatus status) throws ArchivaException {
      if (status == null )
		    throw new ArchivaException("assertion failure: null status",logger);
      status.start();
      List volumes = ConfigurationService.getConfig().getVolumes().getVolumes();
      Iterator i = volumes.iterator();
      while (i.hasNext()) {
          Volume v = (Volume)i.next();
          messageIndex.deleteIndex(v);
      }
      IndexMessage indexMessage = new MessageService.IndexMessage(ConfigurationService.getConfig(),volumes, true, true, false,status);

     try {
          messageStore.processMessages(indexMessage);
     } catch (ProcessException pe) {
         status.finish();
         status.setErrorMessage(pe.getMessage());
     }
      status.finish();
  }

  public static class IndexMessage extends MessageStore.ProcessMessage {
      IndexStatus status = null;
      public IndexMessage(Config config, List volumes, boolean decompress, boolean decrypt, boolean headersOnly, IndexStatus status) {
          super(config, volumes, decompress, decrypt, headersOnly);
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
		        is = messageStore.getRawMessageInputStream(message.getEmailID(),true,true);
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

