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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimePart;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;
import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.domain.Email;
import com.stimulus.archiva.domain.fields.EmailField;
import com.stimulus.archiva.domain.fields.EmailFieldValue;
import com.stimulus.archiva.domain.fields.EmailFields;
import com.stimulus.archiva.exception.ExtractionException;
import com.stimulus.archiva.extraction.Extractor;
import com.stimulus.archiva.language.LanguageIdentifier;
import com.stimulus.util.CharUtil;
import com.stimulus.util.Compare;
import com.stimulus.util.TempFiles;
import com.stimulus.archiva.domain.Indexer;

public class DocumentIndex {

	  protected static LanguageIdentifier languageIdentifier = new LanguageIdentifier();
	  protected static final Logger logger = Logger.getLogger(VolumeIndex.class.getName());
	  protected Indexer indexer;
	  protected Charset utf8_charset = Charset.forName("UTF-8");
	  
	  public DocumentIndex(Indexer indexer) {
		  this.indexer = indexer;
	  }
	  protected void write(Email message, Document doc) throws MessagingException,IOException,ExtractionException
	  {
		  logger.debug("writeMessageToDocument() begin");
	      if (message.getEmailID()==null)
		  	     throw new MessagingException("failed to index message as unique ID is null");
	      
	  	 String uniqueID 	= message.getEmailID().getUniqueID();
	  	 String volID		= message.getEmailID().getVolume().getID();
	  	 
	  	 if (uniqueID==null || volID==null) {
	  		logger.error("failed to index message. uniqueid or volumeid is null.");
	  		return;
	  	 }
	  	 
	  	 doc.add(new Field("ver","2",Field.Store.YES,Field.Index.UN_TOKENIZED));    
	  	 if (uniqueID!=null) {
	  		 doc.add(new Field("uid",uniqueID,Field.Store.YES,Field.Index.UN_TOKENIZED)); 
	  	 }
	  	 if (volID!=null) {
	  		 doc.add(new Field("vol",volID,Field.Store.YES,Field.Index.UN_TOKENIZED));   
	  	 }
	  	EmailFields emailFields = Config.getConfig().getEmailFields();
	     for (EmailField field : emailFields.getAvailableFields().values()) {
	    	 
	    	 String value = "";
	    	 EmailFieldValue efv2 = message.getFields().get(field.getIndex());
	    	 if (efv2!=null && efv2.getValue()!=null) {
	    		 value = efv2.getValue();
	    	 }
	    	
	  		 if (Compare.equalsIgnoreCase(field.getName(), "body") ||
	  			 Compare.equalsIgnoreCase(field.getName(), "attachments"))	 
	  			 continue; // we handle this later

	  		 EmailField.SearchMethod searchMethod = field.getSearchMethod();
	  		 logger.debug("index {field='"+field.getIndex()+"',value='"+value+"'}");
	  		 if (searchMethod==EmailField.SearchMethod.STORED)
	  			 doc.add(new Field(field.getIndex(),value,Field.Store.YES,Field.Index.UN_TOKENIZED));
	  		 else if (searchMethod==EmailField.SearchMethod.TOKENIZED)
	  			 doc.add(new Field(field.getIndex(),value,Field.Store.NO,Field.Index.TOKENIZED));
	  		 else if (searchMethod==EmailField.SearchMethod.TOKENIZED_AND_STORED) {
	  			doc.add(new Field(field.getIndex()+"s",value,Field.Store.YES,Field.Index.UN_TOKENIZED));
	  		 	doc.add(new Field(field.getIndex(),value,Field.Store.NO,Field.Index.TOKENIZED));
	  		 }
	  	 }
	  	
	  	 if (indexer.getIndexMessageBody()) {
	  		 logger.debug("writeBodyParts() begin");
	  		 try {
	  			 writeBodyPartsToDocument(message,doc);
	  		 } catch (OutOfMemoryError ofme) {
	  			 logger.debug("failed to extract message for indexing:"+ofme.getMessage(),ofme);
	  		 } catch (Exception e) {
	  			 logger.debug("could not write a body part to email. most message fields should still be indexed.",e);
	  		 }
	  		 logger.debug("writeBodyParts() end");
	  	 }
	  	
		 String lang = doc.get("lang");
		 if (lang==null) {
	        	lang = indexer.getIndexLanguage();
	        	logger.debug("neglected to detect language during indexing. using default language. {defaultlanguage='"+lang+"'}");
	        	if (lang!=null) {
	        		doc.add(new Field("lang",lang,Field.Store.YES,Field.Index.UN_TOKENIZED));  
	        	}
	        } else {
	        	logger.debug("language detected during indexing {language='"+lang+"'}");
	      } 
		 logger.debug("writeMessageToDocument() end");
	  }

	
	  
