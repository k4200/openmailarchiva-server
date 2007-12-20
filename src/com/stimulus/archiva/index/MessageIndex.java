/*
 * Subversion Infos:
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
*/

		
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;
import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.domain.Email;
import com.stimulus.archiva.domain.EmailID;
import com.stimulus.archiva.domain.Volume;
import com.stimulus.archiva.domain.fields.EmailField;
import com.stimulus.archiva.domain.fields.EmailFieldValue;
import com.stimulus.archiva.exception.ExtractionException;
import com.stimulus.archiva.exception.MessageSearchException;
import com.stimulus.archiva.exception.MessageStoreException;
import com.stimulus.archiva.extraction.Extractor;
import com.stimulus.archiva.language.AnalyzerFactory;
import com.stimulus.archiva.language.LanguageIdentifier;
import com.stimulus.archiva.search.ArchivaAnalyzer;
import com.stimulus.archiva.service.ConfigurationService;
import com.stimulus.archiva.store.MessageStore;
import com.stimulus.util.Compare;
import com.stimulus.util.TempFiles;

public class MessageIndex implements Serializable {
	
	  /**
	 * 
	 */
	private static final long serialVersionUID = -17692874371162272L;
	protected static Logger logger = Logger.getLogger(MessageIndex.class.getName());
	  protected LinkedList<EmailID> indexQueue = new LinkedList<EmailID>();
	  protected static int INDEX_WAIT_PERIOD = 50;
	  protected static int DEAD_PERIOD = 300000000;
	  protected boolean shutdown = false;
	  protected MessageStore ms = null;
	  protected static LanguageIdentifier languageIdentifier = new LanguageIdentifier();
	  IndexWorker worker = null;
	  protected Hashtable<Volume,VolumeIndex> volumeIndexes = new Hashtable<Volume,VolumeIndex>();
	  protected enum AccessStatus { OPEN, CLOSED };
	  ArchivaAnalyzer analyzer 	= new ArchivaAnalyzer();
	  transient Object indexLock = new Object();
	  
