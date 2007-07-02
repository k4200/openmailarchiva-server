
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


package com.stimulus.archiva.index;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.*;
import javax.mail.*;
import org.apache.log4j.Logger;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.exception.*;
import com.stimulus.archiva.store.*;
import com.stimulus.archiva.language.*;
import com.stimulus.archiva.extraction.*;
import com.stimulus.archiva.search.*;
import com.stimulus.archiva.service.*;
import com.stimulus.util.TempFiles;
import javax.mail.internet.*;

public class MessageIndex {
	  protected static final Logger logger = Logger.getLogger(MessageIndex.class.getName());
	  protected LinkedList<EmailID> indexQueue = new LinkedList<EmailID>();
	  protected static int INDEX_WAIT_PERIOD = 500;
	  protected boolean shutdown = false;
	  protected MessageStore ms = null;
	  private SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
	  protected static LanguageIdentifier languageIdentifier = new LanguageIdentifier();

      
	  public MessageIndex(MessageStore ms) {
	      this.ms = ms;
	      new IndexWorker().start();
	  }
	  
	  public long getTotalMessageCount(Volume volume) throws MessageSearchException {
		  if (volume == null)
	            throw new MessageSearchException("assertion failure: null volume",logger);
		  logger.debug("get total no emails {indexpath='"+volume.getIndexPath()+"'}");
	      File indexDir = new File(volume.getIndexPath());
	      if (!indexDir.exists())
	    	  	return 0;
	      IndexReader indexReader = null;
	      try {
	    	  indexReader = IndexReader.open(indexDir);
	      } catch (IOException e ) {
	    	  throw new MessageSearchException("failed to open index to calculate total email count",e,logger);
	      }
	      return indexReader.numDocs();
	  }
	  
//	 deliberately non recursive (so we avoid situations where the whole h/d is deleted)
	  public void deleteIndex(Volume volume) throws MessageSearchException {
	      if (volume == null)
	            throw new MessageSearchException("assertion failure: null volume",logger);

	      logger.debug("delete index {indexpath='"+volume.getIndexPath()+"'}");
	      File indexDir = new File(volume.getIndexPath());
	      if (!indexDir.exists()) return;
	      if (indexDir.isDirectory()) {
	            String[] children = indexDir.list();
	            for (int i=0; i<children.length; i++) {
	                String filepath = volume.getIndexPath()+File.separatorChar+children[i];
	                logger.debug("deleting file {path='" + filepath +"'}");
	                boolean success = new File(filepath).delete();
	                if (!success)
                        throw new MessageSearchException("failed to delete file in existing index {filepath='"+filepath+"'}",logger);
                     else
                         logger.debug("deleted file successfully {filepath='" + filepath +"'}");
	            }
	      }
	  }


	  private class IndexWorker extends Thread {

	    	IndexWriter writer 		= null;
    		ArchivaAnalyzer analyzer = new ArchivaAnalyzer();
    		Volume volume 			= null;
    		
    		boolean	volumeOpened   = false;

	        public void run() {

	            while (!shutdown) {
	                	Email email = null;
	                	volumeOpened = false;
	                	 while (!indexQueue.isEmpty()) {
	           		          EmailID emailId = (EmailID)indexQueue.removeLast();
	           		          Volume newVolume = emailId.getVolume();
	           		          try {
		           		          if (!volumeOpened || newVolume!=volume) {
		           		              volume = newVolume;
		           		              openIndex(volume);
		           		              volumeOpened = true;
		           		          }
	           		              email = ms.retrieveMessage(emailId, true, true, false);
	           		              index(email);
	           		          } catch (MessageStoreException mste) {
	           		              try { ms.copyEmailToNoIndexQueue(emailId); } catch (Exception e) {};
	           		              logger.debug("failed to retrieve message for indexing {"+emailId+"}");
	           		          } catch(MessageSearchException mse) {
	           		           	  try { ms.copyEmailToNoIndexQueue(emailId); } catch (Exception e) {};
	           		              logger.error("failed to index message {"+email+"}");
	           		          }
           		      	}
		      	      if (volumeOpened)
        		          closeIndex();
		      	        try { Thread.sleep(INDEX_WAIT_PERIOD); } catch(Exception e) {}
	            }

	        }

	        public void openIndex(Volume volume) throws MessageSearchException {
	        	openIndexR(volume, false);
	        }
	       
