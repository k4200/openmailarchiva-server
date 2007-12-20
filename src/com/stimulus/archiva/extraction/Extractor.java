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

package com.stimulus.archiva.extraction;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import com.stimulus.archiva.exception.ExtractionException;
import com.stimulus.util.TempFiles;

public class Extractor implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4914643971232234799L;
	protected static Logger logger = Logger.getLogger(Extractor.class.getName());
	protected static final Map<String,TextExtractor> handlers;
	protected ArrayList<String> fileDeleteList = new ArrayList<String>();
	 static {
		handlers = new HashMap<String,TextExtractor>();
		TextExtractor plain = new PlainTextExtractor();
		handlers.put("text/plain", plain);
		handlers.put("txt", plain);
		TextExtractor html = new HTMLExtractor();
		handlers.put("text/html", html);
		handlers.put("html", html);
		TextExtractor pdf = new PDFExtractor();
		handlers.put("application/pdf", pdf);
		handlers.put("pdf", pdf);
		TextExtractor word = new WordExtractor();
		handlers.put("application/msword",word);
		handlers.put("application/vnd.ms-word", word);
		handlers.put("application/vnd.msword", word);
		handlers.put("doc",word);
		TextExtractor excel = new ExcelExtractor();
		handlers.put("application/excel", excel);
		handlers.put("application/msexcel", excel);
		handlers.put("application/vnd.ms-excel", excel);
		handlers.put("xls", excel);
		TextExtractor ppt = new PowerpointExtractor();
		handlers.put("application/vnd.ms-powerpoint", ppt);
		handlers.put("application/mspowerpoint", ppt);
		handlers.put("application/powerpoint", ppt);
		handlers.put("ppt", ppt);
		TextExtractor rtf = new RTFExtractor();
		handlers.put("application/rtf", rtf);
		handlers.put("rtf", rtf);
		
		
		
		
		
	 }

	 public Extractor() {
	 }

	 public static Reader getText(InputStream is, String mimetype,TempFiles tempFiles) throws ExtractionException {
	     TextExtractor extractor;
	     extractor = (TextExtractor)handlers.get(mimetype.toLowerCase(Locale.ENGLISH));
	     if(extractor == null) {
	     	//throw new ExtractionException("failed to extract text (mimetype not supported) {mimetype='"+mimetype+"'}",logger);
	      return null;
	     } else return extractor.getText(is, tempFiles);
	 }
	 
	 // helper
	 
	 public static String writeTemp(InputStream is,TempFiles tempFiles) throws IOException {
	     	File file = File.createTempFile("extract", ".tmp");
	     	tempFiles.markForDeletion(file);
			logger.debug("writing temporary file for text extraction {filename='"+file.getPath()+"'}");
			OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
			BufferedInputStream bis = new BufferedInputStream(is);
			int c;
			while ((c = bis.read()) != -1)
			    os.write(c);
			os.close();
			return file.getPath();
	 }
	
	
}