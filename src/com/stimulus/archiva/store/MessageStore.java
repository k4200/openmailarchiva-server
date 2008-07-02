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


/** 
* MessageStorage.java - stores messages on the file system  
* @author  Jamie Band
* @version 1.0 
*/ 

package com.stimulus.archiva.store;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.exception.MessageStoreException;
import com.stimulus.archiva.exception.ProcessException;
import com.stimulus.util.*;
import java.io.*;
import java.util.Date;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.mail.MessagingException;
import javax.mail.Part;
import org.apache.log4j.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.spec.*;
import javax.mail.internet.*;
import javax.mail.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.*;
import com.sun.mail.util.*;
import com.stimulus.archiva.domain.Archiver;


public class MessageStore extends Archiver implements Serializable
{

	 private static final long serialVersionUID = -2610982280435598267L;
	 protected static final Logger logger = Logger.getLogger(MessageStore.class.getName());
	 protected static final String messageFileExtension = ".mrc";
	 protected static final String attachmentFileExtension = ".att";
	 protected static final int FILE_SIZE = 0;
	 protected static final int FILE_COUNT = 1;
     protected SecretKey key;
     protected AlgorithmParameterSpec paramSpec;
     private static String hexits = "0123456789abcdef";
     protected TempFiles tempfiles;
	 static enum Action { STRIP, COMBINE };

	 public MessageStore() {
	    
	 }