	        protected void openIndexR(Volume volume, boolean retry) throws MessageSearchException {
	        		
	            if (volume == null)
		            throw new MessageSearchException("assertion failure: null volume",logger);
	            logger.info("opening index for write {"+volume+"}");
	            createIndexDir(volume);
 	    		logger.debug("opening search index for write {indexpath='"+volume.getIndexPath()+"'}");
 	    		try {
 	    			writer = new IndexWriter(volume.getIndexPath(), analyzer);
 	    			writer.setMergeFactor(2);
 	    			writer.setMaxMergeDocs(7000000);
 	    			//writer.setMinMergeDocs(5);
 	    	  	} catch (IOException io)
 	    		{
 	    	  		if (!retry) {
	 	    	  		// most obvious reason for error is that there is a lock on the index, due hard shutdown
	 	    	  	    // resolution delete the lock, and try again
	 	    	  	    logger.error("failed to open search index for write. possible write lock due to hard system shutdown.",io);
	 	    	  	    logger.info("attempting recovery. deleting index lock file and retrying..");
	 	    	  	    File lockFile = new File(volume.getIndexPath()+File.separatorChar + "write.lock");
	 	    	  	    lockFile.delete();
	 	    	  	    try {
	 	    	  	    	openIndexR(volume,true); 
	 	    	  	    } catch (MessageSearchException mse) {
	 	    	  	    	throw mse;
	 	    	  	    }
 	    	  		} else
 	    	  			throw new MessageSearchException("failed to open/create index writer {location='"+volume.getIndexPath()+"'}",io,logger);
 	    		}
	        }

	        public void index(Email message) throws MessageSearchException {
	            if (message == null)
		            throw new MessageSearchException("assertion failure: null message",logger);

    	    	logger.debug("indexing message {"+message+"}");
    	    	Document doc = new Document();
    	    	TempFiles tempFiles = new TempFiles();
	    		try {
	    		   writeMessageToDocument(message,doc,tempFiles);  
                   String language = doc.get("lang");
	    		   if (language==null)
	    		       writer.addDocument(doc);
                   else
                       writer.addDocument(doc,AnalyzerFactory.getAnalyzer(language));
	    	 	   logger.debug("message indexed successfully {"+message+",language='"+language+"'}");
	    		} catch (MessagingException me)
	    		{
	    		   throw new MessageSearchException("failed to decode message during indexing",me,logger);
	    		} catch (IOException me) {
	    		    throw new MessageSearchException("failed to index message {"+message+"}",me,logger);
	    		} catch (ExtractionException ee)
	    		{
	    		   throw new MessageSearchException("failed to decode attachments in message {"+message+"}",ee,logger);
	    		} catch (Exception e) {
	    		    throw new MessageSearchException("failed to index message",e,logger);
	    		}
	        }

	        public void closeIndex() {
	            try
    			{
    				logger.debug("closing index");
    				if (writer!=null)
    				    writer.close();
    			} catch (Exception io) {}
	        }
	  }
	  
	  protected String getNormalizedMimeType(String mimeType)
	  {
	  	// check this code may be a dodgy
		int index = mimeType.indexOf(";");
		if (index!=-1)
			mimeType = mimeType.substring(0,index);
		mimeType.toLowerCase();
		mimeType.trim();
	  	return mimeType;
	  }
	  
	  private String stripEmailChars(String email) {
		  if (email!=null && email.length()>0) {
			  email = email.replaceAll("'", "").replaceAll("\"", "");
			  // < will interfere will sort, so we remove
			  if (email.charAt(0)=='<')
				  email = email.replaceAll("<", "").replaceAll(">","");
		  } else email = ""; // should not happen
		  return email;
	  }


