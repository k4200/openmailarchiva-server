
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


import org.apache.log4j.Logger;
import java.util.*;
import com.stimulus.archiva.exception.ExtractionException;
import com.stimulus.util.TempFiles;
import java.io.*;

public class Extractor
{
	protected static final Logger logger = Logger.getLogger(Extractor.class.getName());
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