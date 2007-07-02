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

/** 
* MessageStorage.java - stores messages on the file system  
* @author  Jamie Band
* @version 1.0 
*/ 

package com.stimulus.archiva.persistence;
import java.nio.channels.FileChannel;
import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.exception.MessageStoreException;
import com.stimulus.archiva.exception.ProcessException;
import java.io.*;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.mail.MessagingException;
import org.apache.log4j.Logger;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.spec.*;

public class MessageStore
{

	 protected static final Logger logger = Logger.getLogger(MessageStore.class.getName());
	 protected static final String messageFileExtension = ".mrc";
	 protected static final int FILE_SIZE = 0;
	 protected static final int FILE_COUNT = 1;
     protected SecretKey key;
     protected AlgorithmParameterSpec paramSpec;


	 public MessageStore() throws MessageStoreException {
	     initKeys();
	 }

	 /** 
	   * Initialize encryption keys
	   */  
	 
	 public void initKeys() throws MessageStoreException {
	     	 Config config = Config.getConfig();
	         byte[] salt = config.getSalt();
	         String passPhrase = config.getPassPhrase();
	         if (!config.isDefaultPassPhraseModified())
	             logger.warn("archiving is disabled. encryption password is not set.");
	         int iterationCount = 17;
	         String algorithm = config.getPBEAlgorithm(); // "PBEWithMD5AndDES")
             // Create the key
	    try {
             KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), salt, iterationCount);
             key = SecretKeyFactory.getInstance(algorithm).generateSecret(keySpec);
           
