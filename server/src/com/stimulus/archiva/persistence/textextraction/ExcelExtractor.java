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
import jxl.*;
import com.stimulus.util.*;

public class ExcelExtractor implements TextExtractor
{
	protected static final Logger logger = Logger.getLogger(Extractor.class.getName());

	public ExcelExtractor()
	{
	}

	public Reader getText(InputStream is, TempFiles tempFiles) throws ExtractionException
	{
	    File file = null;
	    PrintWriter out = null;
	    try {
	      file = File.createTempFile("extract", ".tmp");
		  tempFiles.markForDeletion(file);
		  out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
	      Workbook workbook = Workbook.getWorkbook(is);
	      Sheet[] sheets = workbook.getSheets();
	      for (int i = 0; i < sheets.length; i++) {
	        Sheet sheet = sheets[i];
	        int nbCol = sheet.getColumns();
	        for (int j = 0; j < nbCol; j++) {
	          Cell[] cells = sheet.getColumn(j);
	          for (int k = 0; k < cells.length; k++) {
	              out.print(cells[k].getContents() + " ");
	          }
	        }
	      }
	    }
	    catch (Exception e) {
	        throw new ExtractionException("could not extract Excel document",e,logger);
	    } finally {
            if (out != null) {
               out.close(); 
            }
	    }
	    try {
	        return new FileReader(file);
	    } catch(Exception ex) {
	        throw new ExtractionException("failed to extract text from powerpoint document",ex,logger);
	    }
	}

}
