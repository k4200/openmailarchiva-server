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
import com.stimulus.archiva.exception.ExtractionException;
import java.io.*;

import javax.swing.text.rtf.RTFEditorKit;
import org.apache.log4j.Logger;
import javax.swing.text.DefaultStyledDocument;
import com.stimulus.util.*;

public class RTFExtractor implements TextExtractor
{
	protected static final Logger logger = Logger.getLogger(Extractor.class.getName());

	public RTFExtractor() {}

	public Reader getText(InputStream is, TempFiles tempFiles) throws ExtractionException {
	    
	        Reader reader = null;
	        FileWriter writer = null;
	        File file = null;
	        try {
	            reader = new InputStreamReader(is);
	            file = File.createTempFile("extract", ".tmp");
	            tempFiles.markForDeletion(file);
	            writer = new FileWriter(file);
	            DefaultStyledDocument doc = new DefaultStyledDocument();
	            new RTFEditorKit().read(reader, doc, 0);
	            writer.write(doc.getText(0, doc.getLength()));
	        } catch (Exception ioe) {
	            throw new ExtractionException("failed to parse rtf document", ioe,logger);
	        }
	        finally {
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
		        return new FileReader(file);
		    } catch(Exception ex) {
		        throw new ExtractionException("failed to extract text from powerpoint document",ex,logger);
		    }
	        
	    }

}
