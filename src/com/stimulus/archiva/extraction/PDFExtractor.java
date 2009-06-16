

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


package com.stimulus.archiva.extraction;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import org.apache.commons.logging.*;
import org.apache.pdfbox.encryption.DocumentEncryption;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import com.stimulus.archiva.exception.*;
import java.io.*;
import com.stimulus.util.*;
import java.nio.charset.Charset;
import com.stimulus.archiva.index.*;

public class PDFExtractor implements TextExtractor, Serializable
{
	/**
	 *
	 */
	private static final long serialVersionUID = -7182261400513351604L;
	protected static final Log logger = LogFactory.getLog(Extractor.class.getName());

 public PDFExtractor() {
 }

 public Reader getText(InputStream is,Charset charset,IndexInfo indexInfo) throws ExtractionException  {
     logger.debug("extracting pdf file");
	 File file = null;
     PDDocument document = null;
     Writer output = null;
     try {
    	 PDFParser parser = new PDFParser(is);
	     parser.parse();
	     document = parser.getPDDocument();
	     if (document.isEncrypted()) {
	         DocumentEncryption decryptor = new DocumentEncryption(document);
	         if (logger.isDebugEnabled()) {
	             logger.debug("pdf document appears to be encrypted (will attempt decryption)");

	         }
	         decryptor.decryptDocument("");
	     }
	     file = File.createTempFile("extract_pdf", ".tmp");
	     indexInfo.addDeleteFile(file);
	     output = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
	     PDFTextStripper stripper = new PDFTextStripper();
	  	 stripper.writeText(document, output);
	  	/*logger.debug("PDF extraction completed");
	  	 BufferedReader reader;
	  	 try {
	  		 reader = new BufferedReader(new FileReader(file));
	  		String line = null;
	  		while( (line=reader.readLine()) != null) {
	  			logger.debug("PDF>"+line);
	  		}
	  		reader.close();
	  	 } catch(Exception e) {
	  		 logger.error("failed to open txt file",e);
	  	 }*/
     } catch (Throwable e) {
         throw new ExtractionException("failed to extract pdf (probable password protected document)",e,logger,ChainedException.Level.DEBUG);
     } finally {
    	 try {
	    	 if (document != null)
	 	        document.close();
		  	 if (output !=null)
		  		 output.close();
    	 } catch (IOException io) {}
     }
     try {
	 	logger.debug("returning extracted PDF data");
	 	Reader outReader = new FileReader(file);
	    indexInfo.addReader(outReader);
	    return outReader;
     } catch(Exception ex) {
        throw new ExtractionException("failed to extract text from powerpoint document",ex,logger,ChainedException.Level.DEBUG);
     }
 }

}


