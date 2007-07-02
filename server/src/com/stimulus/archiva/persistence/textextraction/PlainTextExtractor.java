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
import com.stimulus.util.*;

public class PlainTextExtractor implements TextExtractor
{
	protected static final Logger logger = Logger.getLogger(Extractor.class.getName());

	public PlainTextExtractor()
	{
	}

	public Reader getText(InputStream is, TempFiles tempFiles) throws ExtractionException
	{
	   return (Reader)(new InputStreamReader(is));
	}

}
