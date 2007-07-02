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


import org.apache.log4j.Logger;
import java.util.*;
import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.exception.ExtractionException;
import com.stimulus.util.TempFiles;
import java.io.*;

public class Extractor
{
	protected static final Logger logger = Logger.getLogger(Extractor.class.getName());
	protected static final Map handlers;
	protected ArrayList fileDeleteList = new ArrayList();
	 static {
		handlers = new HashMap();
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
		handlers.put("doc",word);
		TextExtractor excel = new ExcelExtractor();
		handlers.put("application/excel", excel);
		handlers.put("xls", excel);
		TextExtractor ppt = new PowerpointExtractor();
		handlers.put("application/vnd.ms-powerpoint", ppt);
		handlers.put("application/mspowerpoint", ppt);
		handlers.put("ppt", ppt);
		TextExtractor rtf = new RTFExtractor();
		handlers.put("application/rtf", rtf);
		handlers.put("rtf", rtf);
	 }

	 public Extractor() {
	 }

	 public static Reader getText(InputStream is, String mimetype,TempFiles tempFiles) throws ExtractionException {
	     TextExtractor extractor;
	     extractor = (TextExtractor)handlers.get(mimetype.toLowerCase());
	     if(extractor == null) {
	     	//throw new ExtractionException("failed to extract text (mimetype not supported) {mimetype='"+mimetype+"'}",logger);
	      return null;
	     } else return extractor.getText(is, tempFiles);
	 }
	 
	
	
}