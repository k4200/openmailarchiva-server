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

package com.stimulus.archiva.index;

import org.apache.tools.zip.*;
import javax.mail.internet.MimeUtility;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Enumeration;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimePart;
import javax.mail.MessagingException;
import net.freeutils.tnef.mime.TNEFMime;
import org.apache.commons.logging.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import com.ice.tar.*;
import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.domain.Email;
import com.stimulus.archiva.domain.fields.EmailField;
import com.stimulus.archiva.domain.fields.EmailFieldValue;
import com.stimulus.archiva.domain.fields.EmailFields;
import com.stimulus.archiva.exception.*;
import com.stimulus.archiva.extraction.Extractor;
import com.stimulus.archiva.language.LanguageIdentifier;
import com.stimulus.archiva.monitor.Event;
import com.stimulus.util.*;
import com.stimulus.archiva.domain.Indexer;
import java.util.zip.GZIPInputStream;

public class DocumentIndex {

	  protected static LanguageIdentifier languageIdentifier;
	  protected static final Log logger = LogFactory.getLog(VolumeIndex.class.getName());
	  protected Indexer indexer;
	  protected Charset utf8_charset = Charset.forName("UTF-8");

	  public DocumentIndex(Indexer indexer) {
		  this.indexer = indexer;
	  }