	  protected void writeMessageToDocument(Email message, Document doc, TempFiles tempFiles) throws MessagingException,IOException,ExtractionException
	  {
	      if (message.getEmailID()==null)
		  	     throw new MessagingException("failed to index message as unique ID is null");

	  	 String uniqueID 	= message.getEmailID().getUniqueID();
		 String subject 	= message.getSubject();
		 String to 			= message.getToAddresses(Email.DisplayMode.ALL);
		 String from 		= message.getFromAddress(Email.DisplayMode.ALL);
		 String cc 			= message.getCCAddresses(Email.DisplayMode.ALL);
		 String bcc 		= message.getBCCAddresses(Email.DisplayMode.ALL);
	
		 Date sent 			= message.getSentDate();
		 int size 			= message.getSize() / 1024;
         int attach         = message.hasAttachment() ? 1 : 0;
         int priority       = message.getPriorityID();
         List<String> flags = message.getFlagList();
        
         String flagStr = "";
         for (String flag : flags)
        	 flagStr += " " + flag;
         
		 logger.debug("writing message to document {contenttype='" + message.getContentType()+"', "+message+"}");
         
		 // index version
		 doc.add(new Field("ver","2",Field.Store.YES,Field.Index.UN_TOKENIZED));    
         doc.add(new Field("uid",uniqueID,Field.Store.YES,Field.Index.UN_TOKENIZED));        
        
		 if (subject!=null) {
             doc.add(new Field("subject",subject,Field.Store.NO,Field.Index.TOKENIZED));
             doc.add(new Field("subjects",subject,Field.Store.YES,Field.Index.UN_TOKENIZED));
         }
         
		 if (to!=null) {
             doc.add(new Field("to",to,Field.Store.NO,Field.Index.TOKENIZED));
             doc.add(new Field("tos",stripEmailChars(to),Field.Store.YES,Field.Index.UN_TOKENIZED));
         }
		    
		 if (from!=null) {
             doc.add(new Field("from",from,Field.Store.NO,Field.Index.TOKENIZED));
             doc.add(new Field("froms",stripEmailChars(from),Field.Store.YES,Field.Index.UN_TOKENIZED));
         }
         
         if (bcc!=null) {
             doc.add(new Field("bcc",bcc,Field.Store.NO,Field.Index.TOKENIZED));
             doc.add(new Field("bccs",stripEmailChars(bcc),Field.Store.YES,Field.Index.UN_TOKENIZED));
         }
         
         if (cc!=null) {
             doc.add(new Field("cc",cc,Field.Store.NO,Field.Index.TOKENIZED));
             doc.add(new Field("ccs",stripEmailChars(cc),Field.Store.YES,Field.Index.UN_TOKENIZED));
         }
         
		 if (sent!=null) {
		     String sentDateStr = "d"+format.format(sent);
             doc.add(new Field("sentdate",sentDateStr,Field.Store.YES,Field.Index.UN_TOKENIZED));
		 }
         
         doc.add(new Field("size",Integer.toString(size),Field.Store.YES,Field.Index.UN_TOKENIZED));
         doc.add(new Field("priority",Integer.toString( priority ),Field.Store.YES,Field.Index.UN_TOKENIZED));
         doc.add(new Field("attach",Integer.toString( attach ),Field.Store.YES,Field.Index.UN_TOKENIZED));
         doc.add(new Field("flag",flagStr,Field.Store.YES,Field.Index.TOKENIZED));
		 Object bodyPart = message.getContent();
	  	 writeBodyPartsToDocument(bodyPart,bodyPart,message.getContentLanguage(),message.getContentType(),message.getDisposition(),doc,tempFiles);
	  	 
		 Config config = ConfigurationService.getConfig();
		  
		 String lang = doc.get("lang");
		 if (lang==null) {
	        	lang = config.getIndexLanguage();
	        	logger.debug("failed to detect language during indexing. using default language. {defaultlanguage='"+lang+"'}");
	        	doc.add(new Field("lang",lang,Field.Store.YES,Field.Index.UN_TOKENIZED));  
	        } else {
	        	logger.debug("language detected during indexing {language='"+lang+"'}");
	      } 
	  }

	  protected void writeBodyPartsToDocument(Object bodyPart, Object bodyPartBackup, String[] languages, String mimeTypeA, String disposition, Document doc, TempFiles tempFiles)throws MessagingException,IOException
	  {
	    String mimetype = getNormalizedMimeType(mimeTypeA);
	    logger.debug("extracting message part {mimetype = '"+mimetype+"'}");

	    if (bodyPart instanceof String) {
	  		String bodyPartStr = (String)bodyPart;
	  		byte[] bodyBytes = bodyPartStr.getBytes();
	  		extractAndIndex(new ByteArrayInputStream(bodyBytes),new ByteArrayInputStream(bodyBytes),languages,mimetype,disposition,doc,tempFiles);
        } else if (bodyPart instanceof InputStream) {
            extractAndIndex((InputStream)bodyPart,(InputStream)bodyPart, languages,mimetype,disposition, doc, tempFiles);
        } else if (bodyPart instanceof Multipart) {
        	logger.debug("message contains a multiple parts.");
            Multipart mp = (Multipart)bodyPart;
            for (int j = 0; j < mp.getCount(); j++) {
            	writeBodyPartsToDocument(mp.getBodyPart(j).getContent(),mp.getBodyPart(j).getContent(),languages,mp.getBodyPart(j).getContentType(),mp.getBodyPart(j).getDisposition(),doc,tempFiles);
            }
        } else if (bodyPart instanceof MimeMessage) {
        		MimeMessage message = (MimeMessage)bodyPart;
    			writeBodyPartsToDocument(message.getContent(), message.getContent(), languages, mimetype, disposition, doc, tempFiles);
        } else
        	logger.debug("failed to identify message part. {class='"+bodyPart.getClass().getSimpleName()+"'}");
	  }
	  
