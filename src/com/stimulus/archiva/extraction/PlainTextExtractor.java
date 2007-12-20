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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;

import org.apache.log4j.Logger;

import com.stimulus.archiva.exception.ExtractionException;
import com.stimulus.util.TempFiles;

public class PlainTextExtractor implements TextExtractor, Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3133399038029781732L;
	protected static Logger logger = Logger.getLogger(Extractor.class);

	public PlainTextExtractor()
	{
	}

	public Reader getText(InputStream is, TempFiles tempFiles) throws ExtractionException
	{
	   return (Reader)(new InputStreamReader(is));
	}

}
