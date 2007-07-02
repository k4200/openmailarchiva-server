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
package com.stimulus.archiva.service;
import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.persistence.*;

import java.util.*;
import java.util.zip.GZIPInputStream;
import java.io.*;

import org.apache.log4j.Logger;
import com.stimulus.archiva.exception.*;

public class MessageService {

  protected static final Logger logger = Logger.getLogger(MessageService.class);
  protected static Hashtable extractedMessages = new Hashtable();
  protected static final Logger audit = Logger.getLogger("com.stimulus.archiva.audit");
  protected static MessageSearch messageSearch = null; 
  protected static MessageStore messageStore = null;

  static {
      initMessageStore();
      messageSearch = new MessageSearch(messageStore);
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

  public static List getMessages(Search search, int start, int numberitems) throws ArchivaException
  {
     if (search == null)
          throw new ArchivaException("assertion failure: null search",logger);
  	 logger.debug("getMessages() {start='"+start+"',number items='"+numberitems+"'}");
     List searchresults = search.getResultsList();
     ArrayList messages = new ArrayList();
     for (int i=start;i<start+numberitems;i++)
     {
     	if (i>=searchresults.size()) break;
     	Search.Result result = (Search.Result)(searchresults.get(i));

     	try {
     		// need to create and handle exceptions here
     		Email message = getMessageByID(result.getEmailId(),true);
     		logger.debug("retrieved message successfully {"+message+"}");
     		messages.add(message);
     	} catch (Exception e) {
     		logger.warn("failed to retreive message. crypto password incorrect? {"+result.getEmailId()+"}");
     	}

     }
     return messages;
  }


  private static Volume switchActiveVolume(Email message, String userName, String remoteIP) throws ArchivaException {
      if (message == null || userName == null || remoteIP == null)
          throw new ArchivaException("assertion failure: null message,userName or remoteIP",logger);
      Volume activeVolume = null;
      try {
          activeVolume = ConfigurationService.getConfig().getVolumes().nextActiveVolume();
      } catch (ConfigurationException av) {
          audit.info("fail archive email {"+message+", ip='"+remoteIP+"', uname='"+userName+"'");
	      throw new ArchivaException("failed to archive message. there are no volumes available with sufficient diskspace",logger);
      }
      return activeVolume;
  }

  public static void createMessageStoreDirectories(Config config) throws ArchivaException {
      if (config == null)
          throw new ArchivaException("assertion failure: null config",logger);
      List volumes = config.getVolumes().getVolumes();
      Iterator i = volumes.iterator();
      while (i.hasNext()) {
          Volume v = (Volume)i.next();
          messageStore.createMessageStoreDir(v);
          messageSearch.createIndexDir(v);
      }
  }


  public static synchronized void storeMessage(byte[] compressedMessage, String userName, String remoteIP) throws ArchivaException
	{
      		if (compressedMessage == null || userName == null || remoteIP == null)
      		    throw new ArchivaException("assertion failure: null message,username or remoteIP",logger);

      		logger.debug("message received for archival (via web service) {username='"+userName+"', client ip='"+remoteIP+"',message data length='"+compressedMessage.length+"'}");

	  	    Config config = ConfigurationService.getConfig();
	  	    Email message =  null;
	  	    try {
	  	        message = new Email(new GZIPInputStream(new ByteArrayInputStream(compressedMessage)),false);
	  	    } catch (Exception io) {
	  	        throw new MessageException("failed to archive message. unable to parse it. cause",io,logger);
	  	    }
	  	    if (config.getArchiveRules().shouldArchive(message,ConfigurationService.getConfig().getDomains())) {

	  	      Volume activeVolume = null;
	  	      activeVolume 		  = config.getVolumes().getActiveVolume();

  	          if (activeVolume==null)
  	              activeVolume = switchActiveVolume(message, userName, remoteIP);
  	          else if (!activeVolume.enoughDiskSpace())
  	              activeVolume = switchActiveVolume(message, userName, remoteIP);

  	         while (true) {
			  	    EmailID emailID = new EmailID(activeVolume);
			  	    message.setEmailID(emailID);
		  	        try {
		  	             messageStore.insertMessage(emailID,new ByteArrayInputStream(compressedMessage),false,true);
		  	             messageSearch.indexMessage(emailID);
		  	             config.getVolumes().touch(activeVolume);
		  	             break;
		  	         } catch (Exception e) {

		  	                activeVolume = config.getVolumes().nextActiveVolume();
		  	                if (activeVolume==null) {
		  	                  audit.info("fail archive email {"+message+", ip='"+remoteIP+"', uname='"+userName+"'");
			  	              switchActiveVolume(message, userName, remoteIP);
		  	                }
		  	         }
	  	      }

		  		audit.info("archive email {"+message+", ip='"+remoteIP+"', uname='"+userName+"'");
		  		logger.debug("archive email {"+message+", ip='"+remoteIP+"', uname='"+userName+"'");
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
      messageSearch.deleteIndex(volume);
      List volumes = new ArrayList();
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
          messageSearch.deleteIndex(v);
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
              messageSearch.indexMessage(email.getEmailID());
              } catch (Exception e0) {}

      }

  }