	  protected void writeBodyPartsToDocument(Part p, Document doc) throws MessagingException, IOException {
		  	if (p.isMimeType("multipart/alternative")) {
				 logger.debug("body part is alternative");
			     Multipart mp = (Multipart)p.getContent();
			     Part chosen = null;
			     for (int i = 0; i < mp.getCount(); i++) {
			    	 Part bp = mp.getBodyPart(i);
			    	 if (bp.isMimeType("text/plain")) { // we give preference to plain text
			    		 logger.debug("body part text chosen");
			    		 chosen = bp;
			    		 break;
			    	 } else {
			    		 chosen = bp;
			    	 }
			     }
			     extractAndIndex(chosen,doc);
			 } else if (p.isMimeType("message/rfc822")) {
				 writeBodyPartsToDocument((Part)p.getContent(),doc);
	  		 } else if (p.isMimeType("multipart/*")) {
			     Multipart mp = (Multipart)p.getContent();
			     for (int i = 0; i < mp.getCount(); i++) {
			    	 writeBodyPartsToDocument(mp.getBodyPart(i),doc);
			     }
			 } else {
				 logger.debug("extracting text {contenttype='"+p.getContentType()+"'}");
				 extractAndIndex(p,doc);
			 }
	  }
	
	  protected void extractAndIndex( Part p, Document doc) throws MessagingException {
		  logger.debug("extractAndIndex() start");
		  String disposition = p.getDisposition();
		  String mimetype = getNormalizedMimeType(p.getContentType());
		  String filename = p.getFileName(); 
		  logger.debug("extractAndIndex {disposition='"+disposition+"',mimetype='"+mimetype+"',filename='"+filename+"'}");
		  Charset charset = null;
		  try {
			  charset = CharUtil.getCharsetFromPartContent((MimePart)p);
		  } catch (Exception e) {
			  charset = utf8_charset;
		  }
		  TempFiles tempFiles = Config.getFileSystem().getTempFiles();
		  try {

			  if ((disposition!=null && Compare.equalsIgnoreCase(disposition, Part.ATTACHMENT) || filename!=null)) {
				  boolean indexAttachment = indexer.getIndexAttachments();
				  if (indexAttachment) {
					  if (Compare.equalsIgnoreCase(mimetype, "application/octet-stream")) {
						  octetStream(p.getInputStream(),doc,tempFiles,filename,charset);
					  } else if (Compare.equalsIgnoreCase(mimetype, "application/zip") ||
							  	 Compare.equalsIgnoreCase(mimetype, "application/x-zip") ||
							  	 Compare.equalsIgnoreCase(mimetype, "application/x-zip-compressed") ||
							  	 Compare.equalsIgnoreCase(mimetype, "application/x-compress") ||
							  	 Compare.equalsIgnoreCase(mimetype, "application/x-compressed")) {
							extractAndIndexZipFile(p.getInputStream(), doc, tempFiles,charset);  	 
				  	  } else if (Compare.equalsIgnoreCase(mimetype, "application/x-tar")) {
				  		  	extractAndIndexTarGZipFile(p.getInputStream(),doc,tempFiles,charset);
			  		  } else if (Compare.equalsIgnoreCase(mimetype, "application/gzip") ||
			  				  	 Compare.equalsIgnoreCase(mimetype, "application/x-gzip") ||
			  				  	 Compare.equalsIgnoreCase(mimetype, "application/gzipped") ||
			  				  	 Compare.equalsIgnoreCase(mimetype, "application/gzip-compressed") ||
			  				  	 Compare.equalsIgnoreCase(mimetype, "application/x-compressed") ||
			  				  	 Compare.equalsIgnoreCase(mimetype, "application/x-compress") ||
			  				  	 Compare.equalsIgnoreCase(mimetype, "gzip/document")) {
			  			  	extractAndIndexGZipFile(p.getInputStream(),doc,tempFiles,filename,charset);
					  } else {
						  Reader textReader = Extractor.getText(p.getInputStream(),mimetype,tempFiles,charset);
						  if (textReader!=null) {
							  doc.add(new Field("attachments", textReader));
							 /* try { textReader.close(); } catch (Exception e) {
								  logger.debug("failed to close extraction stream()");
							  }*/
						  }
					  }
				  }
				  if (filename!=null)
					  doc.add(new Field("attachname",filename,Field.Store.NO,Field.Index.TOKENIZED));
			  } else {
				  Reader textReader = Extractor.getText(p.getInputStream(),mimetype,tempFiles,charset);
				  if (textReader!=null) {
					  doc.add(new Field("body", textReader));
		          	  Reader detectReader = Extractor.getText(p.getInputStream(),mimetype,tempFiles,charset);
		          	  String[] languages = ((MimePart)p).getContentLanguage();
		              addLanguage(languages, doc, detectReader);
		              //detectReader.close();
		              /*Reader test = Extractor.getText(getInputStreamFromPart((MimePart)p),mimetype,tempFiles);
		              BufferedReader b = new BufferedReader(test); 
		              String line = b.readLine();
		              while (line != null) {
		                System.out.println("(" + line + ")"); 
		                line = b.readLine(); 
		              }   */
		              
				  }
			  }
		  } catch (Exception ee) {
	          logger.debug("failed to decode message part content (mimetype unsupported?) {mimetype='"+mimetype+"'}",ee);
	          return;
		  } 
		  logger.debug("extractAndIndex() end");
	  }
	  
	
	  protected void octetStream(InputStream is, Document doc, TempFiles tempFiles,String filename, Charset charset) {
		  int dot = filename.lastIndexOf('.');
        if (dot==-1) return;
        String extension = filename.substring(dot+1,filename.length());
        
        if (Compare.equalsIgnoreCase(extension, "tar")) {
			  extractAndIndexTarFile(is,doc,tempFiles,charset);
        } else if (Compare.equalsIgnoreCase(extension, "gz")) {
        	extractAndIndexGZipFile(is,doc,tempFiles,filename,charset);
	  	  } else if (Compare.equalsIgnoreCase(extension,"zip")) {
	  		extractAndIndexZipFile(is,doc,tempFiles,charset);
		  } else {
			  try {
				  Reader textReader = Extractor.getText(is,extension,tempFiles,charset);
				  if (textReader!=null) {
					  doc.add(new Field("attachments", textReader));
					  /*try { textReader.close(); } catch (Exception e) {
						  logger.debug("failed to close extraction stream()");
					  }*/
				  }
			  } catch (Exception io) {
				  logger.error("failed to extract message: "+io.getMessage(),io);
		      }
		  }
	  }
	  
	  
	  protected void extractAndIndexGZipFile(InputStream is, Document doc, TempFiles tempFiles,String filename, Charset charset ) {
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
				  extractAndIndexTarFile(gis,doc,tempFiles,charset);
			  else {
				  Reader textReader = Extractor.getText(gis,extension,tempFiles,charset);
				  if (textReader!=null) {
					  doc.add(new Field("attachments", textReader));
				  }
				  /*try { textReader.close(); } catch (Exception e) {
					  logger.debug("failed to close extraction stream()");
				  }*/
			  }
		  } catch (Exception io) {
			  logger.error("failed to extract gzipped message: "+io.getMessage(),io);
	      }
	  }
	  protected void extractAndIndexTarGZipFile(InputStream is, Document doc, TempFiles tempFiles, Charset charset) {
		  logger.debug("extractAndIndexTarGZipFile()");
		  try {
			  extractAndIndexTarFile(new GZIPInputStream(is),doc, tempFiles, charset );
		  } catch (IOException io) {
			  logger.error(io.getMessage(),io);
		  }
	  }
	  protected void extractAndIndexTarFile(InputStream is, Document doc, TempFiles tempFiles, Charset charset ) {
		  logger.debug("extractAndIndexTarFile()");
		  try {
			  TarInputStream gis = new TarInputStream(is);
			  TarEntry entry;
			  while((entry = gis.getNextEntry()) != null) {
		             String name = entry.getName();
		             int dot = name.lastIndexOf('.');
		             if (dot==-1) continue;
		             String extention = name.substring(dot+1,name.length());
		             Reader textReader = Extractor.getText(gis,extention,tempFiles,charset);
	                 if (textReader!=null) {
	                	doc.add(new Field("attachments", textReader));
	                	/*try { textReader.close(); } catch (Exception e) {
							  logger.debug("failed to close extraction stream()");
						}*/
	                 }
	                 if (name!=null) {
	                	 doc.add(new Field("attachname",name,Field.Store.NO,Field.Index.TOKENIZED));
	                 }
	         	}
			    //gis.close();
		  } catch (Exception io) {
			  logger.error(io.getMessage(),io);
		  }
	  }
	  
	  protected void extractAndIndexZipFile(InputStream is, Document doc, TempFiles tempFiles,Charset charset) {
       	try {
       			ZipInputStream zis = new ZipInputStream(is);
       			ZipEntry entry;
		    	while((entry = zis.getNextEntry()) != null) {
		             String name = entry.getName();
		             int dot = name.lastIndexOf('.');
		             if (dot==-1) continue;
		             String extention = name.substring(dot+1,name.length());
	                 Reader textReader = Extractor.getText(zis,extention,tempFiles,charset);
	                 if (textReader!=null) {
	                	doc.add(new Field("attachments", textReader));
	                	try { textReader.close(); } catch (Exception e) {
							  logger.debug("failed to close extraction stream()");
						}
	                 }
	                 if (name!=null) {
	                	 doc.add(new Field("attachname",name,Field.Store.NO,Field.Index.TOKENIZED));
	                 }
	         	}
	         	//zis.close();
       	} catch (IOException io) {
       	    logger.error(io.getMessage(),io);
       	}
	  }
	  
	  protected void addLanguage(String[] languages, Document doc, Reader detectReader) {
		    String lang;
        	if (indexer.getIndexLanguageDetection() && doc.get("lang")==null) {
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
	  
	  
}
