
/* Copyright (C) 2005 Jamie Angus Band 
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

package com.stimulus.archiva.language;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;


  
  public class LIDFilterReader extends FilterReader implements Serializable {
  /**
	 * 
	 */
	private static final long serialVersionUID = -564285968039628141L;

    // Fields.
    // -------------------------------------------------------------------------

    protected StringBuffer sample;
    protected String lang = null;
    protected LanguageIdentifier lid = null;
   
    // Constructors.
    // -------------------------------------------------------------------------

    public LIDFilterReader(Reader in,LanguageIdentifier lid)
    {
      this(in, lid, new StringBuffer());
      
    }

    public LIDFilterReader(Reader in, LanguageIdentifier lid, StringBuffer sample)
    {
      super(in);
      this.lid = lid;
      this.sample = sample;
    }

    // Instance methods.
    // -------------------------------------------------------------------------

    public synchronized int read() throws IOException
    {
      int i = in.read();
      
      if (lang==null && sample.length()<lid.getAnalyzeLength()) 
          sample.append(i);
      if (lang==null && sample.length()>=lid.getAnalyzeLength()) {
          lang = lid.identify(sample);
          languageDetected(lang);
      }
 
      return i;
    }

    public synchronized int read(char[] buf, int off, int len) throws IOException
    {
      int l = in.read(buf, off, len);
      if (lang==null && sample.length()<lid.getAnalyzeLength()) 
          sample.append(buf, off, len);
      if (lang==null && sample.length()>=lid.getAnalyzeLength()) { 
          lang = lid.identify(sample);
          languageDetected(lang);
      }
      return l;
    }

    public synchronized int read(char[] buf) throws IOException
    {
      return read(buf, 0, buf.length);
    }

    public synchronized long skip(long len) throws IOException
    {
      long l = 0;
      int i = 0;
      char[] buf = new char[1024];
      while (l < len)
        {
          i = read(buf, 0, (int) Math.min((long) buf.length, len - l));
          if (i == -1)
            break;
          l += i;
        }
      return l;
    }

    StringBuffer getSample()
    {
      return sample;
    }
    
    public void close() throws IOException {
        in.close();
        if (lang==null) {
            lang = lid.identify(sample);
            languageDetected(lang);
        }
    }
    
    public String getLanguage() {
        return lang;
    }
    
    // override this method for notification
    public void languageDetected(String lang) {
        
    }
  }
