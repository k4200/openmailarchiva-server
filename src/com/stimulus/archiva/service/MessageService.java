
/* Copyright (C) 2005-2009 Jamie Angus Band
 * MailArchiva Open Source Edition Copyright (c) 2005-2009 Jamie Angus Band
 * This program is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version
 * 3 of the License, or (at your option) any later version.
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
import com.stimulus.util.*;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.GZIPInputStream;
import java.io.*;
import com.stimulus.archiva.index.MessageIndex;
import org.apache.commons.logging.*;
import com.stimulus.archiva.exception.*;
import javax.mail.event.*;
import javax.mail.*;
import com.stimulus.util.*;


public class MessageService implements Serializable {

  private static final long serialVersionUID = -11293874311212271L;
  protected static final Log logger = LogFactory.getLog(MessageService.class);
  protected static Hashtable<String,MessageExtraction> extractedMessages = new Hashtable<String,MessageExtraction>();
  protected static final Log audit = LogFactory.getLog("com.stimulus.archiva.audit");
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
	  if (callback==null) {
		  callback = new WriteMessageCallback();
	  }
	  return callback;
  }


  public static boolean prepareVolume(Volume v) {

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

  protected static void assignEmailID(Email message,Volumes vols) throws ArchivaException, DiskSpaceException {

      Volume activeVolume = vols.getVolume(Volume.Status.ACTIVE);
      if (activeVolume==null)
    	  throw new DiskSpaceException("failed to archive message. there are no volumes available with sufficient diskspace. please configure one.",logger);
      EmailID emailID = EmailID.createEmailID(activeVolume, message);
      message.setEmailID(emailID);
  }

  protected static void archive(Principal principal, Email message,boolean retry) throws Exception {

	    if (Config.getShutdown()) {
	    	throw new ArchiveException( Config.getConfig().getProductName()+" is shutdown",ArchiveException.RecoveryDirective.RETRYLATER);
	    }

	  	Volumes vols = Config.getConfig().getVolumes();


	    try {
	         if (Config.getConfig().getArchiver().insertMessage(message)) {
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

  private static void backupMessage(InputStream is) throws ArchiveException  {
	  Config config = Config.getConfig();
	  File tempFile;
	    try {
	    	tempFile = copyToTemp(is);
	    	config.getArchiver().backupMessage(tempFile);
		} catch (Throwable e) {
			try { is.close(); } catch (Exception e2) {}
			logger.debug(e);
			logger.warn("messages cannot be written to the no archive queue. run of out local disk space?");
			throw new ArchiveException("failed to copy message to the no archive queue.",e,logger,ArchiveException.RecoveryDirective.RETRYLATER);
		} finally {
			try { is.close(); } catch (Exception e) {}
		}
  }

  private static void backupMessage(Email email) throws ArchiveException  {
	  Config config = Config.getConfig();
	    try {
	    	config.getArchiver().backupMessage(email);
		} catch (MessageStoreException mse) {
			if (mse.getCause()!=null && mse.getCause() instanceof MessagingException)
				throw new ArchiveException("failed to copy message to the no archive queue.",ArchiveException.RecoveryDirective.RETRYLATER);
			if (mse.getMessage()!=null) {
				if (mse.getMessage().contains("No content") ||
					mse.getMessage().contains("No inputstream from datasource") ||
				    mse.getMessage().contains("Connection reset")) {
						throw new ArchiveException("failed to copy message to the no archive queue.",ArchiveException.RecoveryDirective.RETRYLATER);
				}
			}
			logger.debug(mse);
			logger.warn("messages cannot be written to the no archive queue. run of out local disk space?");
			throw new ArchiveException("failed to copy message to the no archive queue.",mse,logger,ArchiveException.RecoveryDirective.RETRYLATER);
		}
  }


  private static void deleteTemp(File tempFile) {
	  if (tempFile!=null) {
		  tempFile.delete();
	  }
  }
  private static File copyToTemp(InputStream stream) throws ArchiveException {
	  File tempFile = null;
		try {
			 tempFile = File.createTempFile("incoming",".eml");
			 logger.debug("storing archive data in temp file {tempFile='"+tempFile.getPath()+"'}");
			 IOUtil.copy(stream,new FileOutputStream(tempFile));
			 logger.debug("temp file written {tempFile='"+tempFile.getPath()+"'}");
			 return tempFile;
		} catch (Throwable e) {
			tempFile.delete();
			try { stream.close(); } catch (Exception e2) {}
			throw new ArchiveException("failed to retrieve message for archiving:"+e.getMessage(),e,logger,ArchiveException.RecoveryDirective.RETRYLATER);
		}
  }

  public static void storeMessage(Principal principal, InputStream in) throws ArchiveException
  {
	  			if (in == null)
	      		    throw new ArchiveException("assertion failure: null message,username or remoteIP",logger,ArchiveException.RecoveryDirective.RETRYLATER);

	      		logger.debug("message received for archival {"+principal+"'}");

	      		boolean processMalformed = Config.getConfig().getArchiver().getProcessMalformedMessages();
	      		File tempFile = null;
	      		InputStream inStream = null;
	      		Email message =  null;
		  	    Config config = Config.getConfig();

		  	    try {
			  	    if (processMalformed) {
			  	  		try {
			  	  			tempFile = copyToTemp(in);
			  	  		} catch (Exception io) {
			  	  			throw new ArchiveException("failed to copy message to temp directory:"+io.getMessage(),io,logger,ArchiveException.RecoveryDirective.RETRYLATER);
			  	  		}
			      		try {
			      			inStream = new BufferedInputStream(new FileInputStream(tempFile));
			      		} catch (FileNotFoundException fnfe) {
			      			throw new ArchiveException("failed to copy message to temp directory:"+fnfe.getMessage(),fnfe,logger,ArchiveException.RecoveryDirective.RETRYLATER);
			      		}
			      		try {
			      			message = new Email(null,inStream);
			      		} catch (Exception e) {
			      			InputStream errorStream = null;
			      			try {
			      				errorStream = new BufferedInputStream(new FileInputStream(tempFile));
				      		} catch (FileNotFoundException fnfe) {
				      			throw new ArchiveException("failed to copy message to temp directory:"+fnfe.getMessage(),fnfe,logger,ArchiveException.RecoveryDirective.RETRYLATER);
				      		}
			      			backupMessage(errorStream);
			      			throw new ArchiveException("archive message appears corrupted:"+e.getMessage(),e,logger,ArchiveException.RecoveryDirective.ACCEPT);
			      		}
			  	  	} else {
			  	  		inStream = new BufferedInputStream(in);
			  	  		try {
			  	  			message = new Email(null,inStream);
				  	  	} catch (Exception e) {
			      			throw new ArchiveException("archive message is corrupted:"+e.getMessage()+".",e,logger,ArchiveException.RecoveryDirective.ACCEPT);
			      		}
			  	  	}

			  	    if (!config.getArchiver().isDefaultPassPhraseModified()) {
			  	    	backupMessage(message);
			    		logger.error("failed to archive message. encryption password is not set.");
			  	    }
			  	    if (config.getArchiveFilter().shouldArchive(message,Config.getConfig().getDomains())==ArchiveFilter.Action.ARCHIVE) {
			  	    	try {
			  	    		assignEmailID(message,Config.getConfig().getVolumes());
			  	    		if (message.getEmailID().getVolume().isEjected()) {
				  	  	  		logger.debug("attempt to archive message to ejected volume. sending message to no archive queue.");
				  	  	  		backupMessage(message);
				  	  	  	}
				  	  	  	archive(principal,message,false);
			  	    	} catch (Exception e) {
			  	    		logger.error("error occurred while archiving message. message will be reprocessed on server restart",e);
			  	    		backupMessage(message);
			  	    	}

			  	    } else {
			  	      audit.info("skip email {"+message+", "+principal+"}");
			  	      logger.debug("skip email {"+message+", "+principal+"}");
			  	    }
		  	    } finally {
		  	    	deleteTemp(tempFile);
		  	    	StreamUtil.emptyStream(in);
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

	  Volumes volumes = Config.getConfig().getVolumes();

	  if (volumes==null)
		  return;

	  Volume activeVolume = volumes.getVolume(Volume.Status.ACTIVE);

      if (activeVolume==null) {
    	  logger.debug("aborting recovery of messages in no archive queue. there is no active volume.");
    	  return;
      }

      if (activeVolume.isEjected()) {
    	  logger.debug("aborting recovery of messages in no archive queue. active volume is ejected.");
    	  return;
      }

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


	  ExecutorService threadPool = Executors.newFixedThreadPool(Config.getConfig().getArchiver().getArchiveThreads());
	  BoundedExecutor boundedExecutor = new BoundedExecutor((ThreadPoolExecutor)threadPool,Config.getConfig().getArchiver().getArchiveThreads());

	  public void start() {};

	  public void end(int failed, int success, int total) {};

	  public boolean recover(File file) throws DiskSpaceException {
		  try {
				boundedExecutor.submitTask(new ArchiveEmail(file));
		  } catch (Exception ee) {
			logger.error("failed to block for reindex pool submission:"+ee.getMessage(),ee);
		 }
		  return true;
	  }

	  public void update(Email email, boolean success, String output) {}

	  public class ArchiveEmail implements Runnable {

		  File file;

		  public ArchiveEmail(File file) {
			  this.file = file;
		  }
		  public void run() {
			  try {
				  Config config = Config.getConfig();
				  Volumes vols = config.getVolumes();
				  Email email = null;
				  InputStream is = null;
				  try {
					  is = Config.getConfig().getArchiver().getRawMessageInputStream(file, false, false);
					  EmailID emailID = EmailID.getEmailID(null,file.getPath());
					  email = new Email(emailID,is);
					  assignEmailID(email,vols);
					  Config.getConfig().getArchiver().insertMessage(email);
					  Config.getConfig().getIndex().indexMessage(email);
					  if (is!=null) is.close();
					  boolean deleted;
	  			  	  file.deleteOnExit();
	  			  	  deleted = file.delete();
	  			  	  if (!deleted)
	  			  	    file.renameTo(File.createTempFile("oldrecovery", "tmp"));
	  	              update(email,true,"ok");
		        	  audit.info("recovered email {"+email+"}");
				  } catch (DiskSpaceException de) {
					 throw de;
				  } catch (Exception e) {
					  logger.error("failed to recover email. could not assign email id:"+e.getMessage()+" {"+email+"}",e);
					  update(email,false,e.getMessage());
					  logger.error("failed to recover email. {"+email+"}",e);
					  return;
				  } finally {
					  try { if (is!=null) is.close(); } catch (Exception e) {}
				  }
	          } catch (Exception e0) {
			  } catch (OutOfMemoryError ofme) {
		  			 logger.debug("failed archive message: out of memory",ofme);
			  }
		  }
	  }
  }

  public static class BoundedExecutor {
	    private final Executor exec;
	    private final Semaphore semaphore;

	    public BoundedExecutor(Executor exec, int bound) {
	        this.exec = exec;
	        this.semaphore = new Semaphore(bound);
	    }

	    public void submitTask(final Runnable command) throws InterruptedException {
	        semaphore.acquire();
	        try {
	            exec.execute(new Runnable() {
	                public void run() {
	                    try {
	                        command.run();
	                    } finally {
	                        semaphore.release();
	                    }
	                }
	            });
	        } catch (RejectedExecutionException e) {
	            semaphore.release();
	        }
	    }
	}


  public static class IndexMessage extends com.stimulus.archiva.domain.Archiver.ProcessMessage {

	  ExecutorService threadPool = Executors.newFixedThreadPool(Config.getConfig().getArchiver().getArchiveThreads());
	  BoundedExecutor boundedExecutor = new BoundedExecutor((ThreadPoolExecutor)threadPool,Config.getConfig().getArchiver().getArchiveThreads());

	  public IndexMessage(Volume volume) {
		  super(volume);
	  }

	  public void process(EmailID emailID) throws ProcessException {
		  logger.debug("processing email {"+emailID+"}");
		  try {
				boundedExecutor.submitTask(new IndexEmail(emailID));
		  } catch (Exception ee) {
			logger.error("failed to block for reindex pool submission:"+ee.getMessage(),ee);
		 }
      }

	  public void setErrorMessage(String errorMessage) {

	  }

	  public class IndexEmail implements Runnable {

		  EmailID emailID;

		  public IndexEmail(EmailID emailID) {
			  this.emailID = emailID;
		  }
		  public void run() {
			  try {
				  Email email = Config.getConfig().getArchiver().retrieveMessage(emailID);
	        	  Config.getConfig().getIndex().indexMessage(email);
	          } catch (Exception e0) {
			  } catch (OutOfMemoryError ofme) {
		  			 logger.debug("failed index message: out of memory",ofme);
		  	  } catch (Throwable t) {
				  	logger.debug("failed index message:"+t.getMessage());
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
		  		logger.debug("store message");
		  		String userName = "smtpservice";
		 		MailArchivaPrincipal mp = new MailArchivaPrincipal(userName,Roles.SYSTEM_ROLE.getName(),null,remoteIP);
		 		logger.info("message received for archival (via smtp service)) {username='"+userName+"', client ip='"+remoteIP+"'}");
		 	  	try {
		 	  		logger.debug("start store message");
		 	  	    storeMessage(mp,is);
		 	  	    logger.debug("end store message");
		 	  	} catch (ArchiveException me) {
		 	  	    logger.debug("failed to store message. Cause:",me);

		 	  	     logger.debug("now throwing exception");
		 	  	    throw me;
		 	  	} finally {
		 	  		logger.debug("consuming stream");
		 	  		StreamUtil.emptyStream(is);
		 	  	}
	  }
  }

  static class DebugInputStream extends FilterInputStream
  {
	ByteArrayOutputStream baos;

  	public DebugInputStream(InputStream is)
  	{	super(is);
  		baos = new ByteArrayOutputStream(4096);
  	}

  	public int read() throws IOException
  	{	int b = super.read();
  		baos.write(b);
  		return b;
  	}

  	public int read(byte[] b) throws IOException
  	{	int l = super.read(b);
  		if(l>0) baos.write(b,0,l);
  		return l;
  	}

  	public int read(byte[] b,int off,int len) throws IOException
  	{	int l = super.read(b,off,len);
  		if(l>0) baos.write(b,off,l);
  		return l;
  	}

  	public void debugDump()
  	{	logger.debug(new String(baos.toByteArray()));
  		baos.reset();
  	}

  	public void debugReset()
  	{	baos.reset();
  	}
  }


}

