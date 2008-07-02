
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
import com.stimulus.archiva.exception.ExtractionException;
import java.io.*;
import java.nio.charset.Charset;
import com.stimulus.util.*;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.textmining.extraction.word.*;

public class WordExtractor implements TextExtractor, Serializable
{
	private static final long serialVersionUID = 2150155150657302354L;
	protected static final Logger logger = Logger.getLogger(Extractor.class.getName());
	
	 public Reader getText(InputStream is, TempFiles tempFiles, Charset charset) throws ExtractionException
	 {
		try {
			  File file = File.createTempFile("extract", ".tmp");
              tempFiles.markForDeletion(file);
			  Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			  WordTextExtractorFactory wtef = new WordTextExtractorFactory();
			  wtef.textExtractor(is).getText(out);
			  out.close();
			  return new InputStreamReader(new FileInputStream(file));
	 	} catch (Exception io) {
	 		throw new ExtractionException("failed to extract text from word document:"+io.getMessage(),io,logger,Level.DEBUG);
	 	}
	 }

}
