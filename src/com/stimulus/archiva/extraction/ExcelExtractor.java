
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
import org.apache.log4j.*;
import com.stimulus.util.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class ExcelExtractor implements TextExtractor,Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4500591813545392474L;
	protected static final Logger logger = Logger.getLogger(Extractor.class.getName());

	public ExcelExtractor(){
	}

	public Reader getText(InputStream is, TempFiles tempFiles, Charset charset) throws ExtractionException
	{
		try {
			POIFSFileSystem fs;
	        HSSFWorkbook workbook;
		    fs = new POIFSFileSystem(is);
		    workbook = new HSSFWorkbook (fs);
		    StringBuilder builder = new StringBuilder();
	        for (int numSheets = 0; numSheets < workbook.getNumberOfSheets(); numSheets++) {
	        	HSSFSheet sheet = workbook.getSheetAt(numSheets);
	        	Iterator rows = sheet.rowIterator();
	        	while( rows.hasNext() ) {          
	        		HSSFRow row = (HSSFRow) rows.next();
	        		Iterator cells = row.cellIterator();
		            while( cells.hasNext() ) 
		            {
		                HSSFCell cell = (HSSFCell) cells.next();
		                processCell (cell, builder);
		            }
	        	}
	        }
	        return new StringReader(builder.toString());
		} catch (Exception ee) {
			throw new ExtractionException("failed to extract excel document",logger,Level.DEBUG);
		}
        
	}
	
	
    private void processCell (HSSFCell cell, StringBuilder builder) 
    {
        switch ( cell.getCellType() ) 
        {
            case HSSFCell.CELL_TYPE_STRING:
                builder.append (cell.getStringCellValue());
                builder.append (" ");
                break;
            default:
                break;
        }
    }

}
