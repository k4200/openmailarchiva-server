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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.util.LittleEndian;

import com.stimulus.archiva.exception.ExtractionException;
import com.stimulus.util.Compare;
import com.stimulus.util.TempFiles;

public class PowerpointExtractor implements TextExtractor,POIFSReaderListener,Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6003013329110133862L;
	protected static Logger logger = Logger.getLogger(Extractor.class.getName());
	private OutputStream output = null;
	
	public PowerpointExtractor()
	{
	}

	public Reader getText(InputStream is,TempFiles tempFiles) throws ExtractionException
	{
	    File file = null;
	    try {
	        POIFSReader reader = new POIFSReader();
	        file = File.createTempFile("extract", ".tmp");
   	  	 	tempFiles.markForDeletion(file);
   	  	 	output = new FileOutputStream(file);
	        reader.registerListener(this);
	        reader.read(is);
	    } catch (Exception ex) {
	        throw new ExtractionException("failed to extract text from powerpoint document",ex,logger);
	    }  finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException ioe) {}
            }
        }
	    try {
	        return new FileReader(file);
	    } catch(Exception ex) {
	        throw new ExtractionException("failed to extract text from powerpoint document",ex,logger);
	    }
	}

	public void processPOIFSReaderEvent(POIFSReaderEvent event) {
	    try{
	    	if(!Compare.equalsIgnoreCase(event.getName(), "PowerPoint Document"))
	    		 return;
	        
	        DocumentInputStream input = event.getStream();

	        byte[] buffer = new byte[input.available()];
	        input.read(buffer, 0, input.available());
	        for(int i=0; i<buffer.length-20; i++) {
	            long type = LittleEndian.getUShort(buffer,i+2);
	            long size = LittleEndian.getUInt(buffer,i+4);
	            if(type==4008L) {
	                
	                output.write(buffer, i + 4 + 1, (int) size +3);
	                i = i + 4 + 1 + (int) size - 1;
	            }
	        }
	    } catch (Exception ex) {
	        logger.debug(ex.getMessage());
	    }
	}

}