	  protected void extractAndIndex( InputStream is, InputStream is2, String languages[], String mimetype, String disposition, Document doc,TempFiles tempFiles) {
		  try {
			  if (disposition!=null && disposition.equalsIgnoreCase(Part.ATTACHMENT)) {
				  if (mimetype.compareToIgnoreCase("application/zip")==0) 
					  extractAndIndexZipFile(is, doc, tempFiles);
				  else {
					  Reader textReader = Extractor.getText(is,mimetype,tempFiles);
					  if (textReader!=null)
						  doc.add(new Field("attachments", textReader));
				  }
			  } else {
				  Reader textReader = Extractor.getText(is,mimetype,tempFiles);
				  if (textReader!=null) {
					  doc.add(new Field("body", textReader));
		          	  Reader detectReader = Extractor.getText(is2,mimetype,tempFiles);
		              addLanguage(languages, doc, detectReader);     
				  }
			  }
		  } catch (Exception ee) {
	          logger.debug("failed to decode message part content (mimetype unsupported?) {mimetype='"+mimetype+"'}",ee);
	          return;
		  } 
		  logger.debug("message part content decoded successfully");
	  }
	  
	  protected void extractAndIndexZipFile(InputStream is, Document doc, TempFiles tempFiles) {
		  	ZipInputStream zis = new ZipInputStream(is);
		    ZipEntry entry;
         	try {
		    	while((entry = zis.getNextEntry()) != null) {
		             String name = entry.getName();
		             int dot = name.lastIndexOf('.');
		             if (dot==-1) continue;
		             String extention = name.substring(dot+1,name.length());
		             try {
		                Reader textReader = Extractor.getText(zis,extention,tempFiles);
		                if (textReader!=null)
		                	doc.add(new Field("attachments", textReader));
		       	     } catch (ExtractionException ee) {
		       	              logger.debug("failed to decode message part content (mimetype unsupported?) {extention='"+extention+"'}",ee);
		       	     }
	         	}
	         	zis.close();
         	} catch (IOException io) {
         	    logger.error(io.getMessage(),io);
         	}
	  }
	  
	  protected void addLanguage(String[] languages, Document doc, Reader detectReader) {
		  Config config = ConfigurationService.getConfig();
          String lang;
          	if (config.getIndexLanguageDetection() && doc.get("lang")==null) {
              try {
            	  if (languages!=null && languages.length>0) {
            		  lang = languages[0].trim().toLowerCase();
            		  logger.debug("detected language from the email header. {language='"+lang+"'}");
            	  } else {
            		  logger.debug("email did not contain language header field. analyzing text to determine language.");
            		  lang = languageIdentifier.identify(detectReader);
            	  }
              } catch (Exception e) {
            	  logger.debug("exception occurred while detecting indexing language.",e);
            	  return;
              }
              if (lang!=null)
            	  doc.add(new Field("lang",lang,Field.Store.YES,Field.Index.UN_TOKENIZED));  
          	}
	  }
	

	  public void deleteMessage(Email email) throws MessageSearchException {

	  }

	  public void indexMessage(EmailID emailID) throws MessageSearchException {
	      if (emailID==null)
	            throw new MessageSearchException("assertion failure: null emailId",logger);
	      indexQueue.addFirst(emailID);
	  }

	  public String createIndexDir(Volume volume) throws MessageSearchException {

	      if (volume==null)
	            throw new MessageSearchException("assertion failure: null volume",logger);

	      File indexDir = new File(volume.getIndexPath());
          if (!indexDir.exists()) {
   			logger.info("index director2y does not exist. will proceed with creation {location='" + volume.getIndexPath() + "'}");
   			boolean success = indexDir.mkdir();
   			if (!success)
   					throw new MessageSearchException("failed to create index directory {location='" + volume.getIndexPath() + "'}",logger);
   			logger.info("index directory successfully created {location='" + volume.getIndexPath() + "'}");
   		  }
          return volume.getPath();
	  }
	  
}