	  public void indexEmailField(Document doc, EmailField field, String value) {
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

	  protected void write(Email message, Document doc, IndexInfo indexInfo) throws MessagingException,IOException,ExtractionException
	  {
		 try {
			  logger.debug("writeMessageToDocument() begin");
		      if (message.getEmailID()==null)
			  	     throw new MessagingException("failed to index message as unique ID is null");

		      /*
		      try { message.getEmailID().getVolume().load(); } catch (Exception e) {
		    	  e.printStackTrace();
		      }*/

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

		    	if ( Compare.equalsIgnoreCase(field.getName(), "body") ||
		  			 Compare.equalsIgnoreCase(field.getName(), "attachments") ||
		  			 Compare.equalsIgnoreCase(field.getName(), "attachname"))
		  			 continue; // we handle this later

		    	indexEmailField(doc,field,value);

		  	 }
		  	 if (indexer.getIndexMessageBody()) {
		  		 logger.debug("writeBodyParts() begin");
		  		 try {
		  			 writeBodyPartsToDocument(message,doc,indexInfo);
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
		  } catch (Throwable e) {
			  logger.error("hard error occurred during Index write:"+e.getMessage(),e);
			  throw new MessagingException("hard error occurred during Index write:"+e.getMessage());
		  }
	  }



	  protected void writeBodyPartsToDocument(Part p, Document doc, IndexInfo indexInfo) throws MessagingException, IOException {
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
			     extractAndIndex(chosen,doc,indexInfo);
			 } else if (p.isMimeType("message/rfc822")) {
				 writeBodyPartsToDocument((Part)p.getContent(),doc,indexInfo);
	  		 } else if (p.isMimeType("multipart/*")) {
			     Multipart mp = (Multipart)p.getContent();
			     for (int i = 0; i < mp.getCount(); i++) {
			    	 writeBodyPartsToDocument(mp.getBodyPart(i),doc,indexInfo);
			     }
	  		} else if (p.isMimeType("application/ms-tnef")) { // winmail.dat
	        	Part tnefpart = TNEFMime.convert(null, p, false);
	        	if (tnefpart!=null) {
	        		writeBodyPartsToDocument(tnefpart,doc,indexInfo);
	        	}
	        } else {
				 logger.debug("extracting text {contenttype='"+p.getContentType()+"'}");
				 extractAndIndex(p,doc,indexInfo);
			}
	  }

	  protected void setCharset(Part p, IndexInfo indexInfo) {
		  Charset charset = null;
		  try {
			  charset = CharUtil.getCharsetFromPartContent((MimePart)p);
		  } catch (Exception me) {}
		  try {
			  if (charset==null) {
				  charset = indexInfo.getCharset();
			  } else {
				  indexInfo.setCharset(charset);
			  }
		  } catch (Exception e) {
			  charset = indexInfo.getCharset();
		  }
	  }


	  protected void extractAndIndex( Part p, Document doc,IndexInfo indexInfo) throws MessagingException {
		  logger.debug("extractAndIndex() start");
		  String disposition = p.getDisposition();
		  String mimetype = getNormalizedMimeType(p.getContentType());
		  setCharset(p,indexInfo);
		  Charset charset = indexInfo.getCharset();
		  String filename = p.getFileName();
		  if (filename!=null) {
			  try {
				  filename = MimeUtility.decodeText(filename);
			  } catch (Exception e) {
				  logger.debug("cannot decode filename:"+e.getMessage());
			  }
		  }
		  logger.debug("extractAndIndex {disposition='"+disposition+"',mimetype='"+mimetype+"',filename='"+filename+"'}");
		  InputStream is = null;
		  try {
			  is =  p.getInputStream();
			  if ((disposition!=null && Compare.equalsIgnoreCase(disposition, Part.ATTACHMENT) || filename!=null)) {
				  boolean indexAttachment = indexer.getIndexAttachments();
				  if (indexAttachment) {
					  if (Compare.equalsIgnoreCase(mimetype, "application/octet-stream")) {
						  octetStream(is,doc,filename,charset,indexInfo);
					  } else if (Compare.equalsIgnoreCase(mimetype, "application/zip") ||
							  	 Compare.equalsIgnoreCase(mimetype, "application/x-zip") ||
							  	 Compare.equalsIgnoreCase(mimetype, "application/x-zip-compressed") ||
							  	 Compare.equalsIgnoreCase(mimetype, "application/x-compress") ||
							  	 Compare.equalsIgnoreCase(mimetype, "application/x-compressed")) {
							extractAndIndexZipFile(is, doc,charset,indexInfo);
				  	  } else if (Compare.equalsIgnoreCase(mimetype, "application/x-tar")) {
				  		  	extractAndIndexTarGZipFile(is,doc,charset,indexInfo);
			  		  } else if (Compare.equalsIgnoreCase(mimetype, "application/gzip") ||
			  				  	 Compare.equalsIgnoreCase(mimetype, "application/x-gzip") ||
			  				  	 Compare.equalsIgnoreCase(mimetype, "application/gzipped") ||
			  				  	 Compare.equalsIgnoreCase(mimetype, "application/gzip-compressed") ||
			  				  	 Compare.equalsIgnoreCase(mimetype, "application/x-compressed") ||
			  				  	 Compare.equalsIgnoreCase(mimetype, "application/x-compress") ||
			  				  	 Compare.equalsIgnoreCase(mimetype, "gzip/document")) {
			  			  	extractAndIndexGZipFile(is,doc,filename,charset,indexInfo);
					  } else {
						  Reader textReader = Extractor.getText(is,mimetype,charset,indexInfo);
						  if (textReader!=null) {
							  doc.add(new Field("attachments", textReader));
							 // try { textReader.close(); } catch (Exception e) {
								//  logger.debug("failed to close extraction stream()");
							  //}
						  }
					  }
				  }
			  } else {
				  Reader textReader = Extractor.getText(is,mimetype,charset,indexInfo);
				  if (textReader!=null) {
					  doc.add(new Field("body", textReader));
		          	  Reader detectReader = Extractor.getText(is,mimetype,charset,indexInfo);
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
			  if (filename!=null) {
				  EmailField attachname = Config.getConfig().getEmailFields().get("attachname");
				  if (attachname!=null) {
					  indexEmailField(doc,attachname,filename);
				  }
			  }
		  } catch (Throwable ee) {
			  logger.debug("failed to decode message part content (mimetype unsupported?) {mimetype='"+mimetype+"'}",ee);
	          return;
		  }  finally {
			  	indexInfo.addSourceStream(is);
		  }
		  logger.debug("extractAndIndex() end");
	  }


	  protected void octetStream(InputStream is, Document doc,String filename, Charset charset, IndexInfo indexInfo) throws ArchivaException {
		  int dot = filename.lastIndexOf('.');
        if (dot==-1) return;
        String extension = filename.substring(dot+1,filename.length());
        if (Compare.equalsIgnoreCase(extension, "tar")) {
			  extractAndIndexTarFile(is,doc,charset,indexInfo);
        } else if (Compare.equalsIgnoreCase(extension, "gz")) {
        	extractAndIndexGZipFile(is,doc,filename,charset,indexInfo);
	  	  } else if (Compare.equalsIgnoreCase(extension,"zip")) {
	  		extractAndIndexZipFile(is,doc,charset,indexInfo);
		  } else {
			  try {
				  Reader textReader = Extractor.getText(is,extension,charset,indexInfo);
				  if (textReader!=null) {
					  doc.add(new Field("attachments", textReader));
					 /* try { textReader.close(); } catch (Exception e) {
						  logger.debug("failed to close extraction stream()");
					  }*/
				  }
			  } catch (Throwable io) {
				  throw new ArchivaException("failed to extract message content: "+io.getMessage(),io,logger,ChainedException.Level.DEBUG);
		      }
		  }
	  }


	  protected void extractAndIndexGZipFile(InputStream is, Document doc,String filename, Charset charset, IndexInfo indexInfo ) throws ArchivaException {
		  logger.debug("extractAndIndexGZipFile()");
		  GZIPInputStream gis = null;
		  try {
			  gis = new GZIPInputStream(is);
			  String extension = "";
			  for (int i=0;i<=1;i++) {
			  	int dot = filename.lastIndexOf('.');
	            if (dot==-1) return;
	            extension = filename.substring(dot+1,filename.length());
	            filename  = filename.substring(0,dot);
			  }
			  if (Compare.equalsIgnoreCase(extension,"tar"))
				  extractAndIndexTarFile(gis,doc,charset,indexInfo);
			  else {
				  Reader textReader = Extractor.getText(gis,extension,charset,indexInfo);
				  if (textReader!=null) {
					  doc.add(new Field("attachments", textReader));
				  }
				  /*try { textReader.close(); } catch (Exception e) {
					  logger.debug("failed to close extraction stream()");
				  }*/
			  }
		  } catch (Throwable io) {
			  throw new ArchivaException("failed to extract gzipped file contents: "+io.getMessage(),io,logger);
	      } finally {
	    	  indexInfo.addSourceStream(gis);
	      }
	  }

	  protected void extractAndIndexTarGZipFile(InputStream is, Document doc, Charset charset, IndexInfo indexInfo) throws ArchivaException {
		  logger.debug("extractAndIndexTarGZipFile()");
		  InputStream gis = null;
		  try {
			  gis = new GZIPInputStream(is);
			  extractAndIndexTarFile(new GZIPInputStream(is),doc,charset,indexInfo);
		  } catch (Throwable io) {
			  throw new ArchivaException("failed to extract tar gzip file contents: "+io.getMessage(),io,logger);
		  } finally {
			  indexInfo.addSourceStream(gis);
		  }
	  }

	  protected void extractAndIndexTarFile(InputStream is, Document doc, Charset charset,IndexInfo indexInfo) throws ArchivaException {
		  logger.debug("extractAndIndexTarFile()");
		  TarInputStream tis = null;
		  TarEntry entry;
		  try {
			  tis = new TarInputStream(is);
			  while((entry = tis.getNextEntry()) != null) {
		             String name = entry.getName();
		             int dot = name.lastIndexOf('.');
		             if (dot==-1) continue;
		             String extention = name.substring(dot+1,name.length());
		             File file = File.createTempFile(name,extention);
		             indexInfo.addDeleteFile(file);
		             IOUtil.copy(tis, new FileOutputStream(file));
		             Reader textReader = Extractor.getText(new FileInputStream(file),extention,charset,indexInfo);
	                 if (textReader!=null) {
	                	doc.add(new Field("attachments", textReader));
	                 }
	                 if (name!=null) {
	                	 EmailField attachname = Config.getConfig().getEmailFields().get("name");
		   				  if (attachname!=null) {
		   					  indexEmailField(doc,attachname,name);
		   				  }
	                 }
	         	}
		  } catch (Throwable io) {
			  throw new ArchivaException("failed to extract tar file  contents: "+io.getMessage(),io,logger);
		  } finally {
			  indexInfo.addSourceStream(tis);
		  }
	  }

	  protected void extractAndIndexZipFile(InputStream is, Document doc, Charset charset,IndexInfo indexInfo)  throws ArchivaException {
		  ZipFile zipFile = null;
		  try {
       		logger.debug("extractAndIndexZipFile()");
       		File file = File.createTempFile("extract_zip",".tmp");
       		indexInfo.addDeleteFile(file);
       		IOUtil.copy(is, new FileOutputStream(file));
       		String charname = Config.getConfig().getIndex().getIndexZipFileNameCharSet();
       		zipFile = new ZipFile(file,charname);
       		Enumeration<ZipEntry> zips = (Enumeration<ZipEntry>)zipFile.getEntries();
       		while (zips.hasMoreElements()) {
       			  	 ZipEntry entry = zips.nextElement();
       			  	 String name = entry.getName();
		             int dot = name.lastIndexOf('.');
		             if (dot==-1) continue;
		             String extention = name.substring(dot+1,name.length());
		             InputStream zis = zipFile.getInputStream(entry);
		             indexInfo.addSourceStream(zis);
		             Reader textReader = Extractor.getText(zis,extention,charset,indexInfo);
		             if (textReader!=null) {
		                	doc.add(new Field("attachments", textReader));
		             }
	                 if (name!=null) {
	                	 EmailField attachname = Config.getConfig().getEmailFields().get("name");
		   				  if (attachname!=null) {
		   					  indexEmailField(doc,attachname,name);
		   				  }
	                 }
       		 }
       	} catch (Throwable io) {
       		throw new ArchivaException("failed to extract zip file contents: "+io.getMessage(),io,logger);
       	} finally {
       		indexInfo.addZipFile(zipFile);
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

          		  if (languageIdentifier==null)
          		   languageIdentifier= new LanguageIdentifier();

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
