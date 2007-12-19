
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


package com.stimulus.archiva.extraction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;

import org.apache.log4j.Logger;
import org.pdfbox.encryption.DocumentEncryption;
import org.pdfbox.pdfparser.PDFParser;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;

import com.stimulus.archiva.exception.ExtractionException;
import com.stimulus.util.TempFiles;

public class PDFExtractor implements TextExtractor, Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7182261400513351604L;
	protected static Logger logger = Logger.getLogger(Extractor.class.getName());

 public PDFExtractor() {
 }

 public Reader getText(InputStream is,TempFiles tempFiles) throws ExtractionException  {
     File file = null;
     PDDocument document = null;
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
	     file = File.createTempFile("extract", ".tmp");
	  	 tempFiles.markForDeletion(file);
	     Writer output = null;
	     output = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
	  	 PDFTextStripper stripper = new PDFTextStripper();
	  	 stripper.writeText(document, output);
	  	 output.close();
	  	
	  	
     } catch (Exception e) {
         throw new ExtractionException("failed to extract pdf (probable password protected document)",e,logger);
     } finally {
    	 try {
	    	 if(document != null)
	 	        document.close();
    	 } catch (IOException io) {}
     }
     try {
	        return new FileReader(file);
     } catch(Exception ex) {
        throw new ExtractionException("failed to extract text from powerpoint document",ex,logger);
     }
 }
 
}


