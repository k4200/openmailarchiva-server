
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

import org.apache.commons.logging.*;
import java.util.*;
import java.nio.charset.Charset;
import com.stimulus.util.TempFiles;
import java.io.*;
import com.stimulus.archiva.exception.*;
import com.stimulus.archiva.index.*;

public class Extractor implements Serializable
{
	private static final long serialVersionUID = 4914643971232234799L;
	protected static final Log logger = LogFactory.getLog(Extractor.class.getName());
	protected static final Map<String,TextExtractor> handlers;
	protected ArrayList<String> fileDeleteList = new ArrayList<String>();
	static {
		handlers = new HashMap<String,TextExtractor>();
		
		// text
		
		TextExtractor plain = new PlainTextExtractor();
		handlers.put("text/plain", plain);
		handlers.put("txt", plain);
		
		// html
		
		TextExtractor html = new HTMLExtractor();
		handlers.put("text/html", html);
		handlers.put("html", html);
		
		// pdf
		
		TextExtractor pdf = new PDFExtractor();
		handlers.put("application/pdf", pdf);
		handlers.put("pdf", pdf);
		
		// rtf
		
		TextExtractor rtf = new RTFExtractor();
		handlers.put("application/rtf", rtf);
		handlers.put("rtf", rtf);
		
		TextExtractor poi = new POIExtractor();

		// word
		handlers.put("application/msword",poi);
		handlers.put("application/vnd.ms-word", poi);
		handlers.put("application/vnd.msword", poi);
		handlers.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document",poi);
		handlers.put("application/vnd.openxmlformats-officedocument.wordprocessingml.template",poi);
		handlers.put("doc",poi);
		handlers.put("docx",poi);
		handlers.put("dotx",poi);
		
		// excel
		
		handlers.put("application/excel", poi);
		handlers.put("application/msexcel", poi);
		handlers.put("application/vnd.ms-excel", poi);
		handlers.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", poi);
		handlers.put("application/vnd.openxmlformats-officedocument.spreadsheetml.template",poi);
		handlers.put("xls", poi);
		handlers.put("xlsx",poi);
		
		// powerpoint
		
		handlers.put("application/vnd.ms-powerpoint", poi);
		handlers.put("application/mspowerpoint", poi);
		handlers.put("application/powerpoint", poi);
		handlers.put("ppt", poi);
		handlers.put("pptx",poi);
		handlers.put("potx",poi);
		handlers.put("sldx",poi);
		handlers.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", poi);
		handlers.put("application/vnd.openxmlformats-officedocument.presentationml.template", poi);
		handlers.put("application/vnd.openxmlformats-officedocument.presentationml.slidshow", poi);
		
		// open office
		
		handlers.put("application/vnd.openxmlformats-officedocument.presentationml",poi);
		handlers.put("application/vnd.openxmlformats-officedocument.presentationml.slide",poi);
		handlers.put("application/vnd.oasis.opendocument.text",poi);
		handlers.put("application/vnd.oasis.opendocument.spreadsheet",poi);
		handlers.put("application/vnd.oasis.opendocument.presentation",poi);
		handlers.put("odt",poi);
		handlers.put("ods",poi);
		handlers.put("odp",poi);
		
		// visio
		
		handlers.put("vsd",poi);
		handlers.put("application/visio",poi);
		handlers.put("application/x-visio",poi);
		handlers.put("application/vsd",poi);
		handlers.put("application/x-vsd",poi);
	 }

	 public Extractor() {
	 }

	 public static Reader getText(InputStream is, String mimetype,Charset fromCharset, IndexInfo indexInfo) throws ExtractionException {
	     TextExtractor extractor;
	     extractor = handlers.get(mimetype.toLowerCase(Locale.ENGLISH));
	     if(extractor == null) {
	    	 logger.debug("failed to extract text (mimetype not supported) {mimetype='"+mimetype+"'}");
	     	//throw new ExtractionException("failed to extract text (mimetype not supported) {mimetype='"+mimetype+"'}",logger);
	      return null;
	     } else {
	    	 try {
	    		 return extractor.getText(is, fromCharset,indexInfo);
	    	 } catch (ExtractionException ee) {
	    		 throw ee;
	    	 }
	     }
	 }
	 
	
}
