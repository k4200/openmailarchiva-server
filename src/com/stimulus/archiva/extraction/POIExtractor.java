package com.stimulus.archiva.extraction;


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


import com.stimulus.archiva.exception.*;
import java.io.*;
import java.nio.charset.Charset;
import com.stimulus.util.*;
import com.stimulus.archiva.index.*;
import org.apache.commons.logging.*;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.*;

public class POIExtractor implements TextExtractor, Serializable
{
	private static final long serialVersionUID = 2150155150657302354L;
	protected static final Log logger = LogFactory.getLog(Extractor.class.getName());
	
	 public Reader getText(InputStream is, Charset charset, IndexInfo indexInfo) throws ExtractionException
	 {
		 try {
			 POITextExtractor poiExtractor = ExtractorFactory.createExtractor(is);
			 String text = poiExtractor.getText();
			 poiExtractor = null;
			 Reader outReader = new StringReader(text);
			 indexInfo.addReader(outReader);
			 return outReader;
		 } catch (Throwable e) {
			 throw new ExtractionException("failed to extract text from document:"+e.getMessage(),e,logger);
		 }
	 }
}
