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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import org.apache.log4j.Logger;

import com.stimulus.archiva.exception.ExtractionException;
import com.stimulus.util.TempFiles;

public class ExcelExtractor implements TextExtractor,Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4500591813545392474L;
	protected static Logger logger = Logger.getLogger(Extractor.class.getName());

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