  public interface IndexStatus {

      public void start();

      public void finish();

      public void update(long completeSize, long totalSize, long completeFileCount, long totalFileCount);

      public void setErrorMessage(String error);

  }

  public static String extractMessage(Email message,String baseURL) throws ArchivaException {
      if (message == null || baseURL == null)
		    throw new ArchivaException("assertion failure: null message or baseURL",logger);

      logger.debug("extractMessage() {"+message+"}");

    InputStream is = null;
    try {
        is = messageStore.getRawMessageInputStream(message.getEmailID(),true,true);
    } catch (IOException io) {
        logger.error("failed to retrieve raw message contents. cause:",io);
        is = null;
    }
  	MessageExtraction me = new MessageExtraction(ConfigurationService.getConfig(),baseURL);
    String filename = me.extractMessage(message, is);
    try { if (is!=null) is.close(); } catch(Exception e) {}
    logger.debug("message extracted successfully {filekey='"+filename+"', baseurl='"+baseURL+"'}" );
    String extractionURL = null;
    if (filename!=null) {
    	extractionURL = me.getExtractionURL() + "/" + filename;
    	extractedMessages.put(extractionURL,me);
    }
  	return extractionURL;
  }

  public static void deleteExtractedMessage(String extractionURL) throws ArchivaException {
      if (extractionURL == null)
		    throw new ArchivaException("assertion failure: null extractionURL",logger);

    MessageExtraction me = (MessageExtraction)extractedMessages.get(extractionURL);
  	me.deleteExtractedMessage();
  	extractedMessages.remove(me);
  }

  public static List getAttachments(String extractionURL) throws ArchivaException {
    if (extractionURL == null)
		    throw new ArchivaException("assertion failure: null extractionURL",logger);
  	logger.debug("getAttachments {url='"+extractionURL+"'}");
  	MessageExtraction me = (MessageExtraction)extractedMessages.get(extractionURL);
  	return me.getAttachments();
  }
  
  public static MessageExtraction.Attachment getAttachment(String fileName, String extractionURL) throws ArchivaException {
      if (extractionURL == null)
  		    throw new ArchivaException("assertion failure: null extractionURL",logger);
    	logger.debug("getAttachments {url='"+extractionURL+"'}");
    	MessageExtraction me = (MessageExtraction)extractedMessages.get(extractionURL);
    	return me.getAttachment(fileName);
    }


  public static String getOriginalMessageFilePath(String extractionURL) throws ArchivaException {
        if (extractionURL == null)
		    throw new ArchivaException("assertion failure: null extractionURL",logger);
    	logger.debug("getOriginalMessageFilePath {url='"+extractionURL+"'}");
    	MessageExtraction me = (MessageExtraction)extractedMessages.get(extractionURL);
    	return me.getOriginalMessageFilePath();
  }
  
  public static String getOriginalMessageFileName(String extractionURL) throws ArchivaException {
      if (extractionURL == null)
		    throw new ArchivaException("assertion failure: null extractionURL",logger);
  	logger.debug("getOriginalMessageFileName {url='"+extractionURL+"'}");
  	MessageExtraction me = (MessageExtraction)extractedMessages.get(extractionURL);
  	return me.getOriginalMessageFileName();
}

  public static String getOriginalMessageURL(String extractionURL) throws ArchivaException {
    if (extractionURL == null)
		    throw new ArchivaException("assertion failure: null extractionURL",logger);
  	logger.debug("getOriginalMessageURL {url='"+extractionURL+"'}");
  	MessageExtraction me = (MessageExtraction)extractedMessages.get(extractionURL);
  	return me.getOriginalMessageURL();
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

