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


import org.apache.log4j.Logger;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
import org.apache.poi.util.LittleEndian;
import com.stimulus.util.*;

public class PowerpointExtractor implements TextExtractor,POIFSReaderListener {

	protected static final Logger logger = Logger.getLogger(Extractor.class.getName());
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
	        if(!event.getName().equalsIgnoreCase("PowerPoint Document"))
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
	        logger.error(ex.getMessage());
	    }
	}

}
