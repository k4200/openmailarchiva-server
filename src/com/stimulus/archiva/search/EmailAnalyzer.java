
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


package com.stimulus.archiva.search;
import org.apache.lucene.analysis.*;

import java.io.*;
import org.apache.lucene.analysis.standard.*;

public class EmailAnalyzer extends Analyzer {
      
       /*
       * Create a token stream for this analyzer.
       */
      public final TokenStream tokenStream(String fieldName,final Reader reader)
      {
          TokenStream result = new StandardTokenizer(reader);
          result = new EmailFilter(result);
          return result;
     }

}