	  public MessageIndex(MessageStore ms) {
	      this.ms = ms;
	      worker = new IndexWorker("IndexWorker");
	     
	      worker.start();
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
	  
	  public void shutdownIndexer() {
		  shutdown = true;
		  int attempt = 0;
	      while (worker.isAlive() && attempt < 10 ) { 
	    	  try { Thread.sleep(500); attempt++;  } catch (Exception e) {}
	      }
	      worker = null;
	  }
	  
	  public void startIndexer() {
		  shutdown = false;
		  worker = new IndexWorker("IndexWorker");
	      worker.start();
	  }
//	 deliberately non recursive (so we avoid situations where the whole h/d is deleted)
	  public void deleteIndex(Volume volume) throws MessageSearchException {
		  
	      if (volume == null)
	            throw new MessageSearchException("assertion failure: null volume",logger);

	      logger.debug("delete index {indexpath='"+volume.getIndexPath()+"'}");
	      // we need to stop indexing for a brief moment
	      shutdownIndexer();
	      VolumeIndex volumeIndex = getVolumeIndex(volume);
	      volumeIndex.closeIndex();
	      File indexDir = new File(volume.getIndexPath());
	      if (!indexDir.exists()) return;
	      if (indexDir.isDirectory()) {
	            String[] children = indexDir.list();
	            for (int i=0; i<children.length; i++) {
	                String filepath = volume.getIndexPath()+File.separatorChar+children[i];
	                logger.debug("deleting file {path='" + filepath +"'}");
	                File file = new File(filepath);
	                boolean success = file.delete();
	                if (!success) {
	                	try {
	                		File newFile = File.createTempFile("temp","idx");
	                		file.renameTo(newFile);
	                	} catch (Exception e) {
	                		throw new MessageSearchException("failed to delete file in existing index {filepath='"+filepath+"'}",logger);
	                	}
	                } else
                         logger.debug("deleted file successfully {filepath='" + filepath +"'}");
	            }
	      }
	      startIndexer();
	     
	  }


	  private class VolumeIndex {
		   
		  AccessStatus volumeOpened = AccessStatus.CLOSED;
		  IndexWriter writer = null;
		  Volume volume;
	
	
		  public VolumeIndex(Volume volume) {
			  this.volume = volume;
		  }
	
		  
		  public AccessStatus getAccessStatus() { return volumeOpened;}

		  public void setAccessStatus(AccessStatus volumeOpened) { this.volumeOpened = volumeOpened; }
		  

		  protected synchronized void openIndex() throws MessageSearchException {
			 
			    	if (getAccessStatus()==AccessStatus.CLOSED) {
			    		logger.debug("openIndex() index will be opened. it is currently closed.");
			    		openIndexR(false);
			    		setAccessStatus(AccessStatus.OPEN);
			    	} else 
			    		logger.debug("openIndex() did not bother opening index. it is already open.");
			 
		  } 
		   
		    protected void openIndexR(boolean retry) throws MessageSearchException {
		    		
		        if (volume == null)
		            throw new MessageSearchException("assertion failure: null volume",logger);
		        logger.debug("opening index for write {"+volume+"}");
		        createIndexDir(volume);
				logger.debug("opening search index for write {indexpath='"+volume.getIndexPath()+"'}");
				try {
					synchronized(indexLock) {
						writer = new IndexWriter(volume.getIndexPath(), analyzer);
						writer.setMergeFactor(2);
						writer.setMaxMergeDocs(7000000);
					}
					//writer.setMinMergeDocs(5);
			  	} catch (IOException io)
				{
			  		if (!retry) {
		    	  		// most obvious reason for error is that there is a lock on the index, due hard shutdown
		    	  	    // resolution delete the lock, and try again
		    	  	    logger.warn("failed to open search index for write. possible write lock due to hard system shutdown.",io);
		    	  	    logger.info("attempting recovery. deleting index lock file and retrying..");
		    	  	    File lockFile = new File(volume.getIndexPath()+File.separatorChar + "write.lock");
		    	  	    lockFile.delete();
		    	  	    try {
		    	  	    	openIndexR(true); 
		    	  	    } catch (MessageSearchException mse) {
		    	  	    	throw mse;
		    	  	    }
			  		} else
			  			throw new MessageSearchException("failed to open/create index writer {location='"+volume.getIndexPath()+"'}",io,logger);
				}
		    }
		
		    protected void index(EmailID emailId) throws MessageSearchException, MessageStoreException {
		    
		    	if (getAccessStatus()!=AccessStatus.OPEN) {
		    		logger.error("attempted to index an email on a closed volume");
		    		return;
		    	}
		    	
		    	Email message = ms.retrieveMessage(emailId);
		    	
		        if (message == null)
		            throw new MessageSearchException("assertion failure: null message",logger);
		
		        logger.debug("indexing message {"+message+"}");
		        
		        Document doc = new Document();
		        TempFiles tempFiles = Config.getTempFiles();
				
		        try {
				   writeMessageToDocument(message,doc,tempFiles);  
				   String language = doc.get("lang");
				   if (language==null)
					   language = Config.getConfig().getIndexLanguage();
					synchronized(indexLock) {
						writer.addDocument(doc,AnalyzerFactory.getAnalyzer(language,AnalyzerFactory.Operation.INDEX));
					}
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
				logger.debug("indexing message end {"+message+"}");
			
	
		    }
		
		    protected synchronized void closeIndex() {
		    	
			        if (getAccessStatus()==AccessStatus.CLOSED)
			        		return;
			        try {
						logger.debug("closing index");
						synchronized(indexLock) {
							if (writer!=null)
							    writer.close();
						}
					} catch (Exception io) {}
					setAccessStatus(AccessStatus.CLOSED);
		    	}
	
	  }
	  
	  protected VolumeIndex getVolumeIndex(Volume volume) {
		
		      	VolumeIndex vi = volumeIndexes.get(volume);
		      	if (vi==null) {
		      		logger.debug("new volume index");
		      		vi = new VolumeIndex(volume);
		      		volumeIndexes.put(volume, vi);
		      	}
		      	return vi;

      }
	  
	  protected void closeAllVolumes() {
			Iterator i = volumeIndexes.values().iterator();
        	while (i.hasNext()) {
        		VolumeIndex volumeIndex = (VolumeIndex)i.next();
        		volumeIndex.closeIndex();
        	}
	  }

	  protected class IndexAction extends Thread {

		  VolumeIndex vi; 
		  EmailID emailId; 
		  
		  public IndexAction(VolumeIndex vi, EmailID emailId,String name) {
			  super(name);
			  this.vi = vi;
			  this.emailId = emailId;
	
		  }
		  
		  public void run() {
			  try {
				  vi.index(emailId);
			  } catch (Exception e) {
				  logger.debug("failed to index message", e);
				  try { ms.copyEmailToNoIndexQueue(emailId); } catch (Exception e2) {};
			  }
		  }
		  
		  VolumeIndex getVolumeIndex() { return vi; }
	  }
	  
	  protected class IndexWorker extends Thread {

	    	IndexWriter writer 			= null;
    		ArchivaAnalyzer analyzer 	= new ArchivaAnalyzer();
    		Volume volume 				= null;
    		long t = 0;
    		public IndexWorker(String name) {
    			 setDaemon(true);
    			 setName(name);
    		}

	        public void run() {
	        	logger.debug("index worker is started");
       		 	int indexThreads = Config.getConfig().getIndexThreadsKey();
       		 
       		 	ArrayList<IndexAction> indexers = null;
       		 	
       		
	        	while (!shutdown) {
	        					try {	 
	        						 indexers = new ArrayList<IndexAction>();
	        						 
		            			 	 for (int i=0; i<indexThreads;i++) {
		            			 		 EmailID emailId = (EmailID)indexQueue.removeLast(); // will throw exception if not found
		            			 		 logger.debug("found new message in indexing queue {"+emailId+"}");
		            					 Volume volume = emailId.getVolume();
		            					 VolumeIndex vs = getVolumeIndex(volume);
		            					 vs.openIndex();	
		            					 t++;
			            			 	 IndexAction action =  new IndexAction(vs,emailId,"indexa"+t);
			           		        	 indexers.add(action);
			           		        	 action.start();
		            			 	 }
		            			
		            			 } catch (Exception e) {
		            			 } finally {
		            				    if (indexers.size()>0) {		            	
			            				 	for (IndexAction action: indexers) {
				            			 		try { action.join(DEAD_PERIOD); } catch(Exception e) {};
				            			 		
					           		        	 //	 here we try to recover from a situation where the index thread hangs
					           		        	 // best we can do is interrupt the thread, if that fails
					           		        	 // we set the priority to be low
					            			 	
					           		             if (action.isAlive()) {
					           		            	action.interrupt();
					           		            	 logger.debug("blocked thread interrupted");
						           		             try { action.join(200); } catch(Exception e) {};
					           		        		 if (action.isAlive()) {
					           		        			  logger.debug("set thread min priority");
					           		        			  action.setPriority(Thread.MIN_PRIORITY);
					           		        			  action.stop();
					           		        			  try { Thread.sleep(100); } catch (Exception e) {}
					           		        			  if (action.isAlive())
					           		        				 logger.debug("stuck thread still alive");
					           		        			  else
					           		        				 logger.debug("stuck thread thankfully stopped");
					           		        		 }
					           		        	 }
			            				 	}
					           		        for (IndexAction action: indexers) {
						           		            	 action.getVolumeIndex().closeIndex(); 
		            				    	}
		            				    }
			            				
			            				 try {  Thread.sleep(INDEX_WAIT_PERIOD); } catch(Exception e2) {}
				            		
		            			 }
	            }
	        }

	        
	        public Volume getVolume() { return volume; }
	  }
	  
	  protected String getNormalizedMimeType(String mimeType)
	  {
	  	// check this code may be a dodgy
		int index = mimeType.indexOf(";");
		if (index!=-1)
			mimeType = mimeType.substring(0,index);
		mimeType.toLowerCase(Locale.ENGLISH);
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
	  	 String volID		= message.getEmailID().getVolume().getID();
	  	 doc.add(new Field("ver","2",Field.Store.YES,Field.Index.UN_TOKENIZED));    
         doc.add(new Field("uid",uniqueID,Field.Store.YES,Field.Index.UN_TOKENIZED));   
         doc.add(new Field("vol",volID,Field.Store.YES,Field.Index.UN_TOKENIZED));   
         // legacy code
         boolean archiveDateFound = false;
	  	 Hashtable<String,EmailFieldValue> fields = message.getFields();
	  	 Enumeration e = fields.elements();
	  	 while (e.hasMoreElements()) {
	  		 
	  		 EmailFieldValue efv = (EmailFieldValue)e.nextElement();
	  		String fieldName = efv.getField().getName();
	  		
	  		String fieldValue = efv.getValue();
	  		
	  		 if (Compare.equalsIgnoreCase(efv.getField().getName(), "body") ||
	  			 Compare.equalsIgnoreCase(efv.getField().getName(), "attachments"))	 
	  			 continue; // we handle this later

	  		 EmailField.SearchMethod searchMethod = efv.getField().getSearchMethod();
	  		 
	  		 if (searchMethod==EmailField.SearchMethod.STORED)
	  			 doc.add(new Field(efv.getField().getIndexKey(),efv.getValue(),Field.Store.YES,Field.Index.UN_TOKENIZED));
	  		 else if (searchMethod==EmailField.SearchMethod.TOKENIZED)
	  			 doc.add(new Field(efv.getField().getIndexKey(),efv.getValue(),Field.Store.NO,Field.Index.TOKENIZED));
	  		 else if (searchMethod==EmailField.SearchMethod.TOKENIZED_AND_STORED) {
	  			doc.add(new Field(efv.getField().getIndexKey()+"s",efv.getValue(),Field.Store.YES,Field.Index.UN_TOKENIZED));
	  		 	doc.add(new Field(efv.getField().getIndexKey(),efv.getValue(),Field.Store.NO,Field.Index.TOKENIZED));
	  		 }
	  	 }
	  	 
	  	 Config config = ConfigurationService.getConfig();
	  	 if (config.getIndexMessageBody()) {
	  		 Object bodyPart = message.getContent();
	  		 logger.debug("writeBodyParts() begin");
	  		 writeBodyPartsToDocument(bodyPart,bodyPart,message.getContentLanguage(),message.getContentType(), message.getDisposition(), message.getFileName(),doc,tempFiles);
	  		 logger.debug("writeBodyParts() end");
	  	 }
	  	
		 String lang = doc.get("lang");
		 if (lang==null) {
	        	lang = config.getIndexLanguage();
	        	logger.debug("neglected to detect language during indexing. using default language. {defaultlanguage='"+lang+"'}");
	        	doc.add(new Field("lang",lang,Field.Store.YES,Field.Index.UN_TOKENIZED));  
	        } else {
	        	logger.debug("language detected during indexing {language='"+lang+"'}");
	      } 
	  }

	  protected void writeBodyPartsToDocument(Object bodyPart, Object bodyPartBackup, String[] languages, String mimeTypeA, String disposition, String filename, Document doc, TempFiles tempFiles)throws MessagingException,IOException
	  {
	    String mimetype = getNormalizedMimeType(mimeTypeA);
	    logger.debug("writeBodyPartsToDocument() top {mimetype = '"+mimetype+"',filename='"+filename+"',disposition='"+disposition+"'}");

	    if (bodyPart instanceof String) {
	  		String bodyPartStr = (String)bodyPart;
	  		byte[] bodyBytes = bodyPartStr.getBytes();
	  		logger.debug("body part is a string");
	  		extractAndIndex(new ByteArrayInputStream(bodyBytes),new ByteArrayInputStream(bodyBytes),languages,mimetype,disposition, filename,doc,tempFiles);
	  		logger.debug("body part string processed");
	    } else if (bodyPart instanceof InputStream) {
        	logger.debug("body part is an inputstream");
            extractAndIndex((InputStream)bodyPart,(InputStream)bodyPart, languages,mimetype,disposition, filename, doc, tempFiles);
            logger.debug("input stream body part processed");
        } else if (bodyPart instanceof Multipart) {
        	logger.debug("message contains a multiple parts.");
            Multipart mp = (Multipart)bodyPart;
            for (int j = 0; j < mp.getCount(); j++) {
            	logger.debug("writing multipart ["+j+"]");
            	try {
            		writeBodyPartsToDocument(mp.getBodyPart(j).getContent(),mp.getBodyPart(j).getContent(),languages,mp.getBodyPart(j).getContentType(),mp.getBodyPart(j).getDisposition(),mp.getBodyPart(j).getFileName(), doc,tempFiles);
            	} catch (Exception e) {
            		logger.debug("failed to write multipart ["+j+"]");
            	}
            	logger.debug("multipart ["+j+"] written");
            }
            logger.debug("multiple parts processed");
        } else if (bodyPart instanceof MimeMessage) {
        		logger.debug("body part is a mime message");
        		MimeMessage message = (MimeMessage)bodyPart;
        		try {
        			logger.debug("about to get content");
        			Object content = message.getContent();
        			logger.debug("got content");
	        		if (content!=message) {
	        			writeBodyPartsToDocument(content, content, languages, message.getContentType(), message.getDisposition(), message.getFileName(), doc, tempFiles);
	        		}
	        	} catch(Exception w) {
        			logger.debug("failed to write mime message body part to document");
        		}
        		logger.debug("mime message body part processed");	
        } else if ((bodyPart instanceof Part) && (((Part)bodyPart).isMimeType( "multipart/*" ))) {
        	Part p = (Part) bodyPart; 
        	Multipart mp = (Multipart)p.getContent();
        	writeBodyPartsToDocument(mp, mp, languages, mimeTypeA, disposition, filename, doc, tempFiles);
        } else
        	logger.debug("failed to identify message part. {class='"+bodyPart.getClass().getSimpleName()+"'}");
	    logger.debug("writeBodyPartsToDocument() bottom");
	  }
	  
	  protected void extractAndIndex( InputStream is, InputStream is2, String languages[], String mimetype, String disposition, String filename, Document doc,TempFiles tempFiles) {
		  logger.debug("extractAndIndex() start");
		  try {

			  if ((disposition!=null && Compare.equalsIgnoreCase(disposition, Part.ATTACHMENT) || filename!=null)) {
				  boolean indexAttachment = ConfigurationService.getConfig().getIndexAttachments();
				  if (indexAttachment) {
					  if (Compare.equalsIgnoreCase(mimetype, "application/octet-stream")) {
						  octetStream(is,doc,tempFiles,filename);
					  } else if (Compare.equalsIgnoreCase(mimetype, "application/zip") ||
							  	 Compare.equalsIgnoreCase(mimetype, "application/x-zip") ||
							  	 Compare.equalsIgnoreCase(mimetype, "application/x-zip-compressed") ||
							  	 Compare.equalsIgnoreCase(mimetype, "application/x-compress") ||
							  	 Compare.equalsIgnoreCase(mimetype, "application/x-compressed")) {
							extractAndIndexZipFile(is, doc, tempFiles);  	 
				  	  } else if (Compare.equalsIgnoreCase(mimetype, "application/x-tar")) {
				  		  	extractAndIndexTarGZipFile(is,doc,tempFiles);
			  		  } else if (Compare.equalsIgnoreCase(mimetype, "application/gzip") ||
			  				  	 Compare.equalsIgnoreCase(mimetype, "application/x-gzip") ||
			  				  	 Compare.equalsIgnoreCase(mimetype, "application/gzipped") ||
			  				  	 Compare.equalsIgnoreCase(mimetype, "application/gzip-compressed") ||
			  				  	 Compare.equalsIgnoreCase(mimetype, "application/x-compressed") ||
			  				  	 Compare.equalsIgnoreCase(mimetype, "application/x-compress") ||
			  				  	 Compare.equalsIgnoreCase(mimetype, "gzip/document")) {
			  			  	extractAndIndexGZipFile(is,doc,tempFiles,filename);
					  } else {
						  Reader textReader = Extractor.getText(is,mimetype,tempFiles);
						  if (textReader!=null)
							  doc.add(new Field("attachments", textReader));
					  }
				  }
				  doc.add(new Field("attachname",filename,Field.Store.NO,Field.Index.TOKENIZED));
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
		  logger.debug("extractAndIndex() end");
	  }
	  
	  protected void octetStream(InputStream is, Document doc, TempFiles tempFiles,String filename) {
		  int dot = filename.lastIndexOf('.');
          if (dot==-1) return;
          String extension = filename.substring(dot+1,filename.length());
          
          if (Compare.equalsIgnoreCase(extension, "tar")) {
			  extractAndIndexTarFile(is,doc,tempFiles);
          } else if (Compare.equalsIgnoreCase(extension, "gz")) {
          	extractAndIndexGZipFile(is,doc,tempFiles,filename);
	  	  } else if (Compare.equalsIgnoreCase(extension,"zip")) {
	  		extractAndIndexZipFile(is,doc,tempFiles);
		  } else {
			  try {
				  Reader textReader = Extractor.getText(is,extension,tempFiles);
				  if (textReader!=null)
					  doc.add(new Field("attachments", textReader));
			  } catch (Exception io) {
				  logger.error("failed to extract message: "+io.getMessage(),io);
		      }
		  }
	  }
	  
	  
	  
	  
	  protected void extractAndIndexGZipFile(InputStream is, Document doc, TempFiles tempFiles,String filename ) {
		  logger.debug("extractAndIndexGZipFile()");
		  try {
			  GZIPInputStream gis = new GZIPInputStream(is);
			  String extension = "";
			  
			  for (int i=0;i<=1;i++) {
			  	int dot = filename.lastIndexOf('.');
	            if (dot==-1) return;
	            extension = filename.substring(dot+1,filename.length());
	            filename  = filename.substring(0,dot);
			  }
			  if (Compare.equalsIgnoreCase(extension,"tar"))
				  extractAndIndexTarFile(gis,doc,tempFiles);
			  else {
				  Reader textReader = Extractor.getText(gis,extension,tempFiles);
				  if (textReader!=null)
					  doc.add(new Field("attachments", textReader));
			  }
		  } catch (Exception io) {
			  logger.error("failed to extract gzipped message: "+io.getMessage(),io);
	      }
	  }
	  protected void extractAndIndexTarGZipFile(InputStream is, Document doc, TempFiles tempFiles) {
		  logger.debug("extractAndIndexTarGZipFile()");
		  try {
			  extractAndIndexTarFile(new GZIPInputStream(is),doc, tempFiles );
		  } catch (IOException io) {
			  logger.error(io.getMessage(),io);
		  }
	  }
	  protected void extractAndIndexTarFile(InputStream is, Document doc, TempFiles tempFiles ) {
		  logger.debug("extractAndIndexTarFile()");
		  try {
			  TarInputStream gis = new TarInputStream(is);
			  TarEntry entry;
			  while((entry = gis.getNextEntry()) != null) {
		             String name = entry.getName();
		             int dot = name.lastIndexOf('.');
		             if (dot==-1) continue;
		             String extention = name.substring(dot+1,name.length());
		             try {
		                Reader textReader = Extractor.getText(gis,extention,tempFiles);
		                if (textReader!=null)
		                	doc.add(new Field("attachments", textReader));
		                doc.add(new Field("attachname",name,Field.Store.NO,Field.Index.TOKENIZED));
		       	     } catch (ExtractionException ee) {
		       	              logger.debug("failed to decode message part content (mimetype unsupported?) {extention='"+extention+"'}",ee);
		       	     }
	         	}
		  } catch (Exception io) {
			  logger.error(io.getMessage(),io);
		  }
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
		                doc.add(new Field("attachname",name,Field.Store.NO,Field.Index.TOKENIZED));
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
            		  lang = languages[0].trim().toLowerCase(Locale.ENGLISH);
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
	  // Enterprise version
	  
	  public void deleteMessage(EmailID emailID) throws MessageSearchException {
		  if (emailID == null)
	            throw new MessageSearchException("assertion failure: null emailID",logger);
		  logger.debug("delete message {'"+emailID+"'}");
		  Volume volume = emailID.getVolume();
	      File indexDir = new File(volume.getIndexPath());
		  if (!indexDir.exists())
			  throw new MessageSearchException("could not delete email from index. volume does not exist. {'"+emailID+"}",logger);
	      IndexReader indexReader = null;
	      try {
	    	  indexReader = IndexReader.open(indexDir);
	      } catch (IOException e ) {
	    	  throw new MessageSearchException("failed to open index to calculate total email count",e,logger);
	      }
	      try {
	    	  indexReader.deleteDocuments(new Term("uid",emailID.getUniqueID()));
	    	  indexReader.close();
	      } catch (Exception e) {
	    	  throw new MessageSearchException("failed to delete email from index.",e,logger);
	      }
	  }

	  public void indexMessage(EmailID emailID) throws MessageSearchException {
	      if (emailID==null)
	            throw new MessageSearchException("assertion failure: null emailId",logger);
	      indexQueue.addFirst(emailID);
	      logger.debug("add message to index queue {queueSize='"+indexQueue.size()+"'," + emailID + "}");
	  }

	  public void createIndexDir(Volume volume) throws MessageSearchException {

	      if (volume==null)
	            throw new MessageSearchException("assertion failure: null volume",logger);

	      if (volume.getIndexPath().startsWith("rmi://"))
	    		  return;
	    		  
	      File indexDir = new File(volume.getIndexPath());
          if (!indexDir.exists()) {
   			logger.info("index director2y does not exist. will proceed with creation {location='" + volume.getIndexPath() + "'}");
   			boolean success = indexDir.mkdir();
   			if (!success)
   					throw new MessageSearchException("failed to create index directory {location='" + volume.getIndexPath() + "'}",logger);
   			logger.info("index directory successfully created {location='" + volume.getIndexPath() + "'}");
   		  }

	  }
	  

	  
	  protected void finalize() throws Throwable {
		    logger.debug("messagindex class is shutting down");
		    try {
		        shutdown = true;
		        Thread.sleep(100);
		        closeAllVolumes();
		    } finally {
		        super.finalize();
		    }
		}

}
