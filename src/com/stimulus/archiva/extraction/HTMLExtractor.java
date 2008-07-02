
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
import java.nio.charset.Charset;
import org.apache.log4j.Logger;
import com.stimulus.util.*;

public class HTMLExtractor implements TextExtractor,Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7138851180242634460L;
	protected static final Logger logger = Logger.getLogger(Extractor.class.getName());

	public HTMLExtractor()
	{
	}

	public Reader getText(InputStream is, TempFiles tempFiles, Charset charset) throws ExtractionException
	{
	   return new RemoveHTMLReader(new InputStreamReader(is,charset));
	}

	public class RemoveHTMLReader extends FilterReader {
		  /** A trivial constructor.  Just initialze our superclass */
		  public RemoveHTMLReader(Reader in) { super(in); }

		  boolean intag = false;    // Used to remember whether we are "inside" a tag

		  /**
		   * This is the implementation of the no-op read() method of FilterReader.
		   * It calls in.read() to get a buffer full of characters, then strips
		   * out the HTML tags.  (in is a protected field of the superclass).
		   **/
		  @Override
		public int read(char[] buf, int from, int len) throws IOException {
		    int numchars = 0;        // how many characters have been read
		    // Loop, because we might read a bunch of characters, then strip them
		    // all out, leaving us with zero characters to return.
		    while (numchars == 0) {
		      numchars = in.read(buf, from, len);     // Read characters
		      if (numchars == -1) return -1;          // Check for EOF and handle it.
		      // Loop through the characters we read, stripping out HTML tags.
		      // Characters not in tags are copied over any previous tags in the buffer
		      int last = from;                          // Index of last non-HTML char
		      for(int i = from; i < from + numchars; i++) {
		        if (!intag) {                           // If not in an HTML tag
		          if (buf[i] == '<') intag = true;      //   check for start of a tag
		          else buf[last++] = buf[i];            //   and copy the character
		        }
		        else if (buf[i] == '>') intag = false;  // Else, check for end of tag
		      }
		      numchars = last - from;   // Figure out how many characters remain
		    }                           // And if it is more than zero characters
		    return numchars;            // Then return that number.
		  }


		  /**
		   * This is another no-op read() method we have to implement.  We
		   * implement it in terms of the method above.  Our superclass implements
		   * the remaining read() methods in terms of these two.
		   **/
		  @Override
		public int read() throws IOException {
		    char[] buf = new char[1];
		    int result = read(buf, 0, 1);
		    if (result == -1) return -1;
		    else return buf[0];
		  }
	}
}