             paramSpec = new PBEParameterSpec(salt, iterationCount);

         } catch (java.security.NoSuchAlgorithmException e)	{
             throw new MessageStoreException("failed to locate desired encryption algorithm {algorithm='"+algorithm+"'",logger);
         } catch (Exception e) {
             throw new MessageStoreException(e.toString(),e,logger);
         }
	 }

	 /** 
	   * Retrieve the file name of a message given an EmailID
	   * @param emailID The email ID
	   * @return the message file name
	   */  
	 
    protected String getMessageFileName(EmailID emailID) throws MessageStoreException
    {
        if (emailID==null || emailID.getVolume()==null || emailID.getUniqueID()==null)
            throw new MessageStoreException("assertion failure: null emailID, volume or uniqueId",logger);

        String filename = emailID.getVolume().getPath() + File.separatorChar + emailID.getUniqueID().substring(0, 8) + File.separatorChar + emailID.getUniqueID() + messageFileExtension;
        logger.debug("getMessageFileName() {return='" + filename + "'}");
        return filename;
    }
    
    /** 
      * Retrieve the file location of a message that could not be stored due to error
	  * @param emailID The email ID
	  * @return The file location message not processed
	  */  
    
    protected String getIndexErrorFileName(EmailID emailID) throws MessageStoreException {
        if (emailID==null || emailID.getUniqueID()==null)
            throw new MessageStoreException("assertion failure: null emailID or uniqueId",logger);
        String filename = Config.getApplicationPath() + File.separatorChar + "notindexed" + File.separatorChar + emailID.getUniqueID().substring(0, 8) + File.separatorChar + emailID.getUniqueID() + messageFileExtension;
        logger.debug("getIndexErrorFileName() {return='" + filename + "'}");
        return filename;
    }
    /** 
	   * Retrieve the directory wherein a message is stored 
	   * @param emailID The email ID
	   * @return The directory
	   */  
  
    protected String getMessageFileDirectory(EmailID emailID) throws MessageStoreException
    {
        if (emailID==null || emailID.getVolume()==null || emailID.getUniqueID()==null)
            throw new MessageStoreException("assertion failure: null emailID, volume or uniqueId",logger);

        String dirname = emailID.getVolume().getPath() + File.separatorChar + emailID.getUniqueID().substring(0, 8);
        logger.debug("getMessageFileDirectory() {return='" + dirname + "'}");
        return dirname;
    }
    
    /** 
	   * Create the directory where a volume is stored 
	   * @param volume The volume
	   * @return The directory
	   */  

    public String createMessageStoreDir(Volume volume) throws MessageStoreException {

       if (volume==null)
           throw new MessageStoreException("assertion failure: null volume",logger);

       logger.debug("createMessageDir() {" + volume + "}");

       File storeDir = new File(volume.getPath());
       if(!storeDir.exists())
       {
           logger.info("message store directory does not exist {"+volume+"}");
           boolean success = storeDir.mkdir();
           if(!success)
               throw new MessageStoreException("failed to create message store directory {" + volume + "}", logger);
           logger.info("created message store directory {" + volume + "}");
       }
       return volume.getPath();
    }

    /** 
	   * Create the directory where a message is stored
	   * @param emailID The email ID
	   * @return The message directory
	   */  

    protected String createMessageDir(EmailID emailID) throws MessageStoreException
    {
        if (emailID==null)
            throw new MessageStoreException("assertion failure: null emailID",logger);

    	String messageDir = getMessageFileDirectory(emailID);
        logger.debug("createMessageDir() {messageDir='" + messageDir + "'}");
        File todayDir = new File(messageDir);
        if(!todayDir.exists())
        {
            logger.info("message sub-directory does not exist {messageDir='" + messageDir + "'}");
            boolean makedir = todayDir.mkdir();
            if(makedir)
                logger.info("created message sub-directory {messageDir='" + messageDir + "'}");
            else
                throw new MessageStoreException("failed to create message sub=directory {messageDir='" + messageDir + "'}",logger);
        } else
        {
            logger.debug("message directory exists {messageDir='" + messageDir + "'}");
        }
        return messageDir;
    }

    /** 
	   * Insert a new message in the store
	   * @param emailID The email ID
	   * @param in The message contents
	   * @param compress Is message compressed
	   * @param encrypt Should encrypt message
	   */  
    
    public void insertMessage(EmailID emailId, InputStream in, boolean compress, boolean encrypt) throws MessageStoreException
    {
        Config config = Config.getConfig();
        
       if (emailId==null || emailId.getVolume()==null || emailId.getUniqueID()==null)
           throw new MessageStoreException("assertion failure: null emailID, volume or uniqueId",logger);

       if(emailId.getUniqueID() == null)
            throw new MessageStoreException("insert message was found to have a null message id.", logger);

       logger.debug("insertMessage {"+emailId + ",compress='" + compress + "',encrypt='"+encrypt+"'}");
       
       if (!config.isDefaultPassPhraseModified())
           throw new MessageStoreException("failed to archive message. encryption password is not set. {"+emailId+"}",logger);

       OutputStream out = null;
       createMessageStoreDir(emailId.getVolume());
       String messageDirectory = createMessageDir(emailId);
       String messageFileName = getMessageFileName(emailId);
       File messageFile = null;
        try
        {

            out = getRawMessageOutputStream(messageFileName,compress,encrypt);
            byte[] buf = new byte[1024];
            int numRead = 0;
            while ((numRead = in.read(buf)) >= 0) {
                out.write(buf, 0, numRead);
            }
        } catch(IOException e)
        {
           throw new MessageStoreException("failed to store message to file {file='" + messageFileName + "'}", e, logger);
        } finally {
        	  try
	            {

	            	if (in !=null)
	            		in.close();
	                if (out !=null) {
	                	out.flush();
	                    out.close();
	                }
	            }
	            catch(IOException ioe) { }
        }
    }

    /** 
	   * Retrieves a unique file identifier for a message
	   * @param emailID The email ID
	   */  
    
    protected String getUniqueIdFromFileName(String fileName)
    {
        int lastIndex = fileName.lastIndexOf('.');
        String uid;
        if(lastIndex == -1)
            uid = "";
        else
            uid = fileName.substring(0,lastIndex);
        logger.debug("getUniqueIdFromFileName {ret='" + uid + "'}");
        return uid;
    }
    
    /** 
	   * Retrieve a message from the store
	   * @param emailID The email ID
	   * @param decompress Should decompress message
	   * @param decrypt Should dencrypt message
	   * @param headersOnly Should return just headers
	   * @return An email message
	   */  
    
    public Email retrieveMessage(EmailID emailID, boolean decompress, boolean decrypt, boolean headersOnly) throws MessageStoreException {

        if (emailID==null || emailID.getVolume()==null || emailID.getUniqueID()==null)
            throw new MessageStoreException("assertion failure: null emailID, volume or uniqueId",logger);

        String uniqueId = emailID.getUniqueID();
        Volume volume = emailID.getVolume();
        logger.debug("retrieveMessage() {"+emailID+",retrieveHeadersOnly='" + headersOnly + "', decompress='" + decompress + "',decrypt='"+decrypt+"'");
       
        String messageFileName = getMessageFileName(emailID);
        InputStream is = null;
        try {

            is = getRawMessageInputStream(emailID, decompress, decrypt);
        }   catch(FileNotFoundException fnfe)
        {
            throw new MessageStoreException("message file not found {filename='" + messageFileName + "'}", fnfe, logger);
        } catch(IOException io)
        {
            try
            {
                if (is!=null)
                    is.close();
            }
            catch(Exception e) { }
            throw new MessageStoreException("failed to retrieve message {filename='" + messageFileName + "'}", io, logger);
        }

        Email message = null;
        try {
            message = new Email(is, headersOnly, emailID);
        } catch(MessagingException me)
        {
            try
            {
                is.close();
            }
            catch(Exception e) { }
            throw new MessageStoreException("failed to decode message {filename='" + messageFileName + "'}", me, logger);
        } finally
        {
            if(is != null)
                try
                {
                    is.close();
                }
                catch(IOException ioe)
                {
                    throw new MessageStoreException("error closing message {file '" + messageFileName + "'}", ioe, logger);
                }

        }

        try
        {
            logger.debug("retrieved message {filename='" + messageFileName + "'," + message+"}");
        }
        catch(Exception e)
        {
            throw new MessageStoreException("retrieved message does not appear to be well formed.", e, logger);
        }
        return message;

    }
    
    /** 
	   * Copy a message to a error directory if the message cannot be indexed 
	   * @param emailID The email ID
	   */  
    public void copyEmailToNoIndexQueue(EmailID emailID) throws MessageStoreException {
        if (emailID==null || emailID.getVolume()==null || emailID.getUniqueID()==null)
            throw new MessageStoreException("assertion failure: null emailID, volume or uniqueId",logger);

        String uniqueId = emailID.getUniqueID();
        Volume volume = emailID.getVolume();
        
        logger.debug("copyEmailToNoIndexQueue() {"+emailID+"'");
        FileChannel in = null, out = null;
        String messageFileName = getMessageFileName(emailID);
        String indexErrorFileName = getIndexErrorFileName(emailID);
        try {          
             in = new FileInputStream(messageFileName).getChannel();
             out = new FileOutputStream(indexErrorFileName).getChannel();
             in.transferTo( 0, in.size(), out);
            
        } catch (Exception e) {
            throw new MessageStoreException("failed to copy suspect message to noindex queue {src='"+messageFileName+"=',dest='"+indexErrorFileName+"'",logger);
        } finally {
             if (in != null) try { in.close(); } catch (Exception e) {};
             if (out != null) try { out.close(); } catch (Exception e) {};
        }
    }
    
    /** 
	   * Get a raw input stream for a message
	   * @param emailID The email ID
	   * @param decompress Should decompress message
	   * @param decrypt Should decrypt message
	   * @return An inputstream containing the message
	   */  
   public InputStream getRawMessageInputStream(EmailID emailID, boolean decompress, boolean decrypt)  throws IOException,MessageStoreException {
       if (emailID==null)
           throw new MessageStoreException("assertion failure: null emailID",logger);

       String messageFileName = getMessageFileName(emailID);
       InputStream is = new BufferedInputStream(new FileInputStream(messageFileName));
       Cipher dcipher = null;
       if(decrypt) {
           try {
              
               dcipher = Cipher.getInstance(key.getAlgorithm());
               dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
    	   } catch (Exception e) {
               throw new MessageStoreException("failed to initialize cipher. cause:",e,logger);
           }
           is = new CipherInputStream(is,dcipher);
       }

       if(decompress)
            is = new GZIPInputStream(is);

        return is;
    }
   
   /** 
	   * Get a raw output stream for a message
	   * @param messageFileName The file name of the message
	   * @param compress Should compress message
	   * @param encrypt Should encrypt message
	   * @return An outputstream directed to the message
	   */  
   
   public OutputStream getRawMessageOutputStream(String messageFileName,boolean compress, boolean encrypt) throws IOException,MessageStoreException {
       if (messageFileName==null)
           throw new MessageStoreException("assertion failure: null messageFileName",logger);

       OutputStream os = new BufferedOutputStream(new FileOutputStream(messageFileName));
       Cipher ecipher = null; 
       if (encrypt) {
           try {
               ecipher = Cipher.getInstance(key.getAlgorithm());
               ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
    	   } catch (Exception e) {
               throw new MessageStoreException("failed to initialize cipher. cause:",e,logger);
           }
           os = new CipherOutputStream(os,ecipher);
       }


       if (compress)
           os = new GZIPOutputStream(os);


       return os;
   }
   /** 
	   * Generic handler for returning the status of the reindexing operation
	   * @param ProcessMessage The process that must be executed
	   */  

    public void processMessages(ProcessMessage process ) throws ProcessException
    {
        if (process==null)
            throw new ProcessException("assertion failure: null procress",logger);
        Iterator v = process.volumes.iterator();
        process.totalSize = 0;
        process.totalFileCount = 0;
        FileFilter filter = new MessageStore.MessageFileFilter(new String[] {".",messageFileExtension});

        // calculate total size
        while (v.hasNext()) {
        	Volume volume = (Volume)v.next();
	  		String storePath = volume.getPath();
	  		//if(!storePath.endsWith(Character.toString(File.separatorChar)));
        	//	storePath = storePath.substring(storePath.length() - 1);
        	logger.debug("storepath:"+storePath);
        	File storeDirectory = new File(storePath);
            process.totalSize += getFileSizeOrCount(storeDirectory, filter, 0);
        	process.totalFileCount += getFileSizeOrCount(storeDirectory, filter, 1);
        }

        logger.debug("processing messages {totalsize='"+process.totalSize+"', totalfilecount='"+process.totalFileCount+"'}");
        Iterator v2 = process.volumes.iterator();

        while (v2.hasNext()) {
	  		process.workingVolume = (Volume)v2.next();
	  		String storePath = process.workingVolume.getPath();
	  		//if(!storePath.endsWith(Character.toString(File.separatorChar)));
        	//	storePath = storePath.substring(storePath.length() - 1);
        	File storeDirectory = new File(storePath);
        	if(storeDirectory != null && storeDirectory.isDirectory())
            	recurseMessages(storeDirectory, filter, process);
        }
    }

    private long getFileSizeOrCount(File file, FileFilter filter, int fileOrCount)
    {
        long ret = 0L;
        if(file.isDirectory())
        {
            File files[] = file.listFiles(filter);
            if(files != null)
            {
                for(int i = 0; i < files.length; i++)
                {
                    long tmpRet = getFileSizeOrCount(files[i], filter, fileOrCount);
                    if(tmpRet != -1)
                        ret += tmpRet;
                }

                return ret;
            } else
            {
                return -1;
            }
        }
        if(fileOrCount == 0)
            return file.length();
        else
            return 1;
    }

    protected void recurseMessages(File file, FileFilter filter, ProcessMessage process) throws ProcessException
    {
        if(file != null && filter != null)
            if(file.isDirectory())
            {
                File files[] = file.listFiles(filter);
                if(files != null)
                {
                    for(int i = 0; i < files.length; i++)
                        if(files[i].isDirectory())
                            recurseMessages(files[i], filter, process);
                        else {
                        	process.completeSize += files[i].length();
                        	process.completeFileCount++;
                            recurseMessages(files[i], filter, process);
						}
                }
            } else
            {
                	//logger.debug("unique file name:"+file.getName());
                	Email email = null;
                	EmailID emailID = new EmailID(getUniqueIdFromFileName(file.getName()), process.workingVolume);
                	
                	try
                	{
                    	email = retrieveMessage(emailID, process.decompress, process.decrypt, process.headersOnly);
                    	process.process(process.config, process.workingVolume, email, process.completeSize, process.totalSize, process.completeFileCount, process.totalFileCount);
                    	  
                	} catch (Exception e) {
                    	logger.error("failed to process message during re-indexing {"+emailID+"}",e);
                    	process.completeFileCount--;
                    	// throw new ProcessException(e.toString(), e, logger);
                    }
                   

            }
    }

    public static abstract class ProcessMessage
    {
		protected Volume workingVolume;
		protected Config config;
		protected long completeSize;
		protected long totalSize;
		protected long completeFileCount;
		protected long totalFileCount;
		protected boolean headersOnly;
		protected boolean decompress;
		protected boolean decrypt;
		protected List volumes;

		public ProcessMessage(Config config, List volumes,boolean decompress,boolean decrypt,boolean headersOnly) {
			this.config = config;
			this.decompress = decompress;
			this.decrypt = decrypt;
			this.headersOnly = headersOnly;
			this.volumes = volumes;
		}

        public abstract void process(Config config, Volume volume, Email email, long completeSize, long totalSize, long completeFileCount, long totalFileCount) throws ProcessException;

    }

    protected  class MessageFileFilter
        implements FileFilter
    {

        public boolean accept(File file)
        {
            if(file.isDirectory())
                return true;
            if(ext == null && file.exists())
                return true;
            for(int i = 0; i < ext.length; i++)
                if(file != null && file.exists() && file.getName().indexOf(ext[i]) > 0)
                    return true;

            return false;
        }

        private String ext[];

        public MessageFileFilter()
        {
        }

        public MessageFileFilter(String ext[])
        {
            this.ext = ext;
        }
    }



}