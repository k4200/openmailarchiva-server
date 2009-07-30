
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

import com.stimulus.archiva.exception.*;
import java.io.*;
import java.nio.charset.Charset;
import org.apache.commons.logging.*;
import com.stimulus.util.*;
import com.stimulus.archiva.index.*;
import net.htmlparser.jericho.*;
import net.htmlparser.jericho.TextExtractor;

public class HTMLExtractor implements com.stimulus.archiva.extraction.TextExtractor,Serializable
{
	
	private static final long serialVersionUID = 7138851180242634460L;
	protected static final Log logger = LogFactory.getLog(Extractor.class.getName());

	public HTMLExtractor()
	{
	}

	public Reader getText(InputStream is, Charset charset,IndexInfo indexInfo) throws ExtractionException
	{
		 Reader r = null;
		 try {
			 Source source=new Source(is);
			 r = new StringReader(source.getTextExtractor().toString());
			 return r;
		 } catch (IOException io) {
			 throw new ExtractionException("failed extract text from html:"+io.getMessage(),io,logger);
		 } finally {
			 if (r!=null)
				 indexInfo.addReader(r);
			 if (is!=null)
				 indexInfo.addSourceStream(is);
		 }
	}

}


