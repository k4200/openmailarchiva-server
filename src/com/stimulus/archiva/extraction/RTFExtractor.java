
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
import javax.swing.text.rtf.RTFEditorKit;
import com.stimulus.archiva.index.*;
import org.apache.commons.logging.*;
import javax.swing.text.DefaultStyledDocument;
import com.stimulus.util.*;

import java.nio.charset.Charset;

public class RTFExtractor implements TextExtractor,Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4548973247294566348L;
	protected static final Log logger = LogFactory.getLog(Extractor.class.getName());

	public RTFExtractor() {}

	public Reader getText(InputStream is,  Charset charset,IndexInfo indexInfo) throws ExtractionException {
	    
	        Reader reader = null;
	        FileWriter writer = null;
	        File file = null;
	        try {
	            reader = new InputStreamReader(is);
	            file = File.createTempFile("extract_rtf", ".tmp");
	            indexInfo.addDeleteFile(file);
	            writer = new FileWriter(file);
	            DefaultStyledDocument doc = new DefaultStyledDocument();
	            new RTFEditorKit().read(reader, doc, 0);
	            writer.write(doc.getText(0, doc.getLength()));
	        } catch (Throwable ioe) {
	            throw new ExtractionException("failed to parse rtf document", ioe,logger);
	        } finally {
	            if (reader != null) {
	                try {
	                    reader.close();
	                } catch (IOException ioe) {}
	            }

	            if (writer != null) {
	                try {
	                    writer.close();
	                } catch (IOException ioe) {}
	            }
	        }
	        try {
		        Reader outReader = new FileReader(file);
		        indexInfo.addReader(outReader);
		        return outReader;
		    } catch(Exception ex) {
		        throw new ExtractionException("failed to extract text from powerpoint document",ex,logger,ChainedException.Level.DEBUG);
		    }
	        
	    }

}