	 public void init() throws MessageStoreException {
	 	 tempfiles = Config.getFileSystem().getTempFiles();
         byte[] salt = Config.getConfig().getSalt();
         String passPhrase = getPassPhrase();
         if (!isDefaultPassPhraseModified())
             logger.warn("archiving is disabled. encryption password is not set.");
         int iterationCount = 17;
         String algorithm = Config.getConfig().getPBEAlgorithm(); // "PBEWithMD5AndDES")
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

	
	 protected File getFileFromHashValue(Volume volume, String hash, String extension) { 
		 String filename = volume.getPath() + File.separatorChar +  hash.substring(0, 3) + 
		 		File.separator + hash.substring(3, 6) + File.separator + hash + extension;
		 logger.debug("getMessageFileName() { return='"+filename+"'");
		 return new File(filename);
	 }
	 
	 protected File getLegacyFileFromHashValue(Volume volume, String hash, int directoryLength, String extension) { // legacy
		 String filename = volume.getPath() + File.separatorChar + hash.substring(0, directoryLength) + 
		 		File.separator + hash + extension;
		 logger.debug("getMessageFileName() { return='"+filename+"'");
		 return new File(filename);
	 }
	 
    public File getExistingFile(Volume volume, String hash, String extension) throws MessageStoreException
    {
        File file = getFileFromHashValue(volume,hash,extension);
        if (!file.exists()) {
    		file = getLegacyFileFromHashValue(volume,hash,8,extension);
	        if (!file.exists()) {
	        	file = getLegacyFileFromHashValue(volume,hash,3,extension);
	        	if (!file.exists()) {
	            	file = getLegacyFileFromHashValue(volume,hash,6,extension);
	            	if (!file.exists()) {
	            		file = getLegacyFileFromHashValue(volume,hash,4,extension);
	            	}            	
	            }
	        }
        }
        logger.debug("getMessageFileName() {return='" + file.getAbsolutePath() + "'}");
        return file;
    }
    
    public File getNewFile(Volume volume, String hash, String extension) throws MessageStoreException {
    	prepareStore(volume);
        createDirectories(volume,hash);
    	File file =  getFileFromHashValue(volume, hash,extension);
    	logger.debug("getFile() {return='"+file.getAbsolutePath()+"'}");
    	return file;
    }

    /** 
      * Retrieve the file location of a message that could not be indexed
	  * @param emailID The email ID
	  * @return The file location message not processed
	  */  
    
    protected File getNoIndexFile(EmailID emailID) throws MessageStoreException {
        if (emailID==null || emailID.getUniqueID()==null)
            throw new MessageStoreException("assertion failure: null emailID or uniqueId",logger);
        String filename = Config.getFileSystem().getNoIndexPath() + File.separatorChar + emailID.getUniqueID() + messageFileExtension;
        logger.debug("getNoIndexFileName() {return='" + filename + "'}");
        return new File(filename);
    }
    
  
  protected File getNoArchiveFile(Email email) throws MessageStoreException {
	   logger.debug("getNoArchiveFileName() ()");
	   String filename;
	    try
	  	{
	    	filename = DateUtil.convertDatetoString(new Date());
	  	} catch (Exception e)
	  	{
	  		logger.error("failed to generate a uniqueid for a message");
	  		return null;
	  	}
      logger.debug("ArchiveFileName() {return='" + filename + "'}");
      return new File(Config.getFileSystem().getNoArchivePath()  + File.separatorChar + EmailID.generateUniqueID(email)+ ".mrc");
  }
   
   
   
    /** 
	   * Create the directory where a volume is stored 
	   * @param volume The volume
	   * @return The directory
	   */  

    public void prepareStore(Volume volume) throws MessageStoreException {

       if (volume==null)
           throw new MessageStoreException("assertion failure: null volume",logger);

       logger.debug("createMessageStoreDir() {" + volume + "}");

       File storeDir = new File(volume.getPath());
       if(!storeDir.exists())
       {
           logger.info("message store directory does not exist {"+volume+"}");
           boolean success = storeDir.mkdir();
           if(!success)
               throw new MessageStoreException("failed to create message store directory {" + volume + "}", logger);
           logger.info("created message store directory {" + volume + "}");
       }
    }

    /** 
	   * Create the directory where a message is stored
	   * @param emailID The email ID
	   * @return The message directory
	   */  
    
    private void createDir(String directory) throws MessageStoreException  {
    	 logger.debug("createDir() {messageDir='" + directory + "'}");
         File todayDir = new File(directory);
         if(!todayDir.exists())
         {
             logger.debug("message sub-directory does not exist {dir='" + directory + "'}");
             boolean makedir = todayDir.mkdir();
             if(makedir)
                 logger.debug("created message sub-directory {dir='" + directory + "'}");
             else {
            	 if(!todayDir.exists())
            		 throw new MessageStoreException("failed to create directory {dir='" + directory + "'}",logger);
             }
         } else
         {
             logger.debug("directory exists {dir='" + directory + "'}");
         }
    }

    protected String createDirectories(Volume volume, String hashValue) throws MessageStoreException
    {
    	String messageDirLevel1 = volume.getPath() + File.separatorChar + hashValue.substring(0, 3);
    	createDir(messageDirLevel1);
    	String messageDirLevel2 = messageDirLevel1 + File.separatorChar + hashValue.substring(3, 6);
    	createDir(messageDirLevel2);
    	logger.debug("createDirectories() {messageDir='" + messageDirLevel2 + "'}");
        return messageDirLevel2;
    }

    
   
    /** 
	   * Insert a new message in the store
	   * @param emailID The email ID
	   * @param in The message contents
	   * @param compress Is message compressed
	   * @param encrypt Should encrypt message
	   */  
    
    public boolean insertMessage(EmailID emailId, Email email) throws MessageStoreException
    {
    
        Config config = Config.getConfig();
        
       if (emailId==null || emailId.getVolume()==null || emailId.getUniqueID()==null)
           throw new MessageStoreException("assertion failure: null emailID, volume or uniqueId",logger);

       if(emailId.getUniqueID() == null)
            throw new MessageStoreException("insert message was found to have a null message id.", logger);

       logger.debug("insertMessage {"+emailId + "}");
       
      if (!isDefaultPassPhraseModified())
           throw new MessageStoreException("failed to archive message. encryption password is not set. {"+emailId+"}",logger);

       File messageFile = getNewFile(emailId.getVolume(),emailId.getUniqueID(),messageFileExtension);
       
   
       try {
    	   email.setHeader("X-MailArchiva-Archive-Date", DateUtil.convertDatetoString(new Date()));
       } catch (MessagingException me) {
    	   logger.error("failed to set archive date");
       }
       try {
    	 
    	   writeEmail(email,messageFile,true,true,true);
       } catch (Exception e) {
    	   messageFile.delete();
    	   try {
    		   writeCorruptedEmail(email, getQuarantineFile(emailId.getUniqueID()+".eml"));
    	   } catch (Exception e2) {
    		   throw new MessageStoreException("failed to write corrupted email to quarantine {"+emailId+"}",e2,logger);
    	   }
       }
       return true;
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
    
    
    public boolean isMessageExist(EmailID emailID) {
    	try {
    		File messageFile = getExistingFile(emailID.getVolume(),emailID.getUniqueID(),messageFileExtension);
    		return messageFile.exists();
    	} catch (Exception e) {
    		return false;
    	}
    }
    
 
    
    /** 
	   * Retrieve a message from the store
	   * @param emailID The email ID
	   * @param decompress Should decompress message
	   * @param decrypt Should dencrypt message
	   * @param headersOnly Should return just headers
	   * @return An email message
	   */  
    
    public Email retrieveMessage(EmailID emailID) throws MessageStoreException {

        if (emailID==null || emailID.getVolume()==null || emailID.getUniqueID()==null)
            throw new MessageStoreException("assertion failure: null emailID, volume or uniqueId",logger);

        logger.debug("retrieveMessage() {"+emailID+"'}");
     
     
        Email message = null;
        try {
        	
   			  File messageFile = getExistingFile(emailID.getVolume(),emailID.getUniqueID(),messageFileExtension);
   			  logger.debug("returning input stream {filename='" + messageFile + "'}");
   		     
	   		  message = new Email(emailID,getRawMessageInputStream(messageFile, true, true));
  
            logger.debug("retrieved message {"+message+"}");
        } catch (java.io.FileNotFoundException fnfe) {
        	throw new MessageStoreException("The message is currently not accessible on the storage device.",fnfe,logger);
        } catch(Exception e) {
            throw new MessageStoreException("Retrieved message does not appear to be well formed.", e, logger);
        }
        return message;

    }
    
   
    
    public void copyEmail(File source, File dest) throws MessageStoreException {
       
        logger.debug("copyEmail()");
        FileChannel in = null, out = null;

        try {   
        	
             in = new FileInputStream(source).getChannel();
             out = new FileOutputStream(dest).getChannel();
             in.transferTo( 0, in.size(), out);
            
        } catch (Exception e) {
            throw new MessageStoreException("failed to copy email {src='"+source+"=',dest='"+dest+"'",e,logger);
        } finally {
             if (in != null) try { in.close(); } catch (Exception e) {};
             if (out != null) try { out.close(); } catch (Exception e) {};
        }
    }
    

    public void backupMessage(Email email) throws MessageStoreException {
    	logger.debug("backupMessage() {"+email+"}");
    	if (email==null) {
            logger.error("assertion failure: email is null");
            return;
    	}
    	File noArchiveFile = getNoArchiveFile(email);
    	logger.warn("copying email to no archive queue {dest='"+noArchiveFile.getAbsolutePath()+"'}");
    	
    	try {
     	   writeEmail(email, noArchiveFile,false,false,false);
        } catch (Exception e) {
     	   throw new MessageStoreException("failed to write message to store {"+email+"}",logger);
        }
       
    }
    
    /** 
	   * Copy a message to a error directory if the message cannot be indexed 
	   * @param emailID The email ID
	   */  
	  public void backupMessage(EmailID emailID) throws MessageStoreException {
	      if (emailID==null || emailID.getVolume()==null || emailID.getUniqueID()==null)
	          throw new MessageStoreException("assertion failure: null emailID, volume or uniqueId",logger);
	      logger.debug("backupMessage() {"+emailID+"'");
	      copyEmail(getExistingFile(emailID.getVolume(),emailID.getUniqueID(),messageFileExtension),getNoIndexFile(emailID));   
	  }
	  
	  public void copyEmailToQuarantine(File sourceFile) throws MessageStoreException {
	  	logger.debug("copyEmailToQuarantine() {sourceFile='"+sourceFile+"'}");
	      copyEmail(sourceFile,getQuarantineFile(sourceFile.getName()));   
	  }
  
	  
	  public File getQuarantineFile(String filename) {
		  return  new File(Config.getFileSystem().getQuarantinePath() + File.separatorChar + filename);
	  }
    public int getNoMessagesForRecovery() {
    	File file = new File(Config.getFileSystem().getNoArchivePath());
    	if (file==null || !file.exists() || file.listFiles()==null) {
    		logger.warn("getNoMessagesForRecovery() cannot access no archive directory. returning zero.");
    		return 0;
    	}
    	return file.listFiles().length;
    }
    
    public void recoverMessages(RecoverMessage recover) throws MessageStoreException {
    	recover.start();
    	int total = 0;
    	int success = 0;
    	int failed = 0;
    	String notarchiveddir = Config.getFileSystem().getNoArchivePath();
        logger.debug("checking for failed messages that require rearchiving {notarchiveddirectory='"+notarchiveddir+"'}");
        File noarchiveDir = new File(notarchiveddir);
        if (!noarchiveDir.exists()) return;
        if (noarchiveDir.isDirectory()) {
              String[] children = noarchiveDir.list();
              if (children.length>0)
              	logger.warn("there are messages that require rearchival {notarchiveddirectory='"+notarchiveddir+"'}");
              total = children.length;
              for (int i=0; i<total; i++) {
                  String filepath = notarchiveddir+File.separatorChar+children[i];
                  logger.debug("attempting to recover file {path='" + filepath +"'}");	                
                  Email message = null;
                  try {
                	  InputStream is = getRawMessageInputStream(new File(filepath), false, false);
                	  logger.debug("retrieved inputstream {path='" + filepath +"'}");
                	  if (recover.recover(is,children[i])) {
  			  	    	success++;
  			  	    	is.close();
  	  	            	logger.info("message has been rearchived {"+message+", filepath='" + filepath +"'}");
		  	  	         try {
		  		              File delFile = new File(filepath);
		  			  	      boolean deleted;
		  			  	      delFile.deleteOnExit();
		  			  	      deleted = delFile.delete();
		  			  	      if (!deleted)
		  			  	    	  delFile.renameTo(File.createTempFile("oldrecovery", "tmp"));   
		  	              } catch (IOException io) {
		  	            	  logger.error("failed to delete email {filepath='"+filepath+"'");
		  	              }
  			  	      } else {
  			  	    	failed++;
  			  	    	is.close();
  			  	    	logger.error("failed to rearchive message.");
  			  	      }
  			  	      
                  } catch (Exception io) {
      	  	        logger.error("failed to recover message. {filename='"+filepath+"'}",io);
      	  	        failed++;
      	  	      }  
              }
        }	
        recover.end(failed,success,total);
    }
    
    public int getNoQuarantinedMessages() {
    	return new File(Config.getFileSystem().getQuarantinePath()).listFiles().length;
    }
    
    public void quarantineMessages() {
    	String notarchiveddir = Config.getFileSystem().getNoArchivePath();
    	logger.debug("quarantineEmails {noarchivedpath='"+notarchiveddir+"'}");
    	File noarchiveDir = new File(notarchiveddir);
        if (!noarchiveDir.exists()) return;
        if (noarchiveDir.isDirectory()) {
              String[] children = noarchiveDir.list();
              if (children.length>0)
              	logger.warn("there are messages that require rearchival {notarchiveddirectory='"+notarchiveddir+"'}");
              for (int i=0; i<children.length; i++) {
                  String filepath = notarchiveddir+File.separatorChar+children[i];
                  logger.debug("attempting to quarantine file {path='" + filepath +"'}");	 
	              try {
	            	  copyEmailToQuarantine(new File(filepath)); 
	              } catch (Exception e) {
	            	  logger.error("failed to quarantine email (filepath='"+filepath+"'}",e);
	            	  continue;
	              }
	              try {
		              File delFile = new File(filepath);
			  	      boolean deleted;
			  	      deleted = delFile.delete();
			  	      if (!deleted)
			  	    	  delFile.renameTo(File.createTempFile("oldrecovery", "tmp"));   
			  	      delFile.deleteOnExit();
	              } catch (IOException io) {
	            	  logger.error("failed to delete email {filepath='"+filepath+"'");
	              }
              }
        }
    }
    
   
    
  
  
    /** 
	   * Get a raw input stream for a message
	   * @param emailID The email ID
	   * @param decompress Should decompress message
	   * @param decrypt Should decrypt message
	   * @return An inputstream containing the message
	   */  
   public InputStream getRawMessageInputStream(File messageFile, boolean decompress, boolean decrypt)  throws IOException,MessageStoreException {
       if (messageFile==null )
           throw new MessageStoreException("assertion failure: null messageFileName",logger);

       InputStream is = new BufferedInputStream(new FileInputStream(messageFile));
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
   
   public InputStream getMessageInputStream(EmailID emailID) throws IOException, MessageStoreException {
	   
	   Email email = retrieveMessage(emailID); 
	   File file = File.createTempFile("raw", ".tmp");
	   tempfiles.markForDeletion(file);
	   try {
		   writeEmail(email, file,false,false,false);
	   } catch (Exception e) {}
		   return getRawMessageInputStream(file, false, false);
   }
   
   /** 
	   * Get a raw output stream for a message
	   * @param messageFileName The file name of the message
	   * @param compress Should compress message
	   * @param encrypt Should encrypt message
	   * @return An outputstream directed to the message
	   */  
   
   public OutputStream getRawMessageOutputStream(File messageFile,boolean compress, boolean encrypt) throws IOException,MessageStoreException {
       if (messageFile==null)
           throw new MessageStoreException("assertion failure: null messageFileName",logger);

       OutputStream os = new BufferedOutputStream(new FileOutputStream(messageFile));
       Cipher ecipher = null; 
       if (encrypt) {
           try {
               ecipher = Cipher.getInstance(key.getAlgorithm());
               ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
           } catch (Exception e) {
                logger.fatal("Please ensure you have the Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files installed.");
                logger.fatal("Visit http://java.sun.com2/javase/downloads/index.jsp to download them.");
                throw new MessageStoreException("failed to initalize cipher. Cause:",e,logger);
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
        
        
        logger.debug("start(). starting to process messages for indexing.");
        
        FileFilter filter = new MessageStore.MessageFileFilter(new String[] {messageFileExtension});


  		String storePath =  process.workingVolume.getPath();
    	File storeDirectory = new File(storePath);
      
    
    	if(storeDirectory != null && storeDirectory.isDirectory())
        	recurseMessages(storeDirectory, filter, process);
    }

   
    protected void recurseMessages(File file, FileFilter filter, ProcessMessage process) throws ProcessException
    {
        if(file != null && filter != null)
            if(file.isDirectory()) {
                File files[] = file.listFiles(filter);
                if(files != null) {
                    for(int i = 0; i < files.length; i++)
                        if(files[i].isDirectory())
                            recurseMessages(files[i], filter, process);
                        else {
                        	recurseMessages(files[i], filter, process);
						}
                }
            } else {
                	logger.debug("unique file name:"+file.getName());
                	Email email = null;
                	EmailID emailID = EmailID.getEmailID(process.workingVolume, getUniqueIdFromFileName(file.getName()));
      
                	try {
                    	email = retrieveMessage(emailID);
                    	process.process(process.workingVolume, email);  
                	} catch (Exception e) {
                    	logger.error("failed to process message during re-indexing {"+emailID+"}",e);
                    }  catch (OutOfMemoryError ome) {
                    	logger.error("server has run out of memory. skipping message.",ome);
                    }
   
            }
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
                if(file != null && file.exists() && file.getName().endsWith(ext[i]))
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
    
    public boolean findSignature(Object part) throws Exception {
    	boolean foundSignature = false;
    	if (part instanceof Multipart) {
    		Multipart multipart = (Multipart)part;
    		for (int i=0, n=multipart.getCount(); i<n; i++) 
    			if (findSignature(multipart.getBodyPart(i)))
    				foundSignature =true;
    	}  else if (part instanceof MimeMessage) {
    		if (findSignature(((MimeMessage)part).getContent()))
				foundSignature =true;
    	} else if (part instanceof MimeBodyPart) {
    		MimeBodyPart mpb = (MimeBodyPart)part;
    		String contentType = mpb.getContentType();
    		if (contentType.toLowerCase(Locale.ENGLISH).contains("multipart/signed"))
    			foundSignature = true;
    	}
    	return foundSignature;
    }
  
	public void writeCorruptedEmail(MimeMessage message, File file) throws MessageStoreException {
		logger.debug("writeCorruptedEmail");
		InputStream is = null;
		OutputStream os = null;
		try {
            logger.debug("writing corrupted email to quarantine {filename='" + file + "'}");
            os = new BufferedOutputStream(new FileOutputStream(file));
            is = message.getRawInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            int c = 0;
            while ((c = bis.read()) != -1) {
            	os.write(c);
            }
            try {
				if (os != null) os.close();
				if (is != null) is.close();
			} catch (Exception e) {}
		} catch (Exception ex) {
			throw new MessageStoreException("failed to write corrupted email to quarantine:"+ex.getMessage(),ex,logger);
		}
		
	}
	
	public void saveEmailChanges(MimeMessage message) throws MessagingException {
		logger.debug("saveEmailChanges");
		String[] messageId = message.getHeader("Message-Id");
		//try { System.out.println("Content:"+message.getContent()); } catch (Exception e) {}
		message.saveChanges();
		if (messageId!=null && messageId.length>0)
			message.setHeader("Message-Id", messageId[0]);
	}
		
    public String getHeader(MimeBodyPart mbp, String name) throws MessagingException {
    	String header[] = mbp.getHeader(name);
    	if (header != null && header.length>0)
    		return header[0];
    	else 
    		return null;
    }
   

  public File getExistingAttachmentFilePath(Volume volume, String hash) throws MessageStoreException {
	  logger.debug("getAttachmentFilePath() {"+volume+",hash='"+hash+"'}");
	  File attachFile = getFileFromHashValue(volume, hash,attachmentFileExtension);
  	if (!attachFile.exists()) {
  		attachFile = getLegacyFileFromHashValue(volume,hash,3,attachmentFileExtension);
  		if (!attachFile.exists()) {
  			throw new MessageStoreException("attachment does not exist {attachFile='"+attachFile.getAbsolutePath()+"'}",logger,Level.DEBUG);
  		}
  	}
  	return attachFile;
  }
  
 

    public static void copy(File source, File dest) throws IOException {
	     FileChannel in = null, out = null;
	     try {          
	          in = new FileInputStream(source).getChannel();
	          out = new FileOutputStream(dest).getChannel();
	 
	          long size = in.size();
	          MappedByteBuffer buf = in.map(FileChannel.MapMode.READ_ONLY, 0, size);
	 
	          out.write(buf);
	 
	     } finally {
	          if (in != null)          in.close();
	          if (out != null)     out.close();
	     }
	}
 
	

    private String toHex(byte[] block) {
		StringBuffer buf = new StringBuffer();

		for (int i = 0; i < block.length; ++i) {
			buf.append(hexits.charAt((block[i] >>> 4) & 0xf));
			buf.append(hexits.charAt(block[i] & 0xf));
		}

		return buf + "";
	}

    
    protected void updateDiskSpace(Volume volume, long fileLength) {
    	// we have written to the drive, update disk space counters
			long indexChange = (long)(fileLength*0.1); // rough approximation index %1 size store
			logger.debug("inc volume store disk space {storeinc='"+fileLength+"',indexinc='"+indexChange+"'}");
			volume.setFreeArchiveSpace(volume.getFreeArchiveSpace()-fileLength);
			volume.setUsedArchiveSpace(volume.getUsedArchiveSpace()+fileLength);
			volume.setUsedIndexSpace(volume.getUsedIndexSpace()+indexChange);
			volume.setFreeIndexSpace(volume.getFreeIndexSpace()-indexChange);
    }
    
	public void writeEmail(MimeMessage message, File file, boolean compress, boolean encrypt, boolean vol) throws  MessageStoreException {
		logger.debug("writeEmail");
		try {
			OutputStream fos = getRawMessageOutputStream(file,compress,encrypt);
			message.writeTo(fos);
			fos.close();
			Volume activeVolume = Config.getConfig().getVolumes().getActiveVolume();
			if (activeVolume!=null && activeVolume.isDiskSpaceChecked() && vol) {
				updateDiskSpace(activeVolume,file.length());
			}
	
		} catch (Exception e) {
			if (file.exists()) {
				 boolean deleted = file.delete();
				   if (!deleted) {
					   try {
						   file.renameTo(File.createTempFile("ma", "tmp"));
						   Config.getFileSystem().getTempFiles().markForDeletion(file);
					   } catch (Exception e3) {}
			   	   }
			}
			throw new MessageStoreException("failed to write email {filename='"+file.getAbsolutePath()+"'",e,logger);
		}
		/*
		try {
			//System.out.println("WRITEMAIL:"+message.getContent()+"XXXXXXXXXXXXXXXXXXXXXX");	
			FileOutputStream fos2 = new FileOutputStream("c:\\test.eml");
			message.writeTo(fos2);
			fos2.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}*/
	}

    public void writeTo(MimePart part, OutputStream os, String[] includeList) throws IOException, MessagingException {
		LineOutputStream los = null;
		if (os instanceof LineOutputStream) {
			los = (LineOutputStream) os;
		} else {
			los = new LineOutputStream(os);
		}
		Enumeration hdrLines = part.getMatchingHeaderLines(includeList);
		while (hdrLines.hasMoreElements()) {
			String line = (String)hdrLines.nextElement();
			los.writeln(line);
		}
		los.writeln();
		os = MimeUtility.encode(os, part.getEncoding());
		part.getDataHandler().writeTo(os);
		os.flush(); 
    }

   
    
    
}

