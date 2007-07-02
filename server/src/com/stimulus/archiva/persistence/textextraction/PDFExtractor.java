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

package com.stimulus.archiva.persistence.textextraction;
import java.io.File;
import java.io.FileReader;
import org.apache.log4j.Logger;
import org.pdfbox.encryption.DocumentEncryption;
import org.pdfbox.pdfparser.PDFParser;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;
import com.stimulus.archiva.exception.ExtractionException;
import java.io.*;
import com.stimulus.util.*;

public class PDFExtractor implements TextExtractor
{
    private static final String DEFAULT_ENCODING = "ISO-8859-1";
	protected static final Logger logger = Logger.getLogger(Extractor.class.getName());

 public PDFExtractor() {
 }

 public Reader getText(InputStream is,TempFiles tempFiles) throws ExtractionException  {
     File file = null;
     try {
	     PDFParser parser = new PDFParser(is);
	     parser.parse();
	     PDDocument document = parser.getPDDocument();
	     
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
	     output = new OutputStreamWriter(new FileOutputStream(file), DEFAULT_ENCODING);
	  	 PDFTextStripper stripper = new PDFTextStripper();
	  	 stripper.writeText(document, output);
	  	 output.close();
	  	 if(document != null)
	        document.close();
	  	
     } catch (Exception e) {
         throw new ExtractionException("failed to extract pdf",e,logger);
     }
     try {
	        return new FileReader(file);
     } catch(Exception ex) {
        throw new ExtractionException("failed to extract text from powerpoint document",ex,logger);
     }
 }
 
}


